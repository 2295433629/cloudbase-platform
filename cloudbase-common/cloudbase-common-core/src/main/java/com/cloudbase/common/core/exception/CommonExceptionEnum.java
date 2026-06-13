package com.cloudbase.common.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用业务异常枚举
 */
@Getter
@AllArgsConstructor
public enum CommonExceptionEnum implements AbstractExceptionEnum {

    /** 系统异常 */
    SYSTEM_ERROR("99999", "系统异常，请稍后重试"),
    /** 参数错误 */
    PARAM_ERROR("A0001", "参数校验失败"),
    /** 未登录 */
    NOT_LOGIN("A0200", "请先登录"),
    /** Token过期 */
    TOKEN_EXPIRED("A0201", "登录已过期，请重新登录"),
    /** Token无效 */
    TOKEN_INVALID("A0202", "Token无效"),
    /** 无权限 */
    NO_PERMISSION("A0300", "无操作权限"),
    /** 账号被禁用 */
    ACCOUNT_DISABLED("A0301", "账号已被禁用"),
    /** 验证码错误 */
    CAPTCHA_ERROR("A0400", "验证码错误"),
    /** 数据不存在 */
    DATA_NOT_FOUND("B0001", "数据不存在"),
    /** 数据已存在 */
    DATA_EXISTS("B0002", "数据已存在"),
    /** 操作不允许 */
    OPERATION_NOT_ALLOWED("B0003", "操作不允许"),
    ;

    private final String errorCode;
    private final String errorMsg;
}
