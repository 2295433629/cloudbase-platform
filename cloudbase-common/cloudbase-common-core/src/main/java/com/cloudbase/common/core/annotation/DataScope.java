package com.cloudbase.common.core.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解 - 参考 RuoYi 设计
 * 在Service方法上添加此注解，自动根据用户角色拼接数据过滤SQL
 *
 * 用法: @DataScope(deptAlias = "d", userAlias = "u")
 * SQL中使用 ${params.dataScope} 占位符
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /** 部门表的别名 */
    String deptAlias() default "d";

    /** 用户表的别名 */
    String userAlias() default "u";

    /** 权限字符(用于多角色场景，不指定则取当前用户所有角色) */
    String permission() default "";
}
