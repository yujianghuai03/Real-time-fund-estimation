package com.yujianghuai.common.feign;

import com.yujianghuai.common.exception.BizException;
import com.yujianghuai.common.web.R;

public final class FeignExecutor {

    private FeignExecutor() {
    }

    public static <T> T getData(R<T> result) {
        if (result == null) {
            throw new BizException("remote result is null");
        }
        if (result.getCode() == null || result.getCode() != 200) {
            throw new BizException(result.getCode() == null ? 500 : result.getCode(), result.getMessage());
        }
        return result.getData();
    }
}
