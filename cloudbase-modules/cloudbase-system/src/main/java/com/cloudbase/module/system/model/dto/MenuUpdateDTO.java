package com.cloudbase.module.system.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单更新DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MenuUpdateDTO extends MenuCreateDTO {

    @NotNull(message = "菜单ID不能为空")
    private Long menuId;
}
