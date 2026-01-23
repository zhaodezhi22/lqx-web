package com.lqx.opera.controller;

import com.lqx.opera.common.Result;
import com.lqx.opera.common.dto.CreateTicketDTO;
import com.lqx.opera.common.dto.LockSeatRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lqx.opera.entity.PerformanceEvent;
import com.lqx.opera.entity.TicketOrder;
import com.lqx.opera.service.PerformanceEventService;
import com.lqx.opera.service.TicketService;
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
        // 从Token 中获取 userId (Object -> Number -> Long)
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            return Result.fail(HttpStatus.UNAUTHORIZED.value(), "未登录");
        }
        Long userId;
        if (userIdObj instanceof Number) {
            userId = ((Number) userIdObj).longValue();
        } else {
            try {
                userId = Long.parseLong(userIdObj.toString());
            } catch (NumberFormatException e) {
                return Result.fail(400, "非法用户ID");
            }
        }

        try {
            TicketOrder order = ticketService.createTicketOrder(dto, userId);
            return Result.success(order);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(500, "购票失败: " + e.getMessage());
        }
    }

    @GetMapping("/my-tickets")
    public Result<List<TicketOrder>> getMyTickets(HttpServletRequest request) {
        // 从Token 中获取 userId (Object -> Number -> Long)
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            return Result.fail(HttpStatus.UNAUTHORIZED.value(), "未登录");
        }
        Long userId;
        if (userIdObj instanceof Number) {
            userId = ((Number) userIdObj).longValue();
        } else {
            try {
                userId = Long.parseLong(userIdObj.toString());
            } catch (NumberFormatException e) {
                return Result.fail(400, "非法用户ID");
            }
        }

        System.out.println("DEBUG: getMyTickets userId=" + userId);
        
        LambdaQueryWrapper<TicketOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TicketOrder::getUserId, userId)
                .orderByDesc(TicketOrder::getCreatedTime);
        List<TicketOrder> list = ticketService.list(wrapper);
        System.out.println("DEBUG: Found " + list.size() + " tickets.");
        return Result.success(list);
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
