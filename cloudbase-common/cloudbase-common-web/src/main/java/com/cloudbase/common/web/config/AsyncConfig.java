package com.cloudbase.common.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池配置
 * <p>
 * 替代 Spring 默认的 ForkJoinPool，提供可控的线程池参数。
 * 当任务量超过队列容量时，使用 CallerRunsPolicy 防止任务丢失。
 * </p>
 *
 * <pre>
 * 使用方式：
 * &#064;Async("cloudbaseAsyncExecutor")
 * public void asyncTask() { ... }
 * </pre>
 */
@Slf4j
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Value("${cloudbase.async.core-size:8}")
    private int coreSize;

    @Value("${cloudbase.async.max-size:32}")
    private int maxSize;

    @Value("${cloudbase.async.queue-capacity:256}")
    private int queueCapacity;

    @Value("${cloudbase.async.keep-alive:60}")
    private int keepAliveSeconds;

    @Bean(name = "cloudbaseAsyncExecutor")
    public ThreadPoolTaskExecutor cloudbaseAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix("cloudbase-async-");
        // 拒绝策略：由调用线程执行，保证任务不丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 优雅停机：等待任务执行完毕
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        log.info("异步线程池已初始化: coreSize={}, maxSize={}, queueCapacity={}",
                coreSize, maxSize, queueCapacity);
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return cloudbaseAsyncExecutor();
    }
}
