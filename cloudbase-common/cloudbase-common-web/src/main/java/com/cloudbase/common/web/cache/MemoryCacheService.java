package com.cloudbase.common.web.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 内存缓存实现
 * <p>
 * 基于ConcurrentHashMap实现，支持过期时间。
 * 适用于无Redis环境的单机部署场景。
 * 注意：服务重启后缓存丢失，不适合分布式多实例部署。
 * </p>
 */
@Slf4j
public class MemoryCacheService implements CacheService {

    private final ConcurrentHashMap<String, CacheEntry> store = new ConcurrentHashMap<>();

    /** 过期键清理线程，每30秒执行一次 */
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "memory-cache-cleaner");
        t.setDaemon(true);
        return t;
    });

    public MemoryCacheService() {
        cleaner.scheduleAtFixedRate(this::evictExpired, 30, 30, TimeUnit.SECONDS);
        log.info("MemoryCacheService 已初始化（内存缓存模式）");
    }

    @Override
    public void set(String key, String value) {
        store.put(key, new CacheEntry(value, -1L));
    }

    @Override
    public void set(String key, String value, long timeout, TimeUnit unit) {
        long expireAt = System.currentTimeMillis() + unit.toMillis(timeout);
        store.put(key, new CacheEntry(value, expireAt));
    }

    @Override
    public String get(String key) {
        CacheEntry entry = store.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            store.remove(key);
            return null;
        }
        return entry.value;
    }

    @Override
    public boolean delete(String key) {
        return store.remove(key) != null;
    }

    @Override
    public Set<String> keys(String pattern) {
        // 先转义正则特殊字符，再将 Redis 通配符 * ? 转为正则
        String escaped = pattern
                .replace("\\", "\\\\")
                .replace(".", "\\.")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("+", "\\+")
                .replace("^", "\\^")
                .replace("$", "\\$")
                .replace("|", "\\|")
                .replace("*", ".*")
                .replace("?", ".");
        String regex = "^" + escaped + "$";
        evictExpired();
        return store.keySet().stream()
                .filter(k -> k.matches(regex))
                .collect(Collectors.toSet());
    }

    @Override
    public Long getExpire(String key) {
        CacheEntry entry = store.get(key);
        if (entry == null) {
            return -2L; // key不存在
        }
        if (entry.isExpired()) {
            store.remove(key);
            return -2L;
        }
        if (entry.expireAt < 0) {
            return -1L; // 永不过期
        }
        long remaining = (entry.expireAt - System.currentTimeMillis()) / 1000;
        return Math.max(remaining, 0L);
    }

    /** 清理已过期的键 */
    private void evictExpired() {
        store.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    /** 缓存条目 */
    private static class CacheEntry {
        final String value;
        /** 过期时间戳（毫秒），-1表示永不过期 */
        final long expireAt;

        CacheEntry(String value, long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }

        boolean isExpired() {
            return expireAt > 0 && System.currentTimeMillis() > expireAt;
        }
    }
}
