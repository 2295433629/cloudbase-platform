package com.cloudbase.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站内信消息
 */
@Data
@TableName("sys_message")
public class SysMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 消息类型: NOTICE-通知, ANNOUNCEMENT-公告 */
    private String msgType;

    /** 发送方式: ALL-全部发送 */
    private String sendType;

    /** 状态: 1-已发布, 0-已撤回 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 是否已读（非数据库字段，查询时动态关联） */
    @TableField(exist = false)
    private Integer isRead;

    /** 阅读时间（非数据库字段，查询时动态关联） */
    @TableField(exist = false)
    private String readTime;
}
