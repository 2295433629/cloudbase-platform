package com.cloudbase.module.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 通知公告创建DTO
 */
@Data
public class NoticeCreateDTO {

    @NotBlank(message = "公告标题不能为空")
    private String noticeTitle;

    private Integer noticeType;
    private String noticeContent;
    private Integer status;
}
