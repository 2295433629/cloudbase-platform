package com.cloudbase.common.web.cache;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 * <p>支持Redis和内存两种实现，通过YML配置切换</p>
 */
public interface CacheService {

    /**
     * 设置缓存（无过期时间）
     */
    void set(String key, String value);

    /**
     * 设置缓存并指定过期时间
     */
    void set(String key, String value, long timeout, TimeUnit unit);

    /**
     * 获取缓存值
     */
    String get(String key);

    /**
     * 删除缓存
     */
    boolean delete(String key);

    /**
     * 根据前缀模式匹配所有key（仅用于管理功能，生产慎用）
     *
     * @param pattern 模式，如 "login_tokens:*"
     */
    Set<String> keys(String pattern);

    /**
     * 获取key的剩余过期时间（秒），-1表示永不过期，-2表示key不存在
     */
    Long getExpire(String key);

    /**
     * 原子自增并设置过期时间（用于限流计数器）
     * <p>
     * 若 key 不存在，先初始化为 0 再自增，返回 1。
     * 若 key 已存在，直接自增并返回新值。
     * 过期时间仅在 key 首次创建时设置（后续自增不重置 TTL）。
     * </p>
     *
     * @param key     缓存键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 自增后的值
     */
    Long increment(String key, long timeout, TimeUnit unit);
}
