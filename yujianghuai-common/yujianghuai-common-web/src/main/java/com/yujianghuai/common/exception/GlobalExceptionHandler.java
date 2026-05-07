package com.yujianghuai.common.exception;

import com.yujianghuai.common.web.R;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public R<Void> handleBizException(BizException exception) {
        return R.error(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class
    })
    public R<Void> handleValidationException(Exception exception) {
        return R.error(400, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        return R.error("system error");
    }
}
