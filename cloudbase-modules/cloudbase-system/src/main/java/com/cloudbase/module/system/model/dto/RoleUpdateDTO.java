package com.cloudbase.module.system.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色更新DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleUpdateDTO extends RoleCreateDTO {

    @NotNull(message = "角色ID不能为空")
    private Long roleId;
}
