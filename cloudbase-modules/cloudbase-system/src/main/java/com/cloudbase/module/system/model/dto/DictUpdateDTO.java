package com.cloudbase.module.system.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典更新DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DictUpdateDTO extends DictCreateDTO {

    @NotNull(message = "字典ID不能为空")
    private Long dictId;
}
