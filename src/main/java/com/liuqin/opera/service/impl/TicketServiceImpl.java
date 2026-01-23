package com.liuqin.opera.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liuqin.opera.common.dto.CreateTicketDTO;
import com.liuqin.opera.entity.PerformanceEvent;
import com.liuqin.opera.entity.TicketOrder;
import com.liuqin.opera.mapper.TicketOrderMapper;
import com.liuqin.opera.service.PerformanceEventService;
import com.liuqin.opera.service.TicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class TicketServiceImpl extends ServiceImpl<TicketOrderMapper, TicketOrder> implements TicketService {

    private final PerformanceEventService performanceEventService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Local Lock Map to replace Redis for standalone deployment
    // Key: "event:ID:seat:ID", Value: LockInfo
    private static final Map<String, LockInfo> LOCK_MAP = new ConcurrentHashMap<>();

    private static class LockInfo {
        Long userId;
        long expireTime;

        public LockInfo(Long userId, long expireTime) {
            this.userId = userId;
            this.expireTime = expireTime;
        }
    }

    public TicketServiceImpl(PerformanceEventService performanceEventService) {
        this.performanceEventService = performanceEventService;
    }

    @Override
    public boolean lockSeat(Long eventId, String seatId, Long userId) {
        String key = "event:" + eventId + ":seat:" + seatId;
        long now = System.currentTimeMillis();

        // Check existing lock
        LockInfo existing = LOCK_MAP.get(key);
        if (existing != null) {
            if (existing.expireTime < now) {
                // Expired, try to remove
                LOCK_MAP.remove(key, existing);
            } else {
                // Still valid
                // If locked by same user, consider it success (re-entrant)
                if (existing.userId.equals(userId)) {
                    return true;
                }
                return false;
            }
        }

        // Try to lock
        LockInfo newLock = new LockInfo(userId, now + TimeUnit.MINUTES.toMillis(15));
        LockInfo result = LOCK_MAP.putIfAbsent(key, newLock);
        
        // Double check if we lost race or it was just expired and removed
        if (result == null) {
            return true;
        }
        
        // If result is not null, someone else locked it in between
        // Check if it is expired (unlikely but possible)
        if (result.expireTime < now) {
            LOCK_MAP.remove(key, result);
            return LOCK_MAP.putIfAbsent(key, newLock) == null;
        }
        
        return result.userId.equals(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TicketOrder createTicketOrder(CreateTicketDTO dto, Long userId) {
        Long eventId = dto.getEventId();
        String seatId = dto.getSeatId();
        String key = "event:" + eventId + ":seat:" + seatId;

        // Verify Lock
        LockInfo info = LOCK_MAP.get(key);
        if (info == null) {
            // For better UX, check if it's already sold in DB?
            // But strict process requires locking first.
            throw new RuntimeException("请先选座锁定");
        }
        if (!info.userId.equals(userId)) {
            throw new RuntimeException("座位已被他人锁定");
        }
        if (info.expireTime < System.currentTimeMillis()) {
            LOCK_MAP.remove(key);
            throw new RuntimeException("锁定已过期，请重新选座");
        }

        PerformanceEvent event = performanceEventService.getById(eventId);
        if (event == null) {
            throw new RuntimeException("演出不存在");
        }

        // Update seat layout JSON
        // Assume JSON is List<Map<String, Object>> for flexibility
        // Format: [{"id": "1-1", "status": 0}, ...] 0:Available, 1:Sold
        String layoutJson = event.getSeatLayoutJson();
        List<Map<String, Object>> seats;
        if (!StringUtils.hasText(layoutJson)) {
            // Lazy initialization: Generate default 10x16 grid
            seats = new java.util.ArrayList<>();
            for (int r = 1; r <= 10; r++) {
                for (int c = 1; c <= 16; c++) {
                    java.util.Map<String, Object> s = new java.util.HashMap<>();
                    s.put("id", r + "-" + c);
                    s.put("status", 0);
                    seats.add(s);
                }
            }
        } else {
            try {
                seats = objectMapper.readValue(layoutJson, new TypeReference<List<Map<String, Object>>>() {});
            } catch (Exception e) {
                 // Fallback if parse fails? Or throw?
                 throw new RuntimeException("解析座位图失败");
            }
        }

        boolean found = false;
        for (Map<String, Object> seat : seats) {
            String id = String.valueOf(seat.get("id"));
            if (id.equals(seatId)) {
                Object statusObj = seat.get("status");
                int status = statusObj instanceof Integer ? (Integer) statusObj : 0;
                if (status == 1) {
                    throw new RuntimeException("座位已售出");
                }
                seat.put("status", 1); // Mark as Sold
                found = true;
                break;
            }
        }
        
        if (!found) {
            throw new RuntimeException("座位不存在");
        }
        
        try {
            event.setSeatLayoutJson(objectMapper.writeValueAsString(seats));
            performanceEventService.updateById(event);
        } catch (Exception e) {
            throw new RuntimeException("更新座位图失败");
        }

        TicketOrder order = new TicketOrder();
        order.setOrderNo("TKT" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6));
        order.setUserId(userId);
        order.setEventId(eventId);
        order.setSeatInfo(seatId);
        order.setPrice(event.getTicketPrice());
        order.setStatus(1); // Paid/Active
        order.setCreatedTime(LocalDateTime.now());
        order.setPayTime(LocalDateTime.now());
        // Simple QR Code content
        order.setQrCode("EVENT:" + eventId + ":SEAT:" + seatId + ":USER:" + userId);
        
        this.save(order);

        // Cleanup Lock
        LOCK_MAP.remove(key);

        return order;
    }
}
