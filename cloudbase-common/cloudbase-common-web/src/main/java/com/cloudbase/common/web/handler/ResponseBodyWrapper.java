package com.cloudbase.common.web.handler;

import com.cloudbase.common.core.domain.AjaxResult;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 全局响应包装器 — 自动将Controller返回值包装为AjaxResult
 * 参考 RuoYi 设计
 *
 * @author ruoyi
 */
@ControllerAdvice
public class ResponseBodyWrapper implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        // AjaxResult / TableDataInfo 不重复包装
        if (body instanceof AjaxResult || body instanceof com.cloudbase.common.core.domain.TableDataInfo) {
            return body;
        }
        // String 类型特殊处理（JSON序列化问题）
        if (body instanceof String) {
            return body;
        }
        // 布尔类型
        if (body instanceof Boolean) {
            boolean result = (Boolean) body;
            return result ? AjaxResult.success() : AjaxResult.error();
        }
        return AjaxResult.success(body);
    }
}
