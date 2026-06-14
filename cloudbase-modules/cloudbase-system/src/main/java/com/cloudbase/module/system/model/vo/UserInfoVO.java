package com.cloudbase.module.system.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户基础信息VO（用于登录返回、前端展示）
 */
@Data
public class UserInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String account;
    private String realName;
    private Long deptId;
    private String avatar;
}
