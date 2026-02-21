package com.lqx.opera.common.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardStatsDto {
    private Long totalUsers;
    private Long totalInheritors;
    private Long totalResources;

    private MallRevenueData mallRevenue;
    private TicketSalesData ticketSales;
    private List<UserActivityData> userActivities;
    private List<AuditLogData> logs;

    @Data
    public static class MallRevenueData {
        private List<String> dates;
        private List<BigDecimal> amounts;
    }

    @Data
    public static class TicketSalesData {
        private List<String> dates;
        private List<Integer> counts;
        private List<BigDecimal> amounts;
    }

    @Data
    public static class UserActivityData {
        private String time;
        private String userName;
        private String action; // "上传资源", "发布活动", "上架商品"
        private String targetName;
    }

    @Data
    public static class AuditLogData {
        private String time;
        private String type; // "通过", "驳回"
        private String typeClass; // "success", "danger"
        private String content;
    }
}
