package com.cloudbase.module.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户创建DTO
 */
@Data
public class UserCreateDTO {

    @NotBlank(message = "账号不能为空")
    private String account;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6位")
    private String password;

    private String realName;
    private String phone;
    private String email;
    private Long deptId;
    private Long postId;
    private Integer status;
}
