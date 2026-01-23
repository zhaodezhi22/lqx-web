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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TicketServiceImpl extends ServiceImpl<TicketOrderMapper, TicketOrder> implements TicketService {

    private final PerformanceEventService performanceEventService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TicketServiceImpl(PerformanceEventService performanceEventService) {
        this.performanceEventService = performanceEventService;
    }

    @Override
    public boolean lockSeat(Long eventId, String seatId, Long userId) {
        // Simple mock implementation as the focus is on createOrder fix
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TicketOrder createTicketOrder(CreateTicketDTO dto, Long userId) {
        System.out.println("DEBUG: Entering createTicketOrder. userId=" + userId + ", dto=" + dto);
        
        // 1. Basic validation
        if (dto.getEventId() == null || dto.getSeatInfo() == null || dto.getPrice() == null) {
            System.err.println("DEBUG: Missing params. eventId=" + dto.getEventId() + ", seatInfo=" + dto.getSeatInfo() + ", price=" + dto.getPrice());
            throw new RuntimeException("订单参数缺失");
        }

        PerformanceEvent event = performanceEventService.getById(dto.getEventId());
        if (event == null) {
            throw new RuntimeException("演出不存在");
        }
        
        // 2. Update Seat Status in Event
        changeSeatStatus(event, dto.getSeatInfo(), 1);

        // 3. Create Order
        TicketOrder order = new TicketOrder();
        // Generate Order No
        String orderNo = "TKT" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        order.setOrderNo(orderNo);
        
        order.setUserId(userId);
        order.setEventId(dto.getEventId());
        order.setSeatInfo(dto.getSeatInfo());
        order.setPrice(dto.getPrice());
        
        // Status: 1-Paid (Simulated)
        order.setStatus(1);
        order.setCreatedTime(LocalDateTime.now());
        
        boolean saved = this.save(order);
        System.out.println("DEBUG: Order saved? " + saved + ". ID=" + order.getOrderId());
        
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelUnpaidOrders() {
        // Find unpaid orders created more than 30 minutes ago
        List<TicketOrder> expiredOrders = this.lambdaQuery()
                .eq(TicketOrder::getStatus, 0) // 0-Pending
                .lt(TicketOrder::getCreatedTime, LocalDateTime.now().minusMinutes(30))
                .list();

        if (expiredOrders == null || expiredOrders.isEmpty()) {
            return;
        }

        System.out.println("Found " + expiredOrders.size() + " expired orders. Processing cancellations...");

        for (TicketOrder order : expiredOrders) {
            try {
                // 1. Release seat
                PerformanceEvent event = performanceEventService.getById(order.getEventId());
                if (event != null) {
                    changeSeatStatus(event, order.getSeatInfo(), 0); // 0-Available
                }

                // 2. Delete order (as requested: "delete order info")
                this.removeById(order.getOrderId());
                
                System.out.println("Cancelled order: " + order.getOrderNo());
            } catch (Exception e) {
                System.err.println("Failed to cancel order " + order.getOrderNo() + ": " + e.getMessage());
            }
        }
    }

    private void changeSeatStatus(PerformanceEvent event, String seatId, int targetStatus) {
        try {
            List<Map<String, Object>> layout = new ArrayList<>();
            String json = event.getSeatLayoutJson();

            if (json != null && !json.isEmpty()) {
                try {
                    // Try parsing as List
                    layout = objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
                } catch (Exception e) {
                    System.out.println("Warning: seatLayoutJson is not a List. Resetting to empty list. Content: " + json);
                }
            }

            boolean found = false;
            for (Map<String, Object> seat : layout) {
                if (seatId.equals(seat.get("id"))) {
                    // If trying to sell (target=1), check if already sold
                    if (targetStatus == 1) {
                        Object statusObj = seat.get("status");
                        if (statusObj != null) {
                            int currentStatus = 0;
                            if (statusObj instanceof Number) {
                                currentStatus = ((Number) statusObj).intValue();
                            }
                            if (currentStatus == 1) {
                                throw new RuntimeException("该座位已售出");
                            }
                        }
                    }
                    
                    seat.put("status", targetStatus); 
                    found = true;
                    break;
                }
            }

            if (!found) {
                Map<String, Object> newSeat = new HashMap<>();
                newSeat.put("id", seatId);
                newSeat.put("status", targetStatus);
                layout.add(newSeat);
            }

            event.setSeatLayoutJson(objectMapper.writeValueAsString(layout));
            performanceEventService.updateById(event);
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            System.err.println("Failed to update seat status: " + e.getMessage());
            throw new RuntimeException("更新座位状态失败");
        }
    }
}
