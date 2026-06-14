package com.cloudbase.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloudbase.common.core.pojo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
public class SysNotice extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long noticeId;

    private String noticeTitle;

    /** 1-通知 2-公告 */
    private Integer noticeType;

    private String noticeContent;

    private Integer status;
}
