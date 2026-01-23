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
    }
}
