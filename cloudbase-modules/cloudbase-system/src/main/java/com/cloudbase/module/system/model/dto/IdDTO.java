package com.cloudbase.module.system.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通用ID参数DTO
 */
@Data
public class IdDTO {

    @NotNull(message = "ID不能为空")
    private Long id;
}
