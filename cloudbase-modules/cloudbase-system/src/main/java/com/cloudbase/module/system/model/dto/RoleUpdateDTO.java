package com.cloudbase.module.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 角色更新DTO（独立定义，不继承 RoleCreateDTO，以支持部分更新）
 */
@Data
public class RoleUpdateDTO {

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    private Integer sort;
    private Integer dataScope;
    private Integer status;
    private String remark;
}
