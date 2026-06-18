package com.cloudbase.module.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 岗位创建DTO
 */
@Data
public class PostCreateDTO {

    @NotBlank(message = "岗位编码不能为空")
    private String postCode;

    @NotBlank(message = "岗位名称不能为空")
    private String postName;

    private Integer sort;
    private Integer status;
    private String remark;
}
