package com.cloudbase.module.system.quartz;

import java.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;

/**
 * 定时任务抽象基类 — 子类实现 doExecute 方法
 * 参考 RuoYi AbstractQuartzJob
 */
@Slf4j
public abstract class AbstractQuartzJob {

    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
            5, 10, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    /**
     * 执行任务（子类实现）
     */
    protected abstract void doExecute(String param) throws Exception;

    /**
     * 调度器入口
     */
    public void execute(String jobName, String param) {
        EXECUTOR.submit(() -> {
            long start = System.currentTimeMillis();
            try {
                log.info("定时任务[{}]开始执行", jobName);
                doExecute(param);
                log.info("定时任务[{}]执行完成，耗时{}ms", jobName, System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.error("定时任务[{}]执行失败", jobName, e);
            }
        });
    }
}
