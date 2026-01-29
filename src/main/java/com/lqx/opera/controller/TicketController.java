package com.lqx.opera.controller;

import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.common.dto.CreateTicketDTO;
import com.lqx.opera.common.dto.LockSeatRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lqx.opera.common.dto.TicketOrderDetailDto;
import com.lqx.opera.entity.PerformanceEvent;
import com.lqx.opera.entity.TicketOrder;
import com.lqx.opera.service.PerformanceEventService;
import com.lqx.opera.service.TicketService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ticket")
public class TicketController {

    private final TicketService ticketService;
    private final PerformanceEventService performanceEventService;
    private final com.lqx.opera.service.SysUserService sysUserService;

    public TicketController(TicketService ticketService, PerformanceEventService performanceEventService, com.lqx.opera.service.SysUserService sysUserService) {
        this.ticketService = ticketService;
        this.performanceEventService = performanceEventService;
        this.sysUserService = sysUserService;
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
    public Result<List<TicketOrderDetailDto>> getMyTickets(HttpServletRequest request) {
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

        return Result.success(ticketService.getUserTicketDetails(userId));
    }

    @PostMapping("/refund/{orderId}")
    public Result<Boolean> refundTicket(@PathVariable Long orderId, HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            return Result.fail(HttpStatus.UNAUTHORIZED.value(), "未登录");
        }
        Long userId;
        if (userIdObj instanceof Number) {
            userId = ((Number) userIdObj).longValue();
        } else {
            userId = Long.parseLong(userIdObj.toString());
        }

        try {
            boolean success = ticketService.applyRefund(orderId, userId);
            return success ? Result.success(true) : Result.fail("申请退票失败");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PostMapping("/refund/audit/{id}")
    @RequireRole({2, 3}) // Admin/Reviewer
    public Result<Boolean> auditRefund(@PathVariable Long id, @RequestParam Boolean pass) {
        try {
            boolean ok = ticketService.auditRefund(id, pass);
            return ok ? Result.success(true) : Result.fail("审核失败");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/orders")
    @RequireRole({2, 3})
    public Result<List<TicketOrderDetailDto>> getOrders(@RequestParam(required = false) Integer status) {
        return Result.success(ticketService.getAllTicketDetails(status));
    }

    @GetMapping("/upcoming")
    public Result<List<com.lqx.opera.dto.EventDetailDTO>> upcoming(@RequestParam(required = false, defaultValue = "3") Integer limit) {
        LambdaQueryWrapper<PerformanceEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PerformanceEvent::getStatus, 1)
                .ge(PerformanceEvent::getShowTime, LocalDateTime.now())
                .orderByAsc(PerformanceEvent::getShowTime);
        Page<PerformanceEvent> page = new Page<>(1, limit == null ? 3 : limit);
        List<PerformanceEvent> events = performanceEventService.page(page, wrapper).getRecords();
        
        if (events.isEmpty()) {
            return Result.success(java.util.Collections.emptyList());
        }

        // Collect publisher IDs
        java.util.Set<Long> userIds = events.stream()
            .map(PerformanceEvent::getPublisherId)
            .filter(java.util.Objects::nonNull)
            .collect(java.util.stream.Collectors.toSet());
            
        java.util.Map<Long, com.lqx.opera.entity.SysUser> userMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            List<com.lqx.opera.entity.SysUser> users = sysUserService.listByIds(userIds);
            for (com.lqx.opera.entity.SysUser u : users) {
                userMap.put(u.getUserId(), u);
            }
        }

        List<com.lqx.opera.dto.EventDetailDTO> dtos = events.stream().map(event -> {
            com.lqx.opera.dto.EventDetailDTO dto = new com.lqx.opera.dto.EventDetailDTO();
            org.springframework.beans.BeanUtils.copyProperties(event, dto);
            
            if (event.getPublisherId() != null) {
                com.lqx.opera.entity.SysUser uploader = userMap.get(event.getPublisherId());
                if (uploader != null) {
                    if (uploader.getRole() == 2 || uploader.getRole() == 3) {
                        dto.setPublisherName("官方发布");
                        dto.setPublisherAvatar("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
                    } else {
                        dto.setPublisherName(uploader.getRealName() != null ? uploader.getRealName() : uploader.getUsername());
                        dto.setPublisherAvatar(uploader.getAvatar());
                    }
                    dto.setPublisherRole(uploader.getRole());
                }
            }
            return dto;
        }).collect(java.util.stream.Collectors.toList());

        return Result.success(dtos);
    }

    /**
     * 核销门票
     */
    @PostMapping("/verify")
    @RequireRole({1, 2, 3}) // 传承人或工作人员
    public Result<Boolean> verifyTicket(@RequestBody VerifyTicketRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");

        try {
            ticketService.verifyTicket(req.getOrderNo(), userId);
            return Result.success(true);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @lombok.Data
    public static class VerifyTicketRequest {
        private String orderNo;
    }
}
