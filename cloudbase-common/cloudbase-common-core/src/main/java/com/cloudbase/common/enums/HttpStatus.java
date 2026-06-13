package com.cloudbase.common.enums;

/**
 * 返回状态码
 *
 * @author ruoyi
 */
public enum HttpStatus {
    /** 操作成功 */
    SUCCESS(200),     // 别改这个值，前端拦截器靠这个值判断
    /** 参数列表错误 */
    BAD_REQUEST(400),
    /** 未授权 */
    UNAUTHORIZED(401),
    /** 访问受限 */
    FORBIDDEN(403),
    /** 资源不存在 */
    NOT_FOUND(404),
    /** 方法不允许 */
    METHOD_NOT_ALLOWED(405),
    /** 系统内部错误 */
    ERROR(500),
    /** 业务警告 */
    WARN(601);

    private final int code;

    HttpStatus(int code) { this.code = code; }

    public int getCode() { return code; }

    public int value() { return code; }

    /** code 对应哪个 HttpStatus（没配到的默认 BAD_REQUEST） */
    public static HttpStatus fromCode(int code) {
        for (HttpStatus s : values()) {
            if (s.code == code) return s;
        }
        return BAD_REQUEST;
    }
}
