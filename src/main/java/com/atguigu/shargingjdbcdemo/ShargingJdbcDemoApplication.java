package com.atguigu.shargingjdbcdemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.atguigu.shargingjdbcdemo.mapper")
public class ShargingJdbcDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShargingJdbcDemoApplication.class, args);
    }

}
