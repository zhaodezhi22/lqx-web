package com.lqx.opera.controller;

import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.common.dto.DashboardStatsDto;
import com.lqx.opera.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    @RequireRole({2, 3}) // Admin only
    public Result<DashboardStatsDto> getStats(@RequestParam(defaultValue = "day") String revenuePeriod) {
        return Result.success(dashboardService.getStats(revenuePeriod));
    }
}
