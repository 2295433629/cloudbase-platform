package com.cloudbase.common.web.auth;

import java.util.Map;

/**
 * 当前登录用户上下文（ThreadLocal实现，参考EAP JwtTokenOperator request attribute模式）
 */
public class UserContext {

    private static final ThreadLocal<Map<String, Object>> USER_HOLDER = new ThreadLocal<>();

    public static void set(Map<String, Object> user) {
        USER_HOLDER.set(user);
    }

    public static Map<String, Object> get() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        Map<String, Object> user = get();
        if (user == null || user.get("userId") == null) return null;
        return Long.parseLong(user.get("userId").toString());
    }

    public static String getUsername() {
        Map<String, Object> user = get();
        if (user == null) return null;
        return (String) user.get("username");
    }

    public static void remove() {
        USER_HOLDER.remove();
    }
}
