package com.cloudbase.module.system.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知公告更新DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeUpdateDTO extends NoticeCreateDTO {

    @NotNull(message = "公告ID不能为空")
    private Long noticeId;
}
