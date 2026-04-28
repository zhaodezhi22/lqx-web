package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqx.opera.common.dto.CreateTicketDTO;
import com.lqx.opera.common.dto.TicketOrderDetailDto;
import com.lqx.opera.entity.PerformanceEvent;
import com.lqx.opera.entity.TicketOrder;
import com.lqx.opera.mapper.TicketOrderMapper;
import com.lqx.opera.service.PerformanceEventService;
import com.lqx.opera.service.TicketService;
import com.lqx.opera.service.PointsService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl extends ServiceImpl<TicketOrderMapper, TicketOrder> implements TicketService {

    private final PerformanceEventService performanceEventService;
    private final PointsService pointsService;
    private final ObjectMapper objectMapper;

    public TicketServiceImpl(PerformanceEventService performanceEventService,
                             PointsService pointsService,
                             ObjectMapper objectMapper) {
        this.performanceEventService = performanceEventService;
        this.pointsService = pointsService;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<TicketOrderDetailDto> getUserTicketDetails(Long userId) {
        // 1. Get all orders for user
        List<TicketOrder> orders = this.lambdaQuery()
                .eq(TicketOrder::getUserId, userId)
                .orderByDesc(TicketOrder::getCreatedTime)
                .list();

        if (orders.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. Get Event IDs
        List<Long> eventIds = orders.stream()
                .map(TicketOrder::getEventId)
                .distinct()
                .collect(Collectors.toList());

        // 3. Fetch Events
        Map<Long, PerformanceEvent> eventMap = new HashMap<>();
        if (!eventIds.isEmpty()) {
            List<PerformanceEvent> events = performanceEventService.listByIds(eventIds);
            eventMap = events.stream().collect(Collectors.toMap(PerformanceEvent::getEventId, e -> e));
        }

        // 4. Assemble DTOs
        List<TicketOrderDetailDto> dtos = new ArrayList<>();
        for (TicketOrder order : orders) {
            TicketOrderDetailDto dto = new TicketOrderDetailDto();
            BeanUtils.copyProperties(order, dto);
            
            PerformanceEvent event = eventMap.get(order.getEventId());
            if (event != null) {
                dto.setEventTitle(event.getTitle());
                dto.setEventVenue(event.getVenue());
                dto.setShowTime(event.getShowTime());
                // dto.setEventCover(event.getCoverImage()); // If available
            } else {
                dto.setEventTitle("未知演出");
            }
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    public List<TicketOrderDetailDto> getAllTicketDetails(Integer status) {
        // 1. Query Orders
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TicketOrder> query = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (status != null) {
            query.eq(TicketOrder::getStatus, status);
        }
        query.orderByDesc(TicketOrder::getCreatedTime);
        List<TicketOrder> orders = this.list(query);

        if (orders.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. Get Event IDs
        List<Long> eventIds = orders.stream()
                .map(TicketOrder::getEventId)
                .distinct()
                .collect(Collectors.toList());

        // 3. Fetch Events
        Map<Long, PerformanceEvent> eventMap = new HashMap<>();
        if (!eventIds.isEmpty()) {
            List<PerformanceEvent> events = performanceEventService.listByIds(eventIds);
            eventMap = events.stream().collect(Collectors.toMap(PerformanceEvent::getEventId, e -> e));
        }

        // 4. Assemble DTOs
        List<TicketOrderDetailDto> dtos = new ArrayList<>();
        for (TicketOrder order : orders) {
            TicketOrderDetailDto dto = new TicketOrderDetailDto();
            BeanUtils.copyProperties(order, dto);
            
            PerformanceEvent event = eventMap.get(order.getEventId());
            if (event != null) {
                dto.setEventTitle(event.getTitle());
                dto.setEventVenue(event.getVenue());
                dto.setShowTime(event.getShowTime());
            } else {
                dto.setEventTitle("未知演出");
            }
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refundTicket(Long userId, Long orderId) {
        TicketOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (order.getStatus() != 1) { // Only Paid orders can be refunded (Status 1)
            throw new RuntimeException("当前状态无法退票");
        }
        
        // 1. Update Order Status to Refunded (e.g., 3)
        // Status: 0-Unpaid, 1-Paid, 2-Used, 3-Refunded/Cancelled
        order.setStatus(3);
        this.updateById(order);

        // 2. Release Seat
        PerformanceEvent event = performanceEventService.getById(order.getEventId());
        if (event != null) {
            // Check if event has started? Optional business logic.
            if (event.getShowTime().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("演出已结束，无法退票");
            }
            changeSeatStatus(event, order.getSeatInfo(), 0); // 0 = Available
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TicketOrder lockSeat(Long eventId, String seatId, Long userId) {
        releaseExpiredPendingOrders();

        PerformanceEvent event = performanceEventService.getById(eventId);
        if (event == null) {
            throw new RuntimeException("演出不存在");
        }

        TicketOrder existingOrder = this.lambdaQuery()
                .eq(TicketOrder::getEventId, eventId)
                .eq(TicketOrder::getSeatInfo, seatId)
                .eq(TicketOrder::getUserId, userId)
                .eq(TicketOrder::getStatus, 0)
                .orderByDesc(TicketOrder::getCreatedTime)
                .last("limit 1")
                .one();
        if (existingOrder != null && !isOrderExpired(existingOrder)) {
            return existingOrder;
        }

        TicketOrder lockedByOthers = this.lambdaQuery()
                .eq(TicketOrder::getEventId, eventId)
                .eq(TicketOrder::getSeatInfo, seatId)
                .eq(TicketOrder::getStatus, 0)
                .orderByDesc(TicketOrder::getCreatedTime)
                .last("limit 1")
                .one();
        if (lockedByOthers != null && !isOrderExpired(lockedByOthers)) {
            throw new RuntimeException("该座位已被他人锁定，请稍后重试");
        }

        changeSeatStatus(event, seatId, 2);

        TicketOrder order = new TicketOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setEventId(eventId);
        order.setSeatInfo(seatId);
        order.setPrice(event.getTicketPrice());
        order.setStatus(0);
        order.setCreatedTime(LocalDateTime.now());
        this.save(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TicketOrder createTicketOrder(CreateTicketDTO dto, Long userId) {
        if (dto.getEventId() == null || dto.getSeatInfo() == null || dto.getPrice() == null) {
            throw new RuntimeException("订单参数缺失");
        }

        PerformanceEvent event = performanceEventService.getById(dto.getEventId());
        if (event == null) {
            throw new RuntimeException("演出不存在");
        }

        TicketOrder order = lockSeat(dto.getEventId(), dto.getSeatInfo(), userId);
        order.setPrice(dto.getPrice());
        this.updateById(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TicketOrder> payTicketOrders(List<Long> orderIds, Integer usedPoints, Long userId) {
        releaseExpiredPendingOrders();

        if (orderIds == null || orderIds.isEmpty()) {
            throw new RuntimeException("请选择待支付订单");
        }

        List<TicketOrder> orders = this.listByIds(orderIds).stream()
                .sorted(Comparator.comparing(TicketOrder::getOrderId))
                .collect(Collectors.toList());
        if (orders.size() != orderIds.size()) {
            throw new RuntimeException("存在无效订单");
        }

        for (TicketOrder order : orders) {
            if (!userId.equals(order.getUserId())) {
                throw new RuntimeException("订单不属于当前用户");
            }
            if (order.getStatus() != 0) {
                throw new RuntimeException("订单状态已变更，请刷新后重试");
            }
            if (isOrderExpired(order)) {
                throw new RuntimeException("存在超时订单，请重新锁票");
            }
        }

        int pointsToUse = usedPoints == null ? 0 : usedPoints;
        if (pointsToUse > 0 && pointsToUse < 1000) {
            throw new RuntimeException("积分抵扣最低1000起");
        }

        java.math.BigDecimal totalAmount = orders.stream()
                .map(TicketOrder::getPrice)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        int maxPointsByPrice = totalAmount.multiply(java.math.BigDecimal.valueOf(1000)).intValue();
        if (pointsToUse > maxPointsByPrice) {
            throw new RuntimeException("积分抵扣金额不能超过订单总额");
        }
        if (pointsToUse > 0 && !pointsService.deductPoints(userId, pointsToUse, "演出票抵扣")) {
            throw new RuntimeException("积分余额不足");
        }

        LocalDateTime payTime = LocalDateTime.now();
        for (TicketOrder order : orders) {
            PerformanceEvent event = performanceEventService.getById(order.getEventId());
            if (event == null) {
                throw new RuntimeException("演出不存在");
            }
            if (event.getShowTime() != null && event.getShowTime().isBefore(payTime)) {
                throw new RuntimeException("演出已结束，无法支付");
            }

            changeSeatStatus(event, order.getSeatInfo(), 1);
            order.setStatus(1);
            order.setPayTime(payTime);
            order.setQrCode(generateVoucherCode());
            this.updateById(order);
        }

        return orders;
    }

    @Override
    public void verifyTicket(String code, Long verifierId) {
        // Try to find by OrderNo first, then by Voucher Code (qrCode)
        TicketOrder order = this.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TicketOrder>()
                .eq(TicketOrder::getOrderNo, code)
                .or()
                .eq(TicketOrder::getQrCode, code));
        
        if (order == null) {
            throw new RuntimeException("无效的核销码或订单号");
        }
        
        if (order.getStatus() == 2) {
            throw new RuntimeException("该门票已核销，请勿重复核销");
        }
        
        if (order.getStatus() != 1) {
            throw new RuntimeException("订单状态无效(未支付或已取消)");
        }
        
        order.setStatus(2); // 2-Verified
        order.setVerifierId(verifierId);
        order.setVerifyTime(LocalDateTime.now());
        
        this.updateById(order);

        // Earn Points for Event Participation
        pointsService.earnPoints(order.getUserId(), 100, "活动参与奖励 (核销码 " + order.getQrCode() + ")");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelUnpaidOrders() {
        releaseExpiredPendingOrders();
    }

    @Override
    public boolean applyRefund(Long orderId, Long userId) {
        TicketOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (order.getStatus() != 1) {
            throw new RuntimeException("当前状态无法申请退票");
        }
        PerformanceEvent event = performanceEventService.getById(order.getEventId());
        if (event != null && LocalDateTime.now().isAfter(event.getShowTime())) {
             throw new RuntimeException("演出已结束，无法退票");
        }
        order.setStatus(3); // 3-Refund Pending
        return this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditRefund(Long orderId, boolean pass) {
        TicketOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 3) {
            throw new RuntimeException("订单不是待审核状态");
        }

        if (pass) {
            // 通过：释放座位，状态改为4-Refunded
            PerformanceEvent event = performanceEventService.getById(order.getEventId());
            if (event != null) {
                changeSeatStatus(event, order.getSeatInfo(), 0); // Release seat
            }
            order.setStatus(4);
        } else {
            // 驳回：状态回退到1-Paid
            order.setStatus(1);
        }
        return this.updateById(order);
    }

    @Override
    public boolean checkIn(String orderNo) {
        TicketOrder order = this.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TicketOrder>()
                .eq(TicketOrder::getOrderNo, orderNo));
        
        if (order == null) {
            throw new RuntimeException("无效的票号");
        }
        
        if (order.getStatus() == 2) {
            throw new RuntimeException("该票已核销");
        }
        
        if (order.getStatus() != 1) {
            throw new RuntimeException("该票状态不可核销 (状态码: " + order.getStatus() + ")");
        }
        
        // Update to 2-Checked-in
        order.setStatus(2);
        return this.updateById(order);
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
                    // If trying to sell or lock, make sure the seat is not already sold.
                    if (targetStatus == 1 || targetStatus == 2) {
                        Object statusObj = seat.get("status");
                        if (statusObj != null) {
                            int currentStatus = 0;
                            if (statusObj instanceof Number) {
                                currentStatus = ((Number) statusObj).intValue();
                            }
                            if (currentStatus == 1) {
                                throw new RuntimeException("该座位已售出");
                            }
                            if (targetStatus == 2 && currentStatus == 2) {
                                throw new RuntimeException("该座位已锁定");
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

    private void releaseExpiredPendingOrders() {
        List<TicketOrder> expiredOrders = this.lambdaQuery()
                .eq(TicketOrder::getStatus, 0)
                .lt(TicketOrder::getCreatedTime, LocalDateTime.now().minusSeconds(30))
                .list();

        if (expiredOrders == null || expiredOrders.isEmpty()) {
            return;
        }

        for (TicketOrder order : expiredOrders) {
            PerformanceEvent event = performanceEventService.getById(order.getEventId());
            if (event != null) {
                try {
                    changeSeatStatus(event, order.getSeatInfo(), 0);
                } catch (Exception ignored) {
                    // Ignore single-seat release errors and continue cleaning other expired orders.
                }
            }
            this.removeById(order.getOrderId());
        }
    }

    private boolean isOrderExpired(TicketOrder order) {
        return order.getCreatedTime() != null
                && order.getCreatedTime().isBefore(LocalDateTime.now().minusSeconds(30));
    }

    private String generateOrderNo() {
        return "TKT" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String generateVoucherCode() {
        return String.valueOf((int) ((Math.random() * 9 + 1) * 10000000));
    }
}
