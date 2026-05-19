package com.lqx.opera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.common.dto.WalletSummaryDto;
import com.lqx.opera.common.dto.WalletWithdrawDto;
import com.lqx.opera.entity.MallOrder;
import com.lqx.opera.entity.MallOrderItem;
import com.lqx.opera.entity.PerformanceEvent;
import com.lqx.opera.entity.Product;
import com.lqx.opera.entity.TicketOrder;
import com.lqx.opera.entity.WalletWithdrawRequest;
import com.lqx.opera.mapper.WalletWithdrawRequestMapper;
import com.lqx.opera.service.MallOrderItemService;
import com.lqx.opera.service.MallOrderService;
import com.lqx.opera.service.PerformanceEventService;
import com.lqx.opera.service.ProductService;
import com.lqx.opera.service.TicketService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final ProductService productService;
    private final MallOrderItemService mallOrderItemService;
    private final MallOrderService mallOrderService;
    private final PerformanceEventService performanceEventService;
    private final TicketService ticketService;
    private final WalletWithdrawRequestMapper walletWithdrawRequestMapper;

    public WalletController(ProductService productService,
                            MallOrderItemService mallOrderItemService,
                            MallOrderService mallOrderService,
                            PerformanceEventService performanceEventService,
                            TicketService ticketService,
                            WalletWithdrawRequestMapper walletWithdrawRequestMapper) {
        this.productService = productService;
        this.mallOrderItemService = mallOrderItemService;
        this.mallOrderService = mallOrderService;
        this.performanceEventService = performanceEventService;
        this.ticketService = ticketService;
        this.walletWithdrawRequestMapper = walletWithdrawRequestMapper;
    }

    @GetMapping("/summary")
    @RequireRole({1, 2, 3})
    public Result<WalletSummaryDto> getSummary(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        return Result.success(buildSummary(userId));
    }

    @PostMapping("/withdraw")
    @RequireRole({1, 2, 3})
    public Result<Boolean> withdraw(@RequestBody WalletWithdrawDto dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.fail("提现金额必须大于0");
        }
        if (dto.getAccountName() == null || dto.getAccountName().isBlank()) {
            return Result.fail("请输入收款人");
        }
        if (dto.getAccountNo() == null || dto.getAccountNo().isBlank()) {
            return Result.fail("请输入收款账号");
        }

        WalletSummaryDto summary = buildSummary(userId);
        if (dto.getAmount().compareTo(summary.getAvailableAmount()) > 0) {
            return Result.fail("可提现余额不足");
        }

        WalletWithdrawRequest record = new WalletWithdrawRequest();
        record.setUserId(userId);
        record.setUserRole(role);
        record.setSourceType("ALL");
        record.setAmount(dto.getAmount());
        record.setAccountName(dto.getAccountName().trim());
        record.setAccountNo(dto.getAccountNo().trim());
        record.setRemark(dto.getRemark());
        record.setStatus(1);
        record.setCreateTime(LocalDateTime.now());
        record.setProcessedTime(LocalDateTime.now());
        walletWithdrawRequestMapper.insert(record);
        return Result.success(true);
    }

    private WalletSummaryDto buildSummary(Long userId) {
        BigDecimal mallIncome = calcMallIncome(userId);
        BigDecimal ticketIncome = calcTicketIncome(userId);
        BigDecimal totalIncome = mallIncome.add(ticketIncome);
        BigDecimal withdrawnAmount = calcWithdrawnAmount(userId);
        BigDecimal availableAmount = totalIncome.subtract(withdrawnAmount);
        if (availableAmount.compareTo(BigDecimal.ZERO) < 0) {
            availableAmount = BigDecimal.ZERO;
        }

        List<WalletWithdrawRequest> recentRecords = walletWithdrawRequestMapper.selectList(
                new LambdaQueryWrapper<WalletWithdrawRequest>()
                        .eq(WalletWithdrawRequest::getUserId, userId)
                        .orderByDesc(WalletWithdrawRequest::getId)
                        .last("limit 10")
        );

        WalletSummaryDto dto = new WalletSummaryDto();
        dto.setMallIncome(mallIncome);
        dto.setTicketIncome(ticketIncome);
        dto.setTotalIncome(totalIncome);
        dto.setWithdrawnAmount(withdrawnAmount);
        dto.setAvailableAmount(availableAmount);
        dto.setRecentRecords(recentRecords);
        return dto;
    }

    private BigDecimal calcMallIncome(Long userId) {
        List<Product> myProducts = productService.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getSellerId, userId));
        if (myProducts.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Set<Long> productIds = myProducts.stream().map(Product::getProductId).collect(Collectors.toSet());
        List<MallOrderItem> myItems = mallOrderItemService.list(new LambdaQueryWrapper<MallOrderItem>()
                .in(MallOrderItem::getProductId, productIds));
        if (myItems.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Set<Long> orderIds = myItems.stream().map(MallOrderItem::getOrderId).collect(Collectors.toSet());
        Set<Long> settledOrderIds = mallOrderService.list(new LambdaQueryWrapper<MallOrder>()
                        .in(MallOrder::getId, orderIds)
                        .eq(MallOrder::getStatus, 6))
                .stream()
                .map(MallOrder::getId)
                .collect(Collectors.toSet());
        if (settledOrderIds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return myItems.stream()
                .filter(item -> settledOrderIds.contains(item.getOrderId()))
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcTicketIncome(Long userId) {
        List<PerformanceEvent> myEvents = performanceEventService.list(new LambdaQueryWrapper<PerformanceEvent>()
                .eq(PerformanceEvent::getPublisherId, userId));
        if (myEvents.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Map<Long, PerformanceEvent> eventMap = myEvents.stream()
                .collect(Collectors.toMap(PerformanceEvent::getEventId, Function.identity()));
        List<TicketOrder> orders = ticketService.list(new LambdaQueryWrapper<TicketOrder>()
                .in(TicketOrder::getEventId, eventMap.keySet())
                .in(TicketOrder::getStatus, 1, 2));
        if (orders.isEmpty()) {
            return BigDecimal.ZERO;
        }

        LocalDateTime now = LocalDateTime.now();
        return orders.stream()
                .filter(order -> {
                    if (order.getStatus() != null && order.getStatus() == 2) {
                        return true;
                    }
                    PerformanceEvent event = eventMap.get(order.getEventId());
                    return event != null && event.getShowTime() != null && !event.getShowTime().isAfter(now);
                })
                .map(TicketOrder::getPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcWithdrawnAmount(Long userId) {
        List<WalletWithdrawRequest> records = walletWithdrawRequestMapper.selectList(
                new LambdaQueryWrapper<WalletWithdrawRequest>()
                        .eq(WalletWithdrawRequest::getUserId, userId)
                        .eq(WalletWithdrawRequest::getStatus, 1)
        );
        if (records == null || records.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return records.stream()
                .map(WalletWithdrawRequest::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
