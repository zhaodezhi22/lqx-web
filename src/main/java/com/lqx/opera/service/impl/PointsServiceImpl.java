package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqx.opera.entity.DailySignIn;
import com.lqx.opera.entity.PointsLog;
import com.lqx.opera.entity.SysUser;
import com.lqx.opera.mapper.DailySignInMapper;
import com.lqx.opera.mapper.PointsLogMapper;
import com.lqx.opera.mapper.SysUserMapper;
import com.lqx.opera.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PointsServiceImpl extends ServiceImpl<PointsLogMapper, PointsLog> implements PointsService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private DailySignInMapper dailySignInMapper;

    @Autowired
    private PointsLogMapper pointsLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> signIn(Long userId) {
        LocalDate today = LocalDate.now();
        
        // Check if signed in today
        Long count = dailySignInMapper.selectCount(new LambdaQueryWrapper<DailySignIn>()
                .eq(DailySignIn::getUserId, userId)
                .eq(DailySignIn::getSignDate, today));
        
        if (count > 0) {
            throw new RuntimeException("Today already signed in");
        }

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Insert Daily Sign In
        DailySignIn signIn = new DailySignIn();
        signIn.setUserId(userId);
        signIn.setSignDate(today);
        signIn.setCreateTime(LocalDateTime.now());
        dailySignInMapper.insert(signIn);

        // Calculate Streak
        int streak = 1;
        if (user.getLastSignDate() != null && user.getLastSignDate().equals(today.minusDays(1))) {
            streak = (user.getContinuousSignDays() == null ? 0 : user.getContinuousSignDays()) + 1;
        } else if (user.getLastSignDate() != null && user.getLastSignDate().equals(today)) {
            // Should be caught by count check, but safety
            streak = user.getContinuousSignDays();
        }
        
        // Reset streak if month changed? No, user said "Sign in reward resets monthly", 
        // but usually streak is continuous across months unless specified.
        // "这个签到奖励是按照每个月重置的" -> ambiguous. 
        // Interpretation: The "10 days", "20 days" streaks are likely within a month or reset monthly.
        // "本月签到全满" implies monthly logic.
        // Let's assume the streak resets if month changes? 
        // "连续签到10天...本月签到全满" -> usually implies monthly cycle.
        // Let's implement streak within current month. 
        // If today is day 1 of month, streak is 1.
        if (today.getDayOfMonth() == 1) {
            streak = 1;
        }

        // Calculate Points
        int points = 5; // Base
        String desc = "每日签到";

        // Streak Bonuses (Only trigger on the specific day)
        if (streak == 10) {
            points += 50;
            desc += " + 连续签到10天奖励";
        }
        if (streak == 20) {
            points += 100;
            desc += " + 连续签到20天奖励";
        }

        // Full Month Bonus
        // Check if all days in this month up to today (inclusive) are signed in?
        // Or strictly "Full Month" meaning the last day of month and count == length?
        // Requirement: "本月签到全满奖励300积分" -> Usually awarded on the last day if all days present.
        if (today.equals(today.withDayOfMonth(today.lengthOfMonth()))) {
            // It is last day of month. Check count for this month.
            Long monthCount = dailySignInMapper.selectCount(new LambdaQueryWrapper<DailySignIn>()
                    .eq(DailySignIn::getUserId, userId)
                    .ge(DailySignIn::getSignDate, today.withDayOfMonth(1))
                    .le(DailySignIn::getSignDate, today)); // today is last day
            
            if (monthCount != null && monthCount == today.lengthOfMonth()) {
                points += 300;
                desc += " + 全勤奖励";
            }
        }

        // Update User
        user.setContinuousSignDays(streak);
        user.setLastSignDate(today);
        user.setCurrentPoints((user.getCurrentPoints() == null ? 0 : user.getCurrentPoints()) + points);
        sysUserMapper.updateById(user);

        // Log Points
        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setChangePoint(points);
        log.setReason(desc);
        pointsLogMapper.insert(log);

        Map<String, Object> result = new HashMap<>();
        result.put("points", points);
        result.put("continuousSignDays", streak);
        result.put("totalPoints", user.getCurrentPoints());
        return result;
    }

    @Override
    public Map<String, Object> getSignInInfo(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) return null;
        
        LocalDate today = LocalDate.now();
        boolean signedIn = false;
        if (user.getLastSignDate() != null && user.getLastSignDate().equals(today)) {
            signedIn = true;
        }

        Map<String, Object> info = new HashMap<>();
        info.put("signedIn", signedIn);
        info.put("continuousSignDays", user.getContinuousSignDays() == null ? 0 : user.getContinuousSignDays());
        info.put("currentPoints", user.getCurrentPoints() == null ? 0 : user.getCurrentPoints());
        
        // Calculate progress for next bonus
        int streak = user.getContinuousSignDays() == null ? 0 : user.getContinuousSignDays();
        int nextTarget = 0;
        if (streak < 10) nextTarget = 10;
        else if (streak < 20) nextTarget = 20;
        else nextTarget = today.lengthOfMonth(); // Max target
        
        info.put("nextTarget", nextTarget);
        
        return info;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void earnPoints(Long userId, Integer points, String reason) {
        if (points <= 0) return;
        
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) return;

        user.setCurrentPoints((user.getCurrentPoints() == null ? 0 : user.getCurrentPoints()) + points);
        sysUserMapper.updateById(user);

        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setChangePoint(points);
        log.setReason(reason);
        pointsLogMapper.insert(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductPoints(Long userId, Integer points, String reason) {
        if (points <= 0) return false;
        
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) return false;

        int current = user.getCurrentPoints() == null ? 0 : user.getCurrentPoints();
        if (current < points) {
            return false;
        }

        user.setCurrentPoints(current - points);
        sysUserMapper.updateById(user);

        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setChangePoint(-points); // Negative for deduction
        log.setReason(reason);
        pointsLogMapper.insert(log);
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recordDailyAction(Long userId, String actionType, int maxCount, int pointsPerAction) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        Date startDate = Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());

        Long count = pointsLogMapper.selectCount(new LambdaQueryWrapper<PointsLog>()
                .eq(PointsLog::getUserId, userId)
                .likeRight(PointsLog::getReason, actionType)
                .ge(PointsLog::getCreatedTime, startDate)
        );
        
        if (count < maxCount) {
            earnPoints(userId, pointsPerAction, actionType + "奖励");
            return true;
        }
        return false;
    }
}
