package com.cloudbase.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举
 */
@Getter
@AllArgsConstructor
public enum StatusEnum {

    ENABLE(1, "启用"),
    DISABLE(0, "禁用");

    private final Integer code;
    private final String desc;
}
