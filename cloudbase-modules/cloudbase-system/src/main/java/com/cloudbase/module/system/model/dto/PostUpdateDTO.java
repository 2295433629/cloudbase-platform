package com.cloudbase.module.system.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 岗位更新DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostUpdateDTO extends PostCreateDTO {

    @NotNull(message = "岗位ID不能为空")
    private Long postId;
}
