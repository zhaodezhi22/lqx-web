package com.lqx.opera.controller;

import com.lqx.opera.common.Result;
import com.lqx.opera.common.dto.CheckInRequest;
import com.lqx.opera.entity.PointsLog;
import com.lqx.opera.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PointsController {

    @Autowired
    private PointsService pointsService;

    @GetMapping("/api/points/history")
    public Result<List<PointsLog>> getHistory(@RequestParam Long userId) {
        return Result.success(pointsService.getPointsHistory(userId));
    }

    @PostMapping("/api/points/daily-check-in")
    public Result<String> dailyCheckIn(@RequestBody CheckInRequest request) {
        String msg = pointsService.dailyCheckIn(request.getUserId());
        // 如果是“今日已签到”，也许不算错误，只是提示。
        return Result.success(msg);
    }
}
