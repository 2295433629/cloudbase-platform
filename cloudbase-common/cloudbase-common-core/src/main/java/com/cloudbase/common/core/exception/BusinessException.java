package com.cloudbase.common.core.exception;

import com.cloudbase.common.core.constant.CommonConstants;

/**
 * 业务异常
 */
public class BusinessException extends ServiceException {

    public BusinessException(String errorMsg) {
        super(CommonConstants.ERROR_CODE, errorMsg);
    }

    public BusinessException(String errorCode, String errorMsg) {
        super(errorCode, errorMsg);
    }

    public BusinessException(AbstractExceptionEnum exceptionEnum) {
        super(exceptionEnum);
    }
}
