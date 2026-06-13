package com.cloudbase.common.core.annotation;

import java.lang.annotation.*;

/**
 * 重复提交限制注解
 *
 * @author ruoyi
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {
    /** 间隔时间(ms)，小于此时间视为重复提交 */
    int interval() default 5000;

    /** 提示消息 */
    String message() default "不允许重复提交，请稍候再试";
}
