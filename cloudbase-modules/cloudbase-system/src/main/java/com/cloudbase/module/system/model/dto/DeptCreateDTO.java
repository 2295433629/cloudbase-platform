package com.cloudbase.module.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 部门创建DTO
 */
@Data
public class DeptCreateDTO {

    private Long parentId;

    @NotBlank(message = "部门名称不能为空")
    private String deptName;

    private Integer sort;
    private String leader;
    private String phone;
    private String email;
    private Integer status;
}
