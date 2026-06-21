package com.cloudbase.common.core.exception;

/**
 * 异常枚举接口
 * <p>
 * 错误码遵循阿里巴巴规范：A=用户错误，B=系统业务错误，C=第三方服务错误。
 * 每个错误码对应一个 HTTP 状态码，用于 GlobalExceptionHandler 设置响应状态。
 * </p>
 */
public interface AbstractExceptionEnum {

    /** 错误码（如 A0200, B0001） */
    String getErrorCode();

    /** 错误消息 */
    String getErrorMsg();

    /** 对应的 HTTP 状态码，默认 400 */
    default int getHttpStatus() { return 400; }
}
