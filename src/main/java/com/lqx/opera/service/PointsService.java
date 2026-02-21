package com.lqx.opera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqx.opera.entity.PointsLog;
import com.lqx.opera.entity.SysUser;

import java.util.Map;

public interface PointsService extends IService<PointsLog> {
    /**
     * Daily Sign In
     * @param userId
     * @return Result map with points awarded and streak info
     */
    Map<String, Object> signIn(Long userId);

    /**
     * Get user sign in info
     * @param userId
     * @return
     */
    Map<String, Object> getSignInInfo(Long userId);

    /**
     * Earn points generic method
     * @param userId
     * @param points
     * @param reason
     */
    void earnPoints(Long userId, Integer points, String reason);

    /**
     * Deduct points
     * @param userId
     * @param points
     * @param reason
     * @return true if successful
     */
    boolean deductPoints(Long userId, Integer points, String reason);
    
    /**
     * Check and record daily action limit (e.g. view resource, post community)
     * @param userId
     * @param actionType "RESOURCE_VIEW", "COMMUNITY_POST"
     * @param maxCount
     * @param pointsPerAction
     * @return true if points awarded
     */
    boolean recordDailyAction(Long userId, String actionType, int maxCount, int pointsPerAction);
}
