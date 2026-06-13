package com.cloudbase.common.web.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 缓存自动配置（工厂类）
 * <p>
 * 根据 cloudbase.cache.type 配置选择缓存实现：
 * <ul>
 *     <li>type=redis（默认）→ RedisCacheService</li>
 *     <li>type=memory       → MemoryCacheService</li>
 * </ul>
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfig {

    /**
     * Redis缓存实现（type=redis 或 未配置时生效）
     */
    @Bean
    @ConditionalOnProperty(name = "cloudbase.cache.type", havingValue = "redis", matchIfMissing = true)
    public CacheService redisCacheService(StringRedisTemplate redisTemplate) {
        log.info("缓存模式: Redis");
        return new RedisCacheService(redisTemplate);
    }

    /**
     * 内存缓存实现（type=memory 时生效）
     */
    @Bean
    @ConditionalOnProperty(name = "cloudbase.cache.type", havingValue = "memory")
    public CacheService memoryCacheService() {
        log.info("缓存模式: 内存（MemoryCacheService）");
        return new MemoryCacheService();
    }

    /**
     * 兜底：若以上条件均不满足，默认使用内存缓存
     */
    @Bean
    @ConditionalOnMissingBean(CacheService.class)
    public CacheService fallbackCacheService() {
        log.warn("未匹配到缓存配置，使用内存缓存作为兜底");
        return new MemoryCacheService();
    }
}
