package com.cloudbase.module.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 角色创建DTO
 */
@Data
public class RoleCreateDTO {

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    @NotNull(message = "排序不能为空")
    private Integer sort;

    private Integer dataScope;
    private Integer status;
    private String remark;
}
