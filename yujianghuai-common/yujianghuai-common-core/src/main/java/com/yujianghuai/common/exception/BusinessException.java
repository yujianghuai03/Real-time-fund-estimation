package com.yujianghuai.common.exception;

/**
 * 业务异常，由全局异常处理器统一转换为接口响应。
 */
public class BusinessException extends BizException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.getMessage());
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode.getCode(), message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
