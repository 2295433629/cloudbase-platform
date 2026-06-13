package com.cloudbase.common.web.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解 - 参考 RuoYi 设计
 * 在Controller方法上添加此注解，自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {

    /** 模块标题 */
    String title() default "";

    /** 操作类型 INSERT/UPDATE/DELETE/QUERY/IMPORT/EXPORT/OTHER */
    String operType() default "OTHER";

    /** 是否保存请求参数 */
    boolean saveRequest() default true;

    /** 是否保存响应结果 */
    boolean saveResponse() default false;
}
