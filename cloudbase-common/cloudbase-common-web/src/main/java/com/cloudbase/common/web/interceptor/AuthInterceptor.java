package com.cloudbase.common.web.interceptor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.exception.CommonExceptionEnum;
import com.cloudbase.common.web.annotation.ApiResource;
import com.cloudbase.common.web.auth.JwtProperties;
import com.cloudbase.common.web.auth.JwtTokenUtil;
import com.cloudbase.common.web.auth.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.util.Map;

/**
 * JWT认证拦截器
 * <p>
 * 对所有 HandlerMethod 接口强制校验 JWT Token。
 * 若方法标注了 @ApiResource(requiredToken=false) 则放行。
 * 排除路径由 WebMvcConfig.excludePathPatterns 控制。
 * </p>
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;

    public AuthInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 非Controller方法（如静态资源）直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 检查@ApiResource注解，如果明确标注requiredToken=false则放行
        ApiResource apiResource = handlerMethod.getMethodAnnotation(ApiResource.class);
        if (apiResource != null && !apiResource.requiredToken()) {
            return true;
        }

        // 所有接口默认需要Token
        String token = extractToken(request);
        if (StrUtil.isBlank(token)) {
            writeError(response, CommonExceptionEnum.NOT_LOGIN);
            return false;
        }

        if (!JwtTokenUtil.validateToken(token, jwtProperties.getSecret())) {
            writeError(response, CommonExceptionEnum.TOKEN_INVALID);
            return false;
        }

        if (JwtTokenUtil.isTokenExpired(token, jwtProperties.getSecret())) {
            writeError(response, CommonExceptionEnum.TOKEN_EXPIRED);
            return false;
        }

        // 设置用户上下文
        Claims claims = JwtTokenUtil.parseToken(token, jwtProperties.getSecret());
        UserContext.set(Map.of(
                "userId", claims.get("userId", Long.class),
                "username", claims.get("username", String.class)
        ));
        return true;
    }

    /**
     * 请求完成后清理 ThreadLocal，防止内存泄漏和用户数据串扰
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.remove();
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(jwtProperties.getHeader());
        if (StrUtil.isNotBlank(header) && header.startsWith(jwtProperties.getTokenPrefix())) {
            return header.substring(jwtProperties.getTokenPrefix().length());
        }
        return null;
    }

    private void writeError(HttpServletResponse response, CommonExceptionEnum exceptionEnum) {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        try (PrintWriter writer = response.getWriter()) {
            writer.write(JSON.toJSONString(
                    AjaxResult.error(exceptionEnum.getErrorCode(), exceptionEnum.getErrorMsg())
            ));
        } catch (Exception ignored) {}
    }
}
