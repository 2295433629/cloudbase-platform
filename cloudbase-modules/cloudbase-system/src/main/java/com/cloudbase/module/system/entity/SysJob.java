package com.cloudbase.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.cloudbase.common.core.pojo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 定时任务 — 参考 RuoYi sys_job 设计
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_job")
public class SysJob extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long jobId;

    /** 任务名称 */
    private String jobName;

    /** 任务组名 */
    private String jobGroup;

    /** 调用目标字符串 */
    private String invokeTarget;

    /** cron 表达式 */
    private String cronExpression;

    /** 任务状态 1-正常 0-暂停 */
    private Integer status;

    /** 是否并发执行 1-允许 0-禁止 */
    private Integer concurrent;

    /** 备注 */
    private String remark;
}
