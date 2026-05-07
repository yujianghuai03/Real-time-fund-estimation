package com.yujianghuai.common.web;

import com.yujianghuai.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;

@Schema(description = "统一响应结果")
public class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "响应状态码")
    private final Integer code;
    @Schema(description = "响应消息")
    private final String message;
    @Schema(description = "响应数据")
    private final T data;

    private R(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return new R<>(200, "success", null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(200, "success", data);
    }

    public static <T> R<T> fail(String message) {
        return new R<>(500, message, null);
    }

    public static <T> R<T> fail(Integer code, String message) {
        return new R<>(code, message, null);
    }

    public static <T> R<T> error(String message) {
        return fail(message);
    }

    public static <T> R<T> error(Integer code, String message) {
        return fail(code, message);
    }

    public static <T> R<T> error(ErrorCode errorCode, String message) {
        return fail(errorCode.getCode(), message);
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
