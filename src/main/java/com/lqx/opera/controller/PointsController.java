package com.lqx.opera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lqx.opera.common.Result;
import com.lqx.opera.entity.PointsLog;
import com.lqx.opera.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/points")
public class PointsController {

    @Autowired
    private PointsService pointsService;

    @PostMapping("/signin")
    public Result<Map<String, Object>> signIn(@RequestParam Long userId) {
        try {
            Map<String, Object> result = pointsService.signIn(userId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> getInfo(@RequestParam Long userId) {
        return Result.success(pointsService.getSignInInfo(userId));
    }

    @GetMapping("/log")
    public Result<Page<PointsLog>> getLog(@RequestParam Long userId,
                                          @RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size) {
        Page<PointsLog> pageInfo = new Page<>(page, size);
        return Result.success(pointsService.page(pageInfo, new LambdaQueryWrapper<PointsLog>()
                .eq(PointsLog::getUserId, userId)
                .orderByDesc(PointsLog::getCreatedTime)));
    }

    @PostMapping("/resource-view")
    public Result<Boolean> resourceView(@RequestParam Long userId) {
        boolean rewarded = pointsService.recordDailyAction(userId, "资源观看", 3, 10);
        return Result.success(rewarded);
    }
}
