package com.cloudbase.common.web.cache;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 缓存配置属性
 * <p>
 * cloudbase.cache.type 可选值：
 * <ul>
 *     <li>redis  - 使用Redis作为缓存（默认）</li>
 *     <li>memory - 使用内存作为缓存（单机/无Redis环境适用）</li>
 * </ul>
 */
@Data
@ConfigurationProperties(prefix = "cloudbase.cache")
public class CacheProperties {

    /**
     * 缓存类型：redis（默认）或 memory
     */
    private String type = "redis";
}
