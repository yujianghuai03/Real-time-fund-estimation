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
     * 邮箱验证码失败次数 Redis Key 前缀。
     */
    public static final String VERIFICATION_CODE_FAIL_COUNT_KEY_PREFIX = "common:email:verification:fail:";

    /**
     * 邮箱验证码 IP 分钟限流 Redis Key 前缀。
     */
    public static final String VERIFICATION_CODE_IP_MINUTE_LIMIT_KEY_PREFIX = "common:email:verification:ip:minute:";

    /**
     * 邮箱验证码 IP 小时限流 Redis Key 前缀。
     */
    public static final String VERIFICATION_CODE_IP_HOUR_LIMIT_KEY_PREFIX = "common:email:verification:ip:hour:";

    /**
     * 邮箱验证码有效期。
     */
    public static final Duration VERIFICATION_CODE_TTL = Duration.ofMinutes(10);

    /**
     * 邮箱验证码发送冷却时间。
     */
    public static final Duration VERIFICATION_CODE_COOLDOWN_TTL = Duration.ofSeconds(60);

    /**
     * 邮箱验证码失败次数限制窗口。
     */
    public static final Duration VERIFICATION_CODE_FAIL_COUNT_TTL = Duration.ofMinutes(10);

    /**
     * 邮箱验证码最大失败次数。
     */
    public static final int VERIFICATION_CODE_MAX_FAIL_COUNT = 5;

    /**
     * 邮箱验证码 IP 分钟限流窗口。
     */
    public static final Duration VERIFICATION_CODE_IP_MINUTE_LIMIT_TTL = Duration.ofMinutes(1);

    /**
     * 邮箱验证码 IP 小时限流窗口。
     */
    public static final Duration VERIFICATION_CODE_IP_HOUR_LIMIT_TTL = Duration.ofHours(1);

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

    /**
     * 构建邮箱验证码失败次数 Redis Key。
     *
     * @param tenantId 租户ID
     * @param email 邮箱
     * @return Redis Key
     */
    public static String verificationCodeFailCountKey(String tenantId, String email) {
        return VERIFICATION_CODE_FAIL_COUNT_KEY_PREFIX + tenantId + ":" + email;
    }

    /**
     * 构建邮箱验证码 IP 分钟限流 Redis Key。
     *
     * @param tenantId 租户ID
     * @param clientIp 客户端IP
     * @return Redis Key
     */
    public static String verificationCodeIpMinuteLimitKey(String tenantId, String clientIp) {
        return VERIFICATION_CODE_IP_MINUTE_LIMIT_KEY_PREFIX + tenantId + ":" + clientIp;
    }

    /**
     * 构建邮箱验证码 IP 小时限流 Redis Key。
     *
     * @param tenantId 租户ID
     * @param clientIp 客户端IP
     * @return Redis Key
     */
    public static String verificationCodeIpHourLimitKey(String tenantId, String clientIp) {
        return VERIFICATION_CODE_IP_HOUR_LIMIT_KEY_PREFIX + tenantId + ":" + clientIp;
    }
}
