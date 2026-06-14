package com.cloudbase.module.system.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 分配角色DTO
 */
@Data
public class AssignRolesDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private List<Long> roleIds;
}
