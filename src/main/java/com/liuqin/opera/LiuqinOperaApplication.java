package com.liuqin.opera;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.liuqin.opera.mapper")
public class LiuqinOperaApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiuqinOperaApplication.class, args);
    }
}

