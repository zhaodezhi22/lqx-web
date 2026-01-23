package com.lqx.opera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqx.opera.entity.PointsLog;
import java.util.List;

public interface PointsService extends IService<PointsLog> {
    void addPoints(Long userId, Integer points, String reason);
    List<PointsLog> getPointsHistory(Long userId);
    String dailyCheckIn(Long userId);
}
