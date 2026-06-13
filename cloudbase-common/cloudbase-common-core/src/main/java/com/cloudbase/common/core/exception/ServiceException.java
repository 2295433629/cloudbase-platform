package com.cloudbase.common.core.exception;

/**
 * 服务异常（参考EAP ServiceException）
 */
public class ServiceException extends RuntimeException {

    private final String errorCode;
    private final String errorMsg;

    public ServiceException(String errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public ServiceException(AbstractExceptionEnum exceptionEnum) {
        super(exceptionEnum.getErrorMsg());
        this.errorCode = exceptionEnum.getErrorCode();
        this.errorMsg = exceptionEnum.getErrorMsg();
    }

    public ServiceException(String errorCode, String errorMsg, Throwable cause) {
        super(errorMsg, cause);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public String getErrorCode() { return errorCode; }
    public String getErrorMsg() { return errorMsg; }
}
