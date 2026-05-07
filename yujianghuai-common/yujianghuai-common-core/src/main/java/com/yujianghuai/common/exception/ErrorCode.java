package com.yujianghuai.common.exception;

/**
 * 统一错误码。
 */
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "业务冲突"),
    SYSTEM_ERROR(500, "系统内部错误"),
    REMOTE_SERVICE_ERROR(502, "远程服务异常"),
    FUND_NOT_ENOUGH(10001, "基金份额不足"),
    FUND_DUPLICATE(10002, "基金已存在");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
