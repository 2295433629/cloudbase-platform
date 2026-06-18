package com.cloudbase.module.system.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户更新DTO
 */
@Data
public class UserUpdateDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private String password;
    private String realName;
    private String phone;
    private String email;
    private Long deptId;
    private Long postId;
    private Integer status;
}
