package com.lqx.opera.controller;

import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.entity.PerformanceEvent;
import com.lqx.opera.entity.TicketOrder;
import com.lqx.opera.mapper.TicketOrderMapper;
import com.lqx.opera.service.PerformanceEventService;
import com.lqx.opera.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
public class PerformanceEventController {

    private final PerformanceEventService performanceEventService;
    private final SysUserService sysUserService;
    private final TicketOrderMapper ticketOrderMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PerformanceEventController(PerformanceEventService performanceEventService,
                                      SysUserService sysUserService,
                                      TicketOrderMapper ticketOrderMapper) {
        this.performanceEventService = performanceEventService;
        this.sysUserService = sysUserService;
        this.ticketOrderMapper = ticketOrderMapper;
    }

    @GetMapping
    public Result<List<com.lqx.opera.dto.EventDetailDTO>> list(@RequestParam(required = false) Boolean all, @RequestParam(required = false) Boolean history) {
        LambdaQueryWrapper<PerformanceEvent> wrapper = new LambdaQueryWrapper<>();
        if (Boolean.TRUE.equals(all)) {
            // Admin: Sort by Status (1-Selling first), then ShowTime
            wrapper.last("ORDER BY CASE WHEN status = 1 THEN 0 ELSE 1 END ASC, show_time DESC");
        } else if (Boolean.TRUE.equals(history)) {
            // User: History mode - show ended events
            wrapper.eq(PerformanceEvent::getStatus, 1);
            wrapper.le(PerformanceEvent::getShowTime, LocalDateTime.now());
            wrapper.orderByDesc(PerformanceEvent::getShowTime);
        } else {
            // User: Show active events only AND filter out expired events
            wrapper.eq(PerformanceEvent::getStatus, 1);
            wrapper.gt(PerformanceEvent::getShowTime, LocalDateTime.now());
            wrapper.orderByDesc(PerformanceEvent::getShowTime);
        }
        
        List<PerformanceEvent> events = performanceEventService.list(wrapper);
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

        Set<Long> eventIds = events.stream().map(PerformanceEvent::getEventId).collect(Collectors.toSet());
        final Map<Long, Long> occupiedTicketCountMap;
        if (!eventIds.isEmpty()) {
            List<TicketOrder> ticketOrders = ticketOrderMapper.selectList(new LambdaQueryWrapper<TicketOrder>()
                    .in(TicketOrder::getEventId, eventIds)
                    .in(TicketOrder::getStatus, 1, 2, 3));
            occupiedTicketCountMap = ticketOrders.stream()
                    .collect(Collectors.groupingBy(TicketOrder::getEventId, Collectors.counting()));
        } else {
            occupiedTicketCountMap = new java.util.HashMap<>();
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
            int totalSeatCount = resolveTotalSeatCount(event);
            int occupiedCount =  occupiedTicketCountMap.getOrDefault(event.getEventId(), 0L).intValue();
            dto.setSoldTicketCount(occupiedCount);
            dto.setAvailableTicketCount(Math.max(totalSeatCount - occupiedCount, 0));
            return dto;
        }).collect(java.util.stream.Collectors.toList());

        return Result.success(dtos);
    }

    @PostMapping
    @RequireRole({2, 3})
    public Result<Boolean> add(@RequestBody PerformanceEvent event, HttpServletRequest request) {
        if (event.getStatus() == null) {
            event.setStatus(1); // Default to Active
        }
        // Set publisher as current admin
        Long userId = (Long) request.getAttribute("userId");
        if (userId != null) {
            event.setPublisherId(userId);
        }
        
        boolean success = performanceEventService.save(event);
        return success ? Result.success(true) : Result.fail("添加失败");
    }

    @PutMapping
    @RequireRole({2, 3})
    public Result<Boolean> update(@RequestBody PerformanceEvent event) {
        boolean success = performanceEventService.updateById(event);
        return success ? Result.success(true) : Result.fail("更新失败");
    }

    @DeleteMapping("/{id}")
    @RequireRole({2, 3})
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = performanceEventService.removeById(id);
        return success ? Result.success(true) : Result.fail("删除失败");
    }

    @GetMapping("/{id}")
    public Result<PerformanceEvent> detail(@PathVariable Long id) {
        PerformanceEvent event = performanceEventService.getById(id);
        if (event == null) {
            return Result.fail(404, "演出不存在");
        }
        
        com.lqx.opera.dto.EventDetailDTO dto = new com.lqx.opera.dto.EventDetailDTO();
        org.springframework.beans.BeanUtils.copyProperties(event, dto);
        
        if (event.getPublisherId() != null) {
            com.lqx.opera.entity.SysUser uploader = sysUserService.getById(event.getPublisherId());
            if (uploader != null) {
                if (uploader.getRole() == 2 || uploader.getRole() == 3) {
                    dto.setPublisherName("官方发布");
                    dto.setPublisherAvatar("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"); // Official avatar
                } else {
                    dto.setPublisherName(uploader.getRealName() != null ? uploader.getRealName() : uploader.getUsername());
                    dto.setPublisherAvatar(uploader.getAvatar());
                }
                dto.setPublisherRole(uploader.getRole());
            }
        }
        int totalSeatCount = resolveTotalSeatCount(event);
        int occupiedCount = ticketOrderMapper.selectCount(new LambdaQueryWrapper<TicketOrder>()
                .eq(TicketOrder::getEventId, event.getEventId())
                .in(TicketOrder::getStatus, 1, 2, 3)).intValue();
        dto.setSoldTicketCount(occupiedCount);
        dto.setAvailableTicketCount(Math.max(totalSeatCount - occupiedCount, 0));
        return Result.success(dto);
    }

    private int resolveTotalSeatCount(PerformanceEvent event) {
        if (event.getTotalSeats() != null && event.getTotalSeats() > 0) {
            return event.getTotalSeats();
        }
        String json = event.getSeatLayoutJson();
        if (json == null || json.isBlank()) {
            return 0;
        }
        try {
            List<Map<String, Object>> layout = objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
            return layout == null ? 0 : layout.size();
        } catch (Exception e) {
            return 0;
        }
    }

    @PostMapping("/create")
    @RequireRole(1)
    public Result<Boolean> create(@RequestBody PerformanceEvent event, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");
        event.setPublisherId(userId);
        event.setStatus(0); // 0-Pending Audit
        return performanceEventService.save(event) ? Result.success(true) : Result.fail("发布失败");
    }

    @PutMapping("/update")
    @RequireRole(1)
    public Result<Boolean> updateEvent(@RequestBody PerformanceEvent event, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");

        PerformanceEvent existing = performanceEventService.getById(event.getEventId());
        if (existing == null) return Result.fail("演出不存在");

        if (!existing.getPublisherId().equals(userId)) {
            return Result.fail(403, "无权修改此演出");
        }

        // Auto-approve on update
        event.setPublisherId(userId);
        event.setStatus(1);
        
        // Preserve seat layout if not provided (prevent accidental wipe)
        if (event.getSeatLayoutJson() == null) {
            event.setSeatLayoutJson(existing.getSeatLayoutJson());
        }

        boolean success = performanceEventService.updateById(event);
        return success ? Result.success(true) : Result.fail("更新失败");
    }

    @PostMapping("/offline/{id}")
    @RequireRole(1)
    public Result<Boolean> offlineEvent(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");

        PerformanceEvent event = performanceEventService.getById(id);
        if (event == null) return Result.fail("演出不存在");

        if (!event.getPublisherId().equals(userId)) {
            return Result.fail(403, "无权操作此演出");
        }

        event.setStatus(3); // 3 - Offline (Manually taken down by inheritor)
        return performanceEventService.updateById(event) ? Result.success(true) : Result.fail("下架失败");
    }

    @GetMapping("/my-events")
    @RequireRole(1)
    public Result<List<PerformanceEvent>> myEvents(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");
        // Sort order:
        // 1. Pending Audit (status=0)
        // 2. Active & Not Ended (status=1 and show_time > now)
        // 3. Offline (status=3)
        // 4. Ended (show_time <= now)
        // 5. Rejected (status=2)
        // Note: Ended events might be status 1 but show_time <= now.
        // We want Ended to be below Pending.
        // Let's try: Pending(0) -> Active(1 & future) -> Offline(3) -> Ended(1 & past) -> Rejected(2)
        
        String sql = "ORDER BY CASE " +
                     "WHEN status = 0 THEN 1 " + // Pending first
                     "WHEN status = 1 AND show_time > NOW() THEN 2 " + // Active & Future
                     "WHEN status = 3 THEN 3 " + // Offline
                     "WHEN show_time <= NOW() THEN 4 " + // Ended (includes expired status 1)
                     "ELSE 5 END ASC, show_time DESC";

        return Result.success(performanceEventService.list(new LambdaQueryWrapper<PerformanceEvent>()
                .eq(PerformanceEvent::getPublisherId, userId)
                .last(sql)));
    }
}
