package com.cloudbase.common.core.domain;

import com.cloudbase.common.core.constant.CommonConstants;

import java.util.Map;

/**
 * 分页参数工具类
 * <p>
 * 统一从请求参数 Map 中提取 pageNo / pageSize，避免每个 Controller 重复解析逻辑。
 * 各接口可指定自定义默认分页大小。
 * </p>
 */
public final class PageQuery {

    private PageQuery() {}

    /** 最大允许的分页大小 */
    private static final int MAX_PAGE_SIZE = 200;

    /**
     * 从请求参数中提取分页信息（使用全局默认 pageSize = 20）
     */
    public static PageInfo of(Map<String, Object> params) {
        return of(params, CommonConstants.DEFAULT_PAGE_SIZE);
    }

    /**
     * 从请求参数中提取分页信息，可指定默认 pageSize
     *
     * @param params         请求参数 Map
     * @param defaultPageSize 该接口的默认分页大小
     */
    public static PageInfo of(Map<String, Object> params, int defaultPageSize) {
        int pageNo = parseInt(params.get("pageNo"), CommonConstants.DEFAULT_PAGE_NO);
        int pageSize = parseInt(params.get("pageSize"), defaultPageSize);
        pageNo = Math.max(pageNo, 1);
        pageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        return new PageInfo(pageNo, pageSize);
    }

    private static int parseInt(Object value, int defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 分页信息记录
     */
    public record PageInfo(int pageNo, int pageSize) {}
}
