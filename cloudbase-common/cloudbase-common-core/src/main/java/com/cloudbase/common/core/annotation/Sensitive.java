package com.cloudbase.common.core.annotation;

import java.lang.annotation.*;

/**
 * 敏感信息脱敏注解
 *
 * @author ruoyi
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Sensitive {
    /** 脱敏策略 */
    SensitiveStrategy strategy();

    enum SensitiveStrategy {
        /** 手机号 */
        PHONE(s -> s.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2")),
        /** 身份证 */
        ID_CARD(s -> s.replaceAll("(\\d{4})\\d{10}(\\w{4})", "$1****$2")),
        /** 邮箱 */
        EMAIL(s -> s.replaceAll("(^\\w)[^@]*(@.*$)", "$1****$2")),
        /** 银行卡 */
        BANK_CARD(s -> s.replaceAll("(\\d{4})\\d{8,12}(\\w{4})", "$1****$2"));

        private final java.util.function.UnaryOperator<String> mask;

        SensitiveStrategy(java.util.function.UnaryOperator<String> mask) { this.mask = mask; }

        public String mask(String s) { return s == null ? null : mask.apply(s); }
    }
}
