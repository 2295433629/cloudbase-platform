package com.cloudbase.module.system.model.dto;

import lombok.Data;

/**
 * 更新个人信息DTO
 */
@Data
public class UpdateProfileDTO {

    private String realName;
    private String phone;
    private String email;
    private String avatar;
}
