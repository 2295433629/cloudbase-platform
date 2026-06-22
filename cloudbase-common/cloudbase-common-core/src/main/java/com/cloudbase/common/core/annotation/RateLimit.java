package com.cloudbase.common.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解
 * <p>
 * 基于滑动窗口计数器算法，支持按 IP、用户或全局维度限流。
 * 切面 {@code RateLimitAspect} 在 common-web 模块中实现。
 * </p>
 *
 * <pre>
 * // 每个IP每分钟最多10次请求
 * &#064;RateLimit(count = 10, time = 60, limitType = RateLimitType.IP)
 *
 * // 每个登录用户每30秒最多5次操作
 * &#064;RateLimit(count = 5, time = 30, limitType = RateLimitType.USER)
 *
 * // 全局每秒最多100次请求
 * &#064;RateLimit(count = 100, time = 1)
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 时间窗口内允许的最大请求数
     */
    int count() default 10;

    /**
     * 时间窗口大小（秒）
     */
    int time() default 60;

    /**
     * 限流维度类型，默认按 IP
     */
    LimitType limitType() default LimitType.IP;

    /**
     * 限流提示消息
     */
    String message() default "访问过于频繁，请稍后再试";

    /**
     * 限流维度枚举
     */
    enum LimitType {
        /** 按客户端 IP 限流 */
        IP,
        /** 按当前登录用户限流（未登录退化为 IP） */
        USER,
        /** 全局限流（所有请求共享计数器） */
        GLOBAL
    }
}
