package com.cloudbase.common.core.exception;

/**
 * 服务异常基类
 * <p>
 * 所有业务异常的基类，携带错误码、错误消息和对应的 HTTP 状态码。
 * 错误码遵循阿里巴巴 A/B/C 分类规范。
 * </p>
 */
public class ServiceException extends RuntimeException {

    private final String errorCode;
    private final String errorMsg;
    /** 对应的 HTTP 状态码，由 GlobalExceptionHandler 用于设置响应状态 */
    private final int httpStatus;

    public ServiceException(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, 400);
    }

    public ServiceException(String errorCode, String errorMsg, int httpStatus) {
        super(errorMsg);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.httpStatus = httpStatus;
    }

    public ServiceException(AbstractExceptionEnum exceptionEnum) {
        super(exceptionEnum.getErrorMsg());
        this.errorCode = exceptionEnum.getErrorCode();
        this.errorMsg = exceptionEnum.getErrorMsg();
        this.httpStatus = exceptionEnum.getHttpStatus();
    }

    public ServiceException(String errorCode, String errorMsg, int httpStatus, Throwable cause) {
        super(errorMsg, cause);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() { return errorCode; }
    public String getErrorMsg() { return errorMsg; }
    public int getHttpStatus() { return httpStatus; }
}
