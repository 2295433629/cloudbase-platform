package com.cloudbase.common.web.config;

import com.cloudbase.common.web.storage.FileStorageService;
import com.cloudbase.common.web.storage.LocalFileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 文件存储配置
 * <p>
 * 默认使用本地磁盘存储，通过 yml 配置存储路径：
 * <pre>
 * cloudbase:
 *   storage:
 *     base-path: D:/uploads/cloudbase  # 本地存储根目录
 *     access-path: /files              # Web访问路径前缀
 * </pre>
 * </p>
 */
@Slf4j
@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    @Value("${cloudbase.storage.base-path:./uploads/cloudbase}")
    private String basePath;

    @Value("${cloudbase.storage.access-path:/files}")
    private String accessPath;

    @Bean
    @ConditionalOnMissingBean(FileStorageService.class)
    public FileStorageService localFileStorageService() {
        return new LocalFileStorageService(basePath, accessPath);
    }

    /**
     * 静态资源映射：让上传的文件可通过 /files/** 直接访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 basePath 目录映射为 Web 可访问的静态资源
        String normalizedBase = basePath.replace("\\", "/");
        if (!normalizedBase.endsWith("/")) {
            normalizedBase += "/";
        }
        registry.addResourceHandler(accessPath + "/**")
                .addResourceLocations("file:" + normalizedBase);
    }
}
