package com.cloudbase.common.web.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.*;

/**
 * API资源注解（参考EAP @ApiResource）
 * 替代@GetMapping/@PostMapping，统一控制鉴权和文档
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping
public @interface ApiResource {

    /** 资源编码（为空则自动生成：类名+方法名） */
    String code() default "";

    /** 资源名称 */
    String name() default "";

    /** 是否需要JWT鉴权 */
    boolean requiredToken() default true;

    /** 请求路径 */
    @AliasFor(annotation = RequestMapping.class)
    String[] value() default {};

    /** 请求路径 */
    @AliasFor(annotation = RequestMapping.class)
    String[] path() default {};

    /** 请求方法，默认POST */
    @AliasFor(annotation = RequestMapping.class)
    RequestMethod[] method() default {RequestMethod.POST};
}
