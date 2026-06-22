package com.cloudbase.common.web.aspect;

import com.cloudbase.common.core.annotation.RateLimit;
import com.cloudbase.common.core.exception.CommonExceptionEnum;
import com.cloudbase.common.core.exception.ServiceException;
import com.cloudbase.common.web.auth.UserContext;
import com.cloudbase.common.web.cache.CacheService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流切面
 * <p>
 * 基于 {@link RateLimit} 注解，通过缓存原子计数器实现固定窗口限流。
 * Redis 模式下使用 Redis INCR 原子操作，Memory 模式下使用 ConcurrentHashMap。
 * </p>
 * <p>
 * 限流键格式：{@code rate_limit:{limitType}:{identifier}:{className}.{methodName}}
 * </p>
 */
@Slf4j
@Aspect
@Order(1)
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";

    private final CacheService cacheService;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = buildKey(joinPoint, rateLimit);
        Long count = cacheService.increment(key, rateLimit.time(), TimeUnit.SECONDS);

        if (count != null && count > rateLimit.count()) {
            log.warn("接口限流触发: key={}, count={}, limit={}, method={}.{}",
                    key, count, rateLimit.count(),
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName());
            throw new ServiceException(
                    CommonExceptionEnum.RATE_LIMIT_EXCEEDED.getErrorCode(),
                    rateLimit.message(),
                    CommonExceptionEnum.RATE_LIMIT_EXCEEDED.getHttpStatus()
            );
        }
        return joinPoint.proceed();
    }

    /**
     * 构建限流缓存键
     */
    private String buildKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodKey = signature.getDeclaringTypeName() + "." + method.getName();

        String identifier;
        switch (rateLimit.limitType()) {
            case USER:
                Long userId = UserContext.getUserId();
                // 未登录时退化为 IP
                identifier = userId != null ? "user:" + userId : "ip:" + getClientIp();
                break;
            case GLOBAL:
                identifier = "global";
                break;
            case IP:
            default:
                identifier = "ip:" + getClientIp();
                break;
        }

        return RATE_LIMIT_KEY_PREFIX + rateLimit.limitType().name() + ":" + identifier + ":" + methodKey;
    }

    /**
     * 获取客户端真实 IP（支持 Nginx 反向代理）
     */
    private String getClientIp() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "unknown";
        }
        HttpServletRequest request = attrs.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个（最接近客户端的 IP）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}
