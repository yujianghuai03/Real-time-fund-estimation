package com.yujianghuai.common.constant;

import java.time.Duration;

/**
 * 邮件模块常量。
 */
public final class EmailConstants {

    /**
     * 邮箱验证码 Redis Key 前缀。
     */
    public static final String VERIFICATION_CODE_KEY_PREFIX = "common:email:verification:";

    /**
     * 邮箱验证码发送冷却 Redis Key 前缀。
     */
    public static final String VERIFICATION_CODE_COOLDOWN_KEY_PREFIX = "common:email:verification:cooldown:";

    /**
     * 邮箱验证码有效期。
     */
    public static final Duration VERIFICATION_CODE_TTL = Duration.ofMinutes(10);

    /**
     * 邮箱验证码发送冷却时间。
     */
    public static final Duration VERIFICATION_CODE_COOLDOWN_TTL = Duration.ofSeconds(60);

    private EmailConstants() {
    }

    /**
     * 构建邮箱验证码 Redis Key。
     *
     * @param tenantId 租户ID
     * @param email 邮箱
     * @return Redis Key
     */
    public static String verificationCodeKey(String tenantId, String email) {
        return VERIFICATION_CODE_KEY_PREFIX + tenantId + ":" + email;
    }

    /**
     * 构建邮箱验证码发送冷却 Redis Key。
     *
     * @param tenantId 租户ID
     * @param email 邮箱
     * @return Redis Key
     */
    public static String verificationCodeCooldownKey(String tenantId, String email) {
        return VERIFICATION_CODE_COOLDOWN_KEY_PREFIX + tenantId + ":" + email;
    }
}
