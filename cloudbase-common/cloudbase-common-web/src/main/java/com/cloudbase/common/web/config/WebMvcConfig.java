package com.cloudbase.common.web.config;

import com.cloudbase.common.web.handler.ResponseBodyWrapper;
import com.cloudbase.common.web.interceptor.AuthInterceptor;
import com.cloudbase.common.web.resolver.UserArgumentResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web Mvc配置
 */
@Configuration
@Import(ResponseBodyWrapper.class)
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    /** CORS 允许的源，多个用逗号分隔，生产部署时通过 yml 配置 cloudbase.cors.allowed-origins */
    @Value("${cloudbase.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOrigins;

    public WebMvcConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Auth拦截器 - 根据@ApiResource注解的requiredToken属性决定是否鉴权
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/logout",
                        "/auth/captcha",
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/druid/**",
                        "/files/**",
                        "/error"
                );
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new UserArgumentResolver());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
