package com.cloudbase.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CloudBase启动类
 */
@EnableAsync
@EnableScheduling
@MapperScan("com.cloudbase.module.system.mapper")
@SpringBootApplication(scanBasePackages = "com.cloudbase")
public class CloudBaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudBaseApplication.class, args);
    }
}
