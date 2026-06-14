package com.cloudbase.module.system.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户VO（不包含密码）
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String account;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private Long deptId;
    private Integer status;
    private String lastLoginIp;
    private String lastLoginTime;
    private LocalDateTime createTime;
}
