package com.osen.cloud.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
// 扫描Mapper配置注解
@MapperScan(basePackages = {"com.osen.cloud.model"})
// 开启事务处理
@EnableTransactionManagement
public class LampblackSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(LampblackSystemApplication.class, args);
    }

}
