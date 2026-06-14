package com.cloudbase.module.system.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门更新DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeptUpdateDTO extends DeptCreateDTO {

    @NotNull(message = "部门ID不能为空")
    private Long deptId;
}
