package com.cloudbase.common.web.handler;

import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.exception.CommonExceptionEnum;
import com.cloudbase.common.core.exception.ServiceException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 统一将异常转换为：
 * 1. 正确的 HTTP 状态码（4xx/5xx）
 * 2. 带业务错误码的 AjaxResult 响应体
 * </p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常（ServiceException / BusinessException）
     * 使用异常自带的 httpStatus 设置响应状态，errorCode 写入响应体
     */
    @ExceptionHandler(ServiceException.class)
    public AjaxResult handleServiceException(ServiceException e, HttpServletResponse response) {
        response.setStatus(e.getHttpStatus());
        if (e.getHttpStatus() >= 500) {
            log.error("业务异常: code={}, msg={}", e.getErrorCode(), e.getMessage());
        } else {
            log.warn("业务异常: code={}, msg={}", e.getErrorCode(), e.getMessage());
        }
        if (e.getErrorCode() != null) {
            return AjaxResult.error(e.getErrorCode(), e.getErrorMsg());
        }
        return AjaxResult.error(e.getErrorMsg());
    }

    /**
     * 参数校验异常（@Valid @RequestBody DTO）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public AjaxResult handleValidationException(MethodArgumentNotValidException e,
                                                 HttpServletResponse response) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return AjaxResult.error(CommonExceptionEnum.PARAM_ERROR.getErrorCode(), msg);
    }

    /**
     * 方法级参数校验异常（@Validated on class / method params）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public AjaxResult handleConstraintViolationException(ConstraintViolationException e,
                                                          HttpServletResponse response) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        log.warn("参数校验失败: {}", msg);
        return AjaxResult.error(CommonExceptionEnum.PARAM_ERROR.getErrorCode(), msg);
    }

    /**
     * 运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public AjaxResult handleRuntimeException(RuntimeException e, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        log.error("运行时异常: {}", e.getMessage(), e);
        return AjaxResult.error(CommonExceptionEnum.SYSTEM_ERROR.getErrorCode(),
                "系统内部错误，请联系管理员");
    }

    /**
     * 兜底异常处理
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        log.error("未知异常: {}", e.getMessage(), e);
        return AjaxResult.error(CommonExceptionEnum.SYSTEM_ERROR.getErrorCode(),
                "系统异常，请稍后重试");
    }
}
