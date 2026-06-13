package com.cloudbase.module.system.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 示例定时任务 �?用于测试调度功能
 */
@Slf4j
@Component("noOpJob")
public class NoOpJob {

    public void execute(String param) {
        log.info("NoOpJob执行: {}", param);
    }

    public void execute() {
        log.info("NoOpJob执行（无参数）");
    }
}
