package com.liuqin.opera.controller;

import com.liuqin.opera.common.Result;
import com.liuqin.opera.common.dto.CreateTicketDTO;
import com.liuqin.opera.common.dto.LockSeatRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liuqin.opera.entity.PerformanceEvent;
import com.liuqin.opera.entity.TicketOrder;
import com.liuqin.opera.service.PerformanceEventService;
import com.liuqin.opera.service.TicketService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ticket")
public class TicketController {

    private final TicketService ticketService;
    private final PerformanceEventService performanceEventService;

    public TicketController(TicketService ticketService, PerformanceEventService performanceEventService) {
        this.ticketService = ticketService;
        this.performanceEventService = performanceEventService;
    }

    @PostMapping("/lock")
    public Result<Boolean> lockSeat(@RequestBody LockSeatRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.fail(HttpStatus.UNAUTHORIZED.value(), "未登录");
        }
        boolean success = ticketService.lockSeat(req.getEventId(), req.getSeatId(), userId);
        if (success) {
            return Result.success(true);
        } else {
            return Result.fail(400, "锁定失败，座位可能已被他人锁定或已售出");
        }
    }

    @PostMapping("/order")
    public Result<TicketOrder> createOrder(@RequestBody CreateTicketDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.fail(HttpStatus.UNAUTHORIZED.value(), "未登录");
        }
        try {
            TicketOrder order = ticketService.createTicketOrder(dto, userId);
            return Result.success(order);
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            return Result.fail(500, "购票失败: " + e.getMessage());
        }
    }

    @GetMapping("/upcoming")
    public Result<List<PerformanceEvent>> upcoming(@RequestParam(required = false, defaultValue = "3") Integer limit) {
        LambdaQueryWrapper<PerformanceEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PerformanceEvent::getStatus, 1)
                .ge(PerformanceEvent::getShowTime, LocalDateTime.now())
                .orderByAsc(PerformanceEvent::getShowTime);
        Page<PerformanceEvent> page = new Page<>(1, limit == null ? 3 : limit);
        return Result.success(performanceEventService.page(page, wrapper).getRecords());
    }
}
