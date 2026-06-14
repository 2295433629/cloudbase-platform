package com.cloudbase.common.web.config;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.HibernateValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/**
 * 参数校验配置
 * <p>
 * 启用 fail-fast 模式：检测到第一个校验失败即返回，避免无意义的后继校验。
 * </p>
 */
@Configuration
public class ValidationConfig {

    /**
     * 方法级参数校验（Controller 方法参数上 @Valid 生效）
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

    /**
     * 快速失败 Validator
     */
    @Bean
    public Validator validator() {
        try (ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .failFast(true)
                .buildValidatorFactory()) {
            return factory.getValidator();
        }
    }
}
