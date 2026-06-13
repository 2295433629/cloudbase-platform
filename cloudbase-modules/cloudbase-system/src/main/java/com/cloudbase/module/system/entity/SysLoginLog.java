package com.cloudbase.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_login_log")
public class SysLoginLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long loginId;

    private String userName;

    private String ipAddress;

    private String browser;

    private String os;

    private Integer status;

    private String msg;

    private Date loginTime;
}
