package com.cloudbase.module.system.quartz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudbase.module.system.entity.SysJob;
import com.cloudbase.module.system.entity.SysJobLog;
import com.cloudbase.module.system.mapper.SysJobLogMapper;
import com.cloudbase.module.system.mapper.SysJobMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
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
    private final SysJobLogMapper sysJobLogMapper;
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

    /**
     * 执行任务并记录日志到 sys_job_log
     * 支持 invokeTarget 格式：
     *   com.xxx.ClassName.methodName         — 无参调用
     *   com.xxx.ClassName.methodName("arg")  — 带一个 String 参数
     */
    private void executeJob(SysJob job) {
        LocalDateTime startTime = LocalDateTime.now();
        long startMs = System.currentTimeMillis();
        SysJobLog jobLog = new SysJobLog();
        jobLog.setJobName(job.getJobName());
        jobLog.setJobGroup(job.getJobGroup());
        jobLog.setInvokeTarget(job.getInvokeTarget());
        jobLog.setStartTime(startTime);

        log.info("定时任务[{}]开始执行", job.getJobName());
        try {
            invokeTarget(job.getInvokeTarget());
            long costTime = System.currentTimeMillis() - startMs;
            jobLog.setStatus(1);
            jobLog.setEndTime(LocalDateTime.now());
            jobLog.setCostTime(costTime);
            log.info("定时任务[{}]完成，耗时{}ms", job.getJobName(), costTime);
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startMs;
            jobLog.setStatus(0);
            jobLog.setEndTime(LocalDateTime.now());
            jobLog.setCostTime(costTime);
            jobLog.setExceptionInfo(truncate(e.getMessage(), 2000));
            log.error("定时任务[{}]失败: {}", job.getJobName(), e.getMessage(), e);
        } finally {
            try {
                sysJobLogMapper.insert(jobLog);
            } catch (Exception e) {
                log.error("记录任务日志失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 通过反射调用目标方法，支持无参和一个 String 参数两种形式
     */
    private void invokeTarget(String target) throws Exception {
        if (target == null || !target.contains(".")) {
            throw new IllegalArgumentException("无效的调用目标: " + target);
        }

        // 提取参数（如果有）
        String param = null;
        int parenStart = target.indexOf("(");
        int parenEnd = target.indexOf(")");
        String classAndMethod = (parenStart > 0) ? target.substring(0, parenStart) : target;

        if (parenStart > 0 && parenEnd > parenStart) {
            param = target.substring(parenStart + 1, parenEnd).trim();
            // 去除引号
            if (param.startsWith("\"") && param.endsWith("\"")) {
                param = param.substring(1, param.length() - 1);
            }
        }

        int i = classAndMethod.lastIndexOf(".");
        String className = classAndMethod.substring(0, i);
        String methodName = classAndMethod.substring(i + 1);

        Class<?> clz = Class.forName(className);
        Object bean = ApplicationContextHolder.getBean(clz);
        Objects.requireNonNull(bean, "未找到Bean: " + className);

        if (param != null) {
            // 尝试带 String 参数的方法
            try {
                Method method = bean.getClass().getMethod(methodName, String.class);
                method.invoke(bean, param);
                return;
            } catch (NoSuchMethodException ignored) {
                // 回退到无参方法
            }
        }
        // 无参调用
        Method method = bean.getClass().getMethod(methodName);
        method.invoke(bean);
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() > maxLen ? str.substring(0, maxLen) : str;
    }
}
