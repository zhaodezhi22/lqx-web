package com.lqx.opera.service;

import com.lqx.opera.common.dto.DashboardStatsDto;

public interface DashboardService {
    DashboardStatsDto getStats(String revenuePeriod);
}
