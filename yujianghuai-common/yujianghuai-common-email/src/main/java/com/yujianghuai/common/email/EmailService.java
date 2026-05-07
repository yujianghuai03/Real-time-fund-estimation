package com.yujianghuai.common.email;

/**
 * 邮件发送服务。
 */
public interface EmailService {

    /**
     * 发送验证码邮件。
     *
     * @param targetEmail 收件人邮箱
     * @param code        验证码
     */
    void sendVerificationCode(String targetEmail, String code);
}
