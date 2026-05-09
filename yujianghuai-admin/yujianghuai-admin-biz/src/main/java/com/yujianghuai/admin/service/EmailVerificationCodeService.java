package com.yujianghuai.admin.service;

import com.yujianghuai.admin.dto.EmailVerificationCodeRequest;
import com.yujianghuai.common.web.R;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author yxh
 * @date 2026/5/9 15:24
 */

public interface EmailVerificationCodeService {
    /**
     * 发送邮箱验证码
     *
     * @param request 邮箱地址
     * @return 是否发送成功
     */
    R<Boolean> sendVerificationCode(EmailVerificationCodeRequest request,
                           HttpServletRequest servletRequest);

}
