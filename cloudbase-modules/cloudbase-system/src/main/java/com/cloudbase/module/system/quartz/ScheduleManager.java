package com.cloudbase.module.system.quartz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudbase.module.system.entity.SysJob;
import com.cloudbase.module.system.mapper.SysJobMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 定时任务调度管理器 — 基于 ThreadPoolTaskScheduler
 * 参考 RuoYi ScheduleManager
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleManager {

    private final SysJobMapper sysJobMapper;
    private ThreadPoolTaskScheduler scheduler;
    private final Map<Long, ScheduledFuture<?>> runningTasks = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("schedule-");
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.initialize();

        // 启动所有状态为"正常"的任务
        refreshAll();
    }

    @PreDestroy
    public void destroy() {
        runningTasks.values().forEach(f -> f.cancel(false));
        runningTasks.clear();
        if (scheduler != null) scheduler.destroy();
    }

    /** 刷新所有启用的任务 */
    public void refreshAll() {
        sysJobMapper.selectList(new LambdaQueryWrapper<SysJob>().eq(SysJob::getStatus, 1))
                .forEach(this::startJob);
    }

    /** 启动单个任务 */
    public void startJob(SysJob job) {
        stopJob(job.getJobId());
        try {
            ScheduledFuture<?> future = scheduler.schedule(
                    () -> executeJob(job),
                    new CronTrigger(job.getCronExpression())
            );
            runningTasks.put(job.getJobId(), future);
            log.info("定时任务[{}]已启动,cron={}", job.getJobName(), job.getCronExpression());
        } catch (Exception e) {
            log.error("启动定时任务[{}]失败: {}", job.getJobName(), e.getMessage());
        }
    }

    /** 停止单个任务 */
    public void stopJob(Long jobId) {
        ScheduledFuture<?> future = runningTasks.remove(jobId);
        if (future != null && !future.isCancelled()) {
            future.cancel(false);
        }
    }

    /** 判断任务是否运行中 */
    public boolean isRunning(Long jobId) {
        ScheduledFuture<?> future = runningTasks.get(jobId);
        return future != null && !future.isCancelled();
    }

    /** 立即执行一次任务 */
    public void runOnce(SysJob job) {
        scheduler.execute(() -> executeJob(job));
    }

    private void executeJob(SysJob job) {
        long start = System.currentTimeMillis();
        log.info("定时任务[{}]开始执行", job.getJobName());
        try {
            // 通过反射调用目标方法
            String target = job.getInvokeTarget();
            if (target != null && target.contains(".")) {
                int i = target.lastIndexOf(".");
                String className = target.substring(0, i);
                String methodName = target.substring(i + 1);
                if (methodName.contains("(")) {
                    methodName = methodName.substring(0, methodName.indexOf("("));
                }
                Class<?> clz = Class.forName(className);
                Object bean = ApplicationContextHolder.getBean(clz);
                Objects.requireNonNull(bean, "未找到Bean: " + className);
                bean.getClass().getMethod(methodName).invoke(bean);
            }
            log.info("定时任务[{}]完成，耗时{}ms", job.getJobName(), System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("定时任务[{}]失败: {}", job.getJobName(), e.getMessage(), e);
        }
    }
}
