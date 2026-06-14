package com.cloudbase.module.system.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 参数配置更新DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConfigUpdateDTO extends ConfigCreateDTO {

    @NotNull(message = "参数ID不能为空")
    private Long configId;
}
