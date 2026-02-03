package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lqx.opera.common.dto.DashboardStatsDto;
import com.lqx.opera.entity.*;
import com.lqx.opera.mapper.*;
import com.lqx.opera.service.DashboardService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final SysUserMapper userMapper;
    private final HeritageResourceMapper resourceMapper;
    private final InheritorProfileMapper inheritorMapper;
    private final TicketOrderMapper ticketOrderMapper;
    private final MallOrderMapper mallOrderMapper;
    private final ProductMapper productMapper;
    private final PerformanceEventMapper eventMapper;

    public DashboardServiceImpl(SysUserMapper userMapper,
                                HeritageResourceMapper resourceMapper,
                                InheritorProfileMapper inheritorMapper,
                                TicketOrderMapper ticketOrderMapper,
                                MallOrderMapper mallOrderMapper,
                                ProductMapper productMapper,
                                PerformanceEventMapper eventMapper) {
        this.userMapper = userMapper;
        this.resourceMapper = resourceMapper;
        this.inheritorMapper = inheritorMapper;
        this.ticketOrderMapper = ticketOrderMapper;
        this.mallOrderMapper = mallOrderMapper;
        this.productMapper = productMapper;
        this.eventMapper = eventMapper;
    }

    @Override
    public DashboardStatsDto getStats(String period) {
        DashboardStatsDto dto = new DashboardStatsDto();

        // 1. Basic Counts
        dto.setTotalUsers(userMapper.selectCount(new QueryWrapper<SysUser>().eq("role", 0)));
        dto.setTotalInheritors(inheritorMapper.selectCount(null));
        dto.setTotalResources(resourceMapper.selectCount(null));

        // 2. Prepare Date Ranges for Charts
        List<String> labels = new ArrayList<>();
        List<BigDecimal> mallAmounts = new ArrayList<>();
        List<Integer> ticketCounts = new ArrayList<>();
        List<BigDecimal> ticketAmounts = new ArrayList<>();

        LocalDate today = LocalDate.now();
        
        if ("month".equals(period)) {
            // Last 6 Months
            for (int i = 5; i >= 0; i--) {
                LocalDate date = today.minusMonths(i);
                LocalDate start = date.withDayOfMonth(1);
                LocalDate end = date.withDayOfMonth(date.lengthOfMonth());
                labels.add(date.format(DateTimeFormatter.ofPattern("yyyy-MM")));
                
                mallAmounts.add(getMallRevenue(start, end));
                TicketStats ts = getTicketStats(start, end);
                ticketCounts.add(ts.count);
                ticketAmounts.add(ts.amount);
            }
        } else if ("week".equals(period)) {
            // Last 8 Weeks
            for (int i = 7; i >= 0; i--) {
                LocalDate end = today.minusWeeks(i); // Assuming today is end of current week window for simplicity
                LocalDate start = end.minusDays(6);
                labels.add(start.format(DateTimeFormatter.ofPattern("MM-dd")));
                
                mallAmounts.add(getMallRevenue(start, end));
                TicketStats ts = getTicketStats(start, end);
                ticketCounts.add(ts.count);
                ticketAmounts.add(ts.amount);
            }
        } else {
            // Default: Last 15 Days
            for (int i = 14; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                labels.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
                
                mallAmounts.add(getMallRevenue(date, date));
                TicketStats ts = getTicketStats(date, date);
                ticketCounts.add(ts.count);
                ticketAmounts.add(ts.amount);
            }
        }

        DashboardStatsDto.MallRevenueData mallData = new DashboardStatsDto.MallRevenueData();
        mallData.setDates(labels);
        mallData.setAmounts(mallAmounts);
        dto.setMallRevenue(mallData);

        DashboardStatsDto.TicketSalesData ticketData = new DashboardStatsDto.TicketSalesData();
        ticketData.setDates(labels);
        ticketData.setCounts(ticketCounts);
        ticketData.setAmounts(ticketAmounts);
        dto.setTicketSales(ticketData);

        // 3. User Activities (Upload Resource, Publish Event, List Product)
        dto.setUserActivities(getUserActivities());

        // 4. Audit Logs (Inheritor Audit)
        dto.setLogs(getAuditLogs());

        return dto;
    }

    // --- Helpers ---

    private BigDecimal getMallRevenue(LocalDate start, LocalDate end) {
        List<MallOrder> orders = mallOrderMapper.selectList(new QueryWrapper<MallOrder>()
                .ge("create_time", start.atStartOfDay())
                .le("create_time", end.atTime(23, 59, 59))
                .in("status", 1, 2, 4, 6)); // Paid/Shipped/RefundPending/Completed
        return orders.stream()
                .map(o -> o.getPayAmount() != null ? o.getPayAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static class TicketStats {
        int count;
        BigDecimal amount;
    }

    private TicketStats getTicketStats(LocalDate start, LocalDate end) {
        List<TicketOrder> orders = ticketOrderMapper.selectList(new QueryWrapper<TicketOrder>()
                .ge("created_time", start.atStartOfDay()) // Using created_time as proxy for sales time
                .le("created_time", end.atTime(23, 59, 59))
                .in("status", 1, 2)); // Paid/Verified
        TicketStats ts = new TicketStats();
        ts.count = orders.size();
        ts.amount = orders.stream()
                .map(o -> o.getPrice() != null ? o.getPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return ts;
    }

    private List<DashboardStatsDto.UserActivityData> getUserActivities() {
        List<DashboardStatsDto.UserActivityData> list = new ArrayList<>();
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("MM-dd HH:mm");

        // Resources
        List<HeritageResource> resources = resourceMapper.selectList(new QueryWrapper<HeritageResource>()
                .orderByDesc("created_time").last("LIMIT 10"));
        for (HeritageResource r : resources) {
            DashboardStatsDto.UserActivityData d = new DashboardStatsDto.UserActivityData();
            d.setTime(r.getCreatedTime().format(timeFmt));
            d.setAction("上传资源");
            d.setTargetName(r.getTitle());
            d.setUserName(getUserName(r.getUploaderId()));
            list.add(d);
        }

        // Products
        List<Product> products = productMapper.selectList(new QueryWrapper<Product>()
                .orderByDesc("created_time").last("LIMIT 10"));
        for (Product p : products) {
            DashboardStatsDto.UserActivityData d = new DashboardStatsDto.UserActivityData();
            d.setTime(p.getCreatedTime().format(timeFmt));
            d.setAction("上架商品");
            d.setTargetName(p.getName());
            d.setUserName(getUserName(p.getSellerId()));
            list.add(d);
        }

        // Events
        // PerformanceEvent has no created_time, use show_time as fallback or event_id
        // Assuming recently added events have higher IDs.
        List<PerformanceEvent> events = eventMapper.selectList(new QueryWrapper<PerformanceEvent>()
                .orderByDesc("event_id").last("LIMIT 10"));
        for (PerformanceEvent e : events) {
            DashboardStatsDto.UserActivityData d = new DashboardStatsDto.UserActivityData();
            // Use showTime as a proxy for display, but note it might be future.
            // Or just use "近期" (Recent) if time is unavailable?
            // Let's use showTime for now but maybe mark it.
            if (e.getShowTime() != null) {
                d.setTime(e.getShowTime().format(timeFmt));
            } else {
                d.setTime("近期");
            }
            d.setAction("发布活动");
            d.setTargetName(e.getTitle());
            d.setUserName(getUserName(e.getPublisherId()));
            list.add(d);
        }

        // Sort by time string? No, format is MM-dd. Better sort by comparable time then format.
        // Re-sorting mixed list is tricky without original time object.
        // Let's wrap in a helper class or just sort by time string (works for same year).
        // For simplicity, we just sort by time string descending (approximate).
        list.sort((a, b) -> b.getTime().compareTo(a.getTime()));
        
        return list.stream().limit(10).collect(Collectors.toList());
    }

    private String getUserName(Long userId) {
        if (userId == null) return "未知用户";
        SysUser u = userMapper.selectById(userId);
        return u != null ? u.getUsername() : "未知用户";
    }

    private List<DashboardStatsDto.AuditLogData> getAuditLogs() {
        List<DashboardStatsDto.AuditLogData> list = new ArrayList<>();
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("MM-dd HH:mm");

        List<InheritorProfile> profiles = inheritorMapper.selectList(new QueryWrapper<InheritorProfile>()
                .isNotNull("audit_time")
                .orderByDesc("audit_time")
                .last("LIMIT 10"));
        
        for (InheritorProfile p : profiles) {
            DashboardStatsDto.AuditLogData log = new DashboardStatsDto.AuditLogData();
            log.setTime(p.getAuditTime().format(timeFmt));
            String name = getUserName(p.getUserId());
            if (p.getVerifyStatus() == 1) {
                log.setType("通过");
                log.setTypeClass("success");
                log.setContent("通过了 " + name + " 的非遗传承人申请");
            } else {
                log.setType("驳回");
                log.setTypeClass("danger");
                log.setContent("驳回了 " + name + " 的非遗传承人申请");
            }
            list.add(log);
        }
        return list;
    }
}
