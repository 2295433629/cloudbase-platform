package com.cloudbase.module.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 参数配置创建DTO
 */
@Data
public class ConfigCreateDTO {

    @NotBlank(message = "参数名称不能为空")
    private String configName;

    @NotBlank(message = "参数键名不能为空")
    private String configKey;

    @NotBlank(message = "参数键值不能为空")
    private String configValue;

    private Integer configType;
    private String remark;
}
