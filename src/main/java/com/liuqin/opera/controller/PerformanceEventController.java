package com.liuqin.opera.controller;

import com.liuqin.opera.common.Result;
import com.liuqin.opera.common.annotation.RequireRole;
import com.liuqin.opera.entity.PerformanceEvent;
import com.liuqin.opera.service.PerformanceEventService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class PerformanceEventController {

    private final PerformanceEventService performanceEventService;

    public PerformanceEventController(PerformanceEventService performanceEventService) {
        this.performanceEventService = performanceEventService;
    }

    @GetMapping
    public Result<List<PerformanceEvent>> list(@RequestParam(required = false) Integer status,
                                               @RequestParam(required = false) Boolean upcoming,
                                               @RequestParam(required = false) Boolean all) {
        LambdaQueryWrapper<PerformanceEvent> wrapper = new LambdaQueryWrapper<>();
        
        if (Boolean.TRUE.equals(all)) {
            // No status filter (return all)
        } else if (status != null) {
            wrapper.eq(PerformanceEvent::getStatus, status);
        } else {
            wrapper.eq(PerformanceEvent::getStatus, 1);
        }
        
        if (Boolean.TRUE.equals(upcoming)) {
            wrapper.ge(PerformanceEvent::getShowTime, LocalDateTime.now());
        }
        
        wrapper.orderByDesc(PerformanceEvent::getShowTime); // Admin usually wants latest first, or maybe sorting by time is fine.
        // Original was orderByAsc showTime (for upcoming events, earliest first makes sense).
        // Let's keep Asc for upcoming, but maybe Desc for admin list?
        // Let's stick to one order for now or make it dynamic.
        // If 'all' is true (admin), maybe Desc is better.
        if (Boolean.TRUE.equals(all)) {
            wrapper.orderByDesc(PerformanceEvent::getEventId);
        } else {
            wrapper.orderByAsc(PerformanceEvent::getShowTime);
        }
        
        return Result.success(performanceEventService.list(wrapper));
    }

    @PostMapping
    @RequireRole({2, 3})
    public Result<Boolean> add(@RequestBody PerformanceEvent event) {
        if (event.getStatus() == null) {
            event.setStatus(1); // Default to Active
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
        return Result.success(event);
    }

    @PostMapping("/create")
    @RequireRole(1)
    public Result<Boolean> create(@RequestBody PerformanceEvent event, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");
        event.setPublisherId(userId);
        event.setStatus(1); 
        return performanceEventService.save(event) ? Result.success(true) : Result.fail("发布失败");
    }

    @GetMapping("/my-events")
    @RequireRole(1)
    public Result<List<PerformanceEvent>> myEvents(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");
        return Result.success(performanceEventService.list(new LambdaQueryWrapper<PerformanceEvent>()
                .eq(PerformanceEvent::getPublisherId, userId)
                .orderByDesc(PerformanceEvent::getShowTime)));
    }
}
