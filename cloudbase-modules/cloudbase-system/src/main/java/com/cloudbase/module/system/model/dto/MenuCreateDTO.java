package com.cloudbase.module.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 菜单创建DTO
 */
@Data
public class MenuCreateDTO {

    private Long parentId;

    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    @NotNull(message = "菜单类型不能为空")
    private Integer menuType;

    private String path;
    private String component;
    private String perms;
    private String icon;
    private Integer sort;
    private Integer hidden;
    private Integer status;
}
