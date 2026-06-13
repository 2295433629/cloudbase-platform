package com.cloudbase.common.web.interceptor;

import com.cloudbase.common.web.annotation.ApiResource;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 安全拦截器基类（参考EAP BaseSecurityInterceptor）
 */
public abstract class BaseSecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            ApiResource apiResource = handlerMethod.getMethodAnnotation(ApiResource.class);
            if (apiResource != null) {
                return doFilter(request, response, apiResource);
            }
        }
        return true;
    }

    /**
     * 具体过滤逻辑，子类实现
     * @return true-放行，false-拦截
     */
    protected abstract boolean doFilter(HttpServletRequest request, HttpServletResponse response, ApiResource apiResource);
}
