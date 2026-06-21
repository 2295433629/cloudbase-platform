package com.cloudbase.common.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用业务异常枚举
 * <p>
 * 错误码遵循阿里巴巴规范：
 * A = 用户错误（参数、认证、授权）
 * B = 系统业务错误（数据校验、业务规则）
 * C = 第三方服务错误（预留）
 * </p>
 */
@Getter
@AllArgsConstructor
public enum CommonExceptionEnum implements AbstractExceptionEnum {

    // ============ A: 用户错误 ============
    /** 参数错误 */
    PARAM_ERROR("A0001", "参数校验失败", 400),
    /** 未登录 */
    NOT_LOGIN("A0200", "请先登录", 401),
    /** Token过期 */
    TOKEN_EXPIRED("A0201", "登录已过期，请重新登录", 401),
    /** Token无效 */
    TOKEN_INVALID("A0202", "Token无效", 401),
    /** 无权限 */
    NO_PERMISSION("A0300", "无操作权限", 403),
    /** 账号被禁用 */
    ACCOUNT_DISABLED("A0301", "账号已被禁用", 403),
    /** 验证码错误 */
    CAPTCHA_ERROR("A0400", "验证码错误", 400),

    // ============ B: 系统业务错误 ============
    /** 数据不存在 */
    DATA_NOT_FOUND("B0001", "数据不存在", 404),
    /** 数据已存在 */
    DATA_EXISTS("B0002", "数据已存在", 400),
    /** 操作不允许 */
    OPERATION_NOT_ALLOWED("B0003", "操作不允许", 400),

    // ============ 系统级异常 ============
    /** 系统异常 */
    SYSTEM_ERROR("99999", "系统异常，请稍后重试", 500),
    ;

    private final String errorCode;
    private final String errorMsg;
    /** 对应的 HTTP 状态码 */
    private final int httpStatus;
}
