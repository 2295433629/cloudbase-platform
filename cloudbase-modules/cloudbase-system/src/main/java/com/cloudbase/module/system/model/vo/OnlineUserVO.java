package com.cloudbase.module.system.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 在线用户VO
 */
@Data
public class OnlineUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;
    private Long userId;
    private String account;
    private String realName;
    private Long deptId;
    private Long loginTime;
    private String ipAddress;
    private Long expire;
}
