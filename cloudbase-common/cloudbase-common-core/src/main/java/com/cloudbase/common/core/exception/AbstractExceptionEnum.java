package com.cloudbase.common.core.exception;

/**
 * 异常枚举接口
 */
public interface AbstractExceptionEnum {

    /** 错误码 */
    String getErrorCode();

    /** 错误消息 */
    String getErrorMsg();
}
