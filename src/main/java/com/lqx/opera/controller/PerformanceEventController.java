package com.lqx.opera.controller;

import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.entity.PerformanceEvent;
import com.lqx.opera.service.PerformanceEventService;
import com.lqx.opera.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class PerformanceEventController {

    private final PerformanceEventService performanceEventService;
    private final SysUserService sysUserService;

    public PerformanceEventController(PerformanceEventService performanceEventService, SysUserService sysUserService) {
        this.performanceEventService = performanceEventService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<PerformanceEvent>> list(@RequestParam(required = false) Boolean all) {
        LambdaQueryWrapper<PerformanceEvent> wrapper = new LambdaQueryWrapper<>();
        if (Boolean.TRUE.equals(all)) {
            // Admin: Sort by Status (1-Selling first), then ShowTime
            wrapper.last("ORDER BY CASE WHEN status = 1 THEN 0 ELSE 1 END ASC, show_time DESC");
        } else {
            // User: Show active/ended events
            wrapper.in(PerformanceEvent::getStatus, 1, 2);
            wrapper.orderByDesc(PerformanceEvent::getShowTime);
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
        
        com.lqx.opera.dto.EventDetailDTO dto = new com.lqx.opera.dto.EventDetailDTO();
        org.springframework.beans.BeanUtils.copyProperties(event, dto);
        
        if (event.getPublisherId() != null) {
            com.lqx.opera.entity.SysUser uploader = sysUserService.getById(event.getPublisherId());
            if (uploader != null) {
                dto.setPublisherName(uploader.getRealName() != null ? uploader.getRealName() : uploader.getUsername());
                dto.setPublisherAvatar(uploader.getAvatar());
                dto.setPublisherRole(uploader.getRole());
            }
        }
        return Result.success(dto);
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
