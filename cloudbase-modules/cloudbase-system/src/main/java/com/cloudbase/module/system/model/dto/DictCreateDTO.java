package com.cloudbase.module.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 字典创建DTO
 */
@Data
public class DictCreateDTO {

    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    @NotBlank(message = "字典标签不能为空")
    private String dictLabel;

    @NotBlank(message = "字典值不能为空")
    private String dictValue;

    private Integer sort;
    private Integer status;
    private String remark;
}
