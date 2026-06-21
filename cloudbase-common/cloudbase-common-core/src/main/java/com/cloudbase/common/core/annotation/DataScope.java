package com.cloudbase.common.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限注解 - 参考 RuoYi 设计
 * 在Service方法上添加此注解，自动根据用户角色拼接数据过滤SQL
 *
 * 用法: @DataScope(deptAlias = "d", userAlias = "u")
 * 切面会将过滤条件注入到方法参数中的 QueryWrapper/LambdaQueryWrapper 对象
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /** 部门表的别名（单表查询时留空即可） */
    String deptAlias() default "";

    /** 用户表的别名（单表查询时留空即可） */
    String userAlias() default "";

    /** 部门祖先路径字段名（用于"本部门及以下"查找子部门，默认 ancestors） */
    String ancestors() default "ancestors";

    /** 权限字符(用于多角色场景，不指定则取当前用户所有角色) */
    String permission() default "";
}
