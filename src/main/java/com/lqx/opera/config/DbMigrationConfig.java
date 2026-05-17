package com.lqx.opera.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DbMigrationConfig implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DbMigrationConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            jdbcTemplate.execute("ALTER TABLE performance_event ADD COLUMN publisher_id BIGINT COMMENT '发布人ID'");
            System.out.println("Migration: Added publisher_id to performance_event");
        } catch (Exception e) {
            // Ignore if exists
        }

        try {
            jdbcTemplate.execute("ALTER TABLE product ADD COLUMN seller_id BIGINT COMMENT '卖家ID(传承人)'");
            System.out.println("Migration: Added seller_id to product");
        } catch (Exception e) {
            // Ignore if exists
        }

        try {
            jdbcTemplate.execute("ALTER TABLE community_post ADD COLUMN status TINYINT DEFAULT 0 COMMENT '0-Pending, 1-Approved, 2-Rejected'");
            System.out.println("Migration: Added status to community_post");
        } catch (Exception e) {
            // Ignore
        }

        try {
            // Update default value to 1 (Approved) and auto-approve existing pending posts
            jdbcTemplate.execute("ALTER TABLE community_post MODIFY COLUMN status TINYINT DEFAULT 1 COMMENT '0-Pending, 1-Approved, 2-Rejected'");
            jdbcTemplate.execute("UPDATE community_post SET status = 1 WHERE status = 0");
            System.out.println("Migration: Updated community_post status default to 1 and approved pending posts");
        } catch (Exception e) {
            System.err.println("Migration warning: " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE mall_order ADD COLUMN refund_apply_time DATETIME NULL COMMENT '退款申请时间'");
            System.out.println("Migration: Added refund_apply_time to mall_order");
        } catch (Exception e) {
            // Ignore if exists
        }

        try {
            jdbcTemplate.execute("ALTER TABLE community_post ADD COLUMN source_assignment_id BIGINT NULL COMMENT '来源作业ID'");
            System.out.println("Migration: Added source_assignment_id to community_post");
        } catch (Exception e) {
            // Ignore if exists
        }

        try {
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS resource_category (" +
                            "category_id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                            "name VARCHAR(100) NOT NULL," +
                            "sort_order INT DEFAULT 0," +
                            "created_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                            "UNIQUE KEY uk_resource_category_name (name)" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源分类配置表'"
            );
            System.out.println("Migration: Ensured resource_category table exists");
        } catch (Exception e) {
            System.err.println("Migration warning: " + e.getMessage());
        }

        try {
            jdbcTemplate.execute(
                    "INSERT IGNORE INTO resource_category (name, sort_order) VALUES " +
                            "('柳琴戏历史', 1)," +
                            "('经典剧目', 2)," +
                            "('名家唱段', 3)," +
                            "('脸谱艺术', 4)," +
                            "('服饰道具', 5)," +
                            "('其他', 99)"
            );
            System.out.println("Migration: Seeded resource categories");
        } catch (Exception e) {
            System.err.println("Migration warning: " + e.getMessage());
        }

        try {
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS admin_operation_log (" +
                            "log_id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                            "operator_id BIGINT NULL," +
                            "operator_name VARCHAR(100) NULL," +
                            "operator_role INT NULL," +
                            "request_method VARCHAR(16) NOT NULL," +
                            "request_path VARCHAR(255) NOT NULL," +
                            "request_query VARCHAR(1000) NULL," +
                            "action_name VARCHAR(255) NULL," +
                            "response_status INT NULL," +
                            "success_flag TINYINT DEFAULT 1," +
                            "created_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                            "KEY idx_admin_log_created_time (created_time)," +
                            "KEY idx_admin_log_operator_id (operator_id)" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台操作日志表'"
            );
            System.out.println("Migration: Ensured admin_operation_log table exists");
        } catch (Exception e) {
            System.err.println("Migration warning: " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE heritage_resource ADD COLUMN audit_remark VARCHAR(500) NULL COMMENT '审核备注'");
            System.out.println("Migration: Added audit_remark to heritage_resource");
        } catch (Exception e) {
            // Ignore if exists
        }

        try {
            jdbcTemplate.execute("ALTER TABLE product ADD COLUMN audit_remark VARCHAR(500) NULL COMMENT '审核备注'");
            System.out.println("Migration: Added audit_remark to product");
        } catch (Exception e) {
            // Ignore if exists
        }
    }
}
