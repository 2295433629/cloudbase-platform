package com.cloudbase.common.web.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存实现
 */
@Slf4j
@RequiredArgsConstructor
public class RedisCacheService implements CacheService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void set(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public boolean delete(String key) {
        Boolean result = redisTemplate.delete(key);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    @Override
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    @Override
    public Long increment(String key, long timeout, TimeUnit unit) {
        Long count = redisTemplate.opsForValue().increment(key);
        // 首次创建时设置过期时间，后续自增不重置 TTL
        if (count != null && count == 1L) {
            redisTemplate.expire(key, timeout, unit);
        }
        return count != null ? count : 0L;
    }
}
