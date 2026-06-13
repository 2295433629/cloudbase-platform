package com.cloudbase.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务执行日志
 */
@Data
@TableName("sys_job_log")
public class SysJobLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long logId;

    /** 任务名称 */
    private String jobName;

    /** 任务组名 */
    private String jobGroup;

    /** 调用目标 */
    private String invokeTarget;

    /** 执行状态 1-成功 0-失败 */
    private Integer status;

    /** 异常信息 */
    private String exceptionInfo;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 耗时(ms) */
    private Long costTime;
}
