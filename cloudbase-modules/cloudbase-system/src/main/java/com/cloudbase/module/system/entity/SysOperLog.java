package com.cloudbase.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志（不继承BaseEntity，简化字段）
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long logId;

    /** 操作模块 */
    private String module;

    /** 操作类型 */
    private String operType;

    /** 操作方法 */
    private String method;

    /** 请求参数 */
    private String requestParam;

    /** 响应结果 */
    private String responseResult;

    /** 操作人ID */
    private Long operUserId;

    /** 操作人名称 */
    private String operUserName;

    /** 操作IP */
    private String operIp;

    /** 操作时间 */
    private LocalDateTime operTime;

    /** 耗时(ms) */
    private Long costTime;

    /** 是否成功 1-成功 0-失败 */
    private Integer success;
}
