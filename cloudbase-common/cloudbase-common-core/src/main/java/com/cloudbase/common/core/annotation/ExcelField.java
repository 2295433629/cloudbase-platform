package com.cloudbase.common.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel 字段注解
 * <p>
 * 配合 {@code EasyExcelUtil} 使用，标注在 DTO/VO 字段上，
 * 自动映射 Excel 列名与 Java 字段。
 * </p>
 *
 * <pre>
 * public class UserExportVO {
 *     &#064;ExcelField(name = "用户名", sort = 1)
 *     private String username;
 *
 *     &#064;ExcelField(name = "手机号", sort = 2)
 *     private String phone;
 * }
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelField {

    /** Excel 列名 */
    String name();

    /** 列排序（越小越靠前） */
    int sort() default 0;

    /** 列宽（字符数），0 表示自适应 */
    int width() default 0;

    /** 日期格式（仅 Date/LocalDateTime 类型字段有效） */
    String dateFormat() default "yyyy-MM-dd HH:mm:ss";

    /** 字典类型（如 gender: 0=男,1=女），格式 "0=男,1=女" */
    String dictType() default "";

    /** 提示信息（用于导入时的列头说明） */
    String prompt() default "";
}
