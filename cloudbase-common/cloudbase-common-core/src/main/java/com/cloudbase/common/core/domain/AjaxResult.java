package com.cloudbase.common.core.domain;

import com.cloudbase.common.core.constant.CommonConstants;

import java.util.HashMap;

/**
 * 统一响应结构
 * <p>
 * code: String 业务码（"00000" = 成功，其他为错误码如 A0200, B0001）
 * msg:  人类可读的消息
 * data: 业务数据（可选）
 * <p>
 * HTTP 状态码由 GlobalExceptionHandler 或调用方通过 HttpServletResponse 设置，
 * AjaxResult 本身只承载业务层响应体。
 */
public class AjaxResult extends HashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    public static final String CODE_TAG = "code";
    public static final String MSG_TAG = "msg";
    public static final String DATA_TAG = "data";

    /**
     * 主构造方法
     *
     * @param code 业务码（"00000" = 成功）
     * @param msg  返回消息
     * @param data 数据对象
     */
    public AjaxResult(String code, String msg, Object data) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
        if (data != null) {
            super.put(DATA_TAG, data);
        }
    }

    // ======================== 成功 ========================

    public static AjaxResult success() {
        return success("操作成功");
    }

    public static AjaxResult success(Object data) {
        return success("操作成功", data);
    }

    public static AjaxResult success(String msg) {
        return success(msg, null);
    }

    public static AjaxResult success(String msg, Object data) {
        return new AjaxResult(CommonConstants.SUCCESS_CODE, msg, data);
    }

    // ======================== 错误 ========================

    /**
     * 默认错误（无错误码，HTTP 400）
     */
    public static AjaxResult error() {
        return error("操作失败");
    }

    /**
     * 简单错误消息（无错误码，HTTP 400）
     */
    public static AjaxResult error(String msg) {
        return new AjaxResult(null, msg, null);
    }

    /**
     * 带业务错误码的错误响应
     *
     * @param code 业务错误码（如 A0200, B0001）
     * @param msg  错误消息
     */
    public static AjaxResult error(String code, String msg) {
        return new AjaxResult(code, msg, null);
    }

    /**
     * 兼容旧版 int code 的错误响应（已废弃，请使用 String code 版本）
     */
    @Deprecated
    public static AjaxResult error(int code, String msg) {
        return new AjaxResult(String.valueOf(code), msg, null);
    }

    // ======================== 警告 ========================

    public static AjaxResult warn(String msg) {
        return warn(msg, null);
    }

    public static AjaxResult warn(String msg, Object data) {
        return new AjaxResult("WARN", msg, data);
    }

    // ======================== 链式调用 ========================

    @Override
    public AjaxResult put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
