package com.lqx.opera;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.lqx.opera.mapper")
public class LqxOperaApplication {

    public static void main(String[] args) {
        SpringApplication.run(LqxOperaApplication.class, args);
    }
}

