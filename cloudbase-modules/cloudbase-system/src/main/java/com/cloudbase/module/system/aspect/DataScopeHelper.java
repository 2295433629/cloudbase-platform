package com.cloudbase.module.system.aspect;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;

/**
 * 数据权限辅助类 - ThreadLocal 桥接切面与 Service 方法
 *
 * 用法：
 * 1. DataScopeAspect 在 @Before 中调用 setCondition(sql)
 * 2. Service 方法构建 wrapper 后调用 DataScopeHelper.applyTo(wrapper)
 */
public class DataScopeHelper {

    private static final ThreadLocal<String> CONDITION_HOLDER = new ThreadLocal<>();

    /**
     * 设置数据权限 SQL 条件（由切面调用）
     */
    public static void setCondition(String sql) {
        CONDITION_HOLDER.set(sql);
    }

    /**
     * 将数据权限条件应用到 wrapper（由 Service 方法调用）
     * 调用后自动清理 ThreadLocal
     */
    public static void applyTo(AbstractWrapper<?, ?, ?> wrapper) {
        String sql = CONDITION_HOLDER.get();
        if (sql != null && !sql.isEmpty()) {
            CONDITION_HOLDER.remove();
            wrapper.apply(sql);
        }
    }

    /**
     * 检查是否有待应用的数据权限条件
     */
    public static boolean hasCondition() {
        String sql = CONDITION_HOLDER.get();
        return sql != null && !sql.isEmpty();
    }

    /**
     * 清理 ThreadLocal（防止内存泄漏）
     */
    public static void clear() {
        CONDITION_HOLDER.remove();
    }
}
