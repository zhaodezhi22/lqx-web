package com.lqx.opera.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        String sql = "CREATE TABLE IF NOT EXISTS product_log (" +
                "log_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "product_id BIGINT, " +
                "product_name VARCHAR(255), " +
                "operator_id BIGINT, " +
                "action_type VARCHAR(50), " +
                "content TEXT, " +
                "create_time DATETIME" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        
        jdbcTemplate.execute(sql);
        System.out.println("Checked/Created product_log table.");
    }
}
