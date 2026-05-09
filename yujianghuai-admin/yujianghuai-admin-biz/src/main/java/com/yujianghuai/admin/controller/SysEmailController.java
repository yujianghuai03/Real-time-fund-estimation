package com.yujianghuai.admin.controller;

import java.security.SecureRandom;

import com.yujianghuai.admin.dto.EmailVerificationCodeRequest;
import com.yujianghuai.common.constant.EmailConstants;
import com.yujianghuai.common.email.EmailProperties;
import com.yujianghuai.common.email.EmailService;
import com.yujianghuai.common.tenant.TenantContext;
import com.yujianghuai.common.web.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 邮件发送管理接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin-api/email")
@Tag(name = "邮件发送", description = "邮件发送相关管理接口")
public class SysEmailController {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int VERIFICATION_CODE_BOUND = 1_000_000;
    private static final String UNKNOWN_IP = "unknown";

    private final EmailService emailService;
    private final EmailProperties emailProperties;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 发送邮箱验证码。
     *
     * @param request 邮箱验证码发送请求参数
     * @param servletRequest HTTP请求
     * @return 发送结果
     */
    @PostMapping("/verification-code")
    @Operation(summary = "发送邮箱验证码", description = "生成六位数字验证码并发送到指定邮箱")
    public R<Boolean> sendVerificationCode(@Valid @RequestBody EmailVerificationCodeRequest request,
                                           HttpServletRequest servletRequest) {
        String tenantId = TenantContext.getRequiredTenantId();
        String email = request.getEmail();
        String clientIp = getClientIp(servletRequest);

        if (isIpLimited(tenantId, clientIp)) {
            return R.failed("当前IP请求过于频繁，请稍后再试");
        }

        if (isInCooldown(tenantId, email)) {
            return R.failed("验证码发送过于频繁，请60秒后再试");
        }

        String code = generateSixDigitCode();
        emailService.sendVerificationCode(email, code);

        cacheVerificationCode(tenantId, email, code);
        cacheCooldown(tenantId, email);
        recordIpLimit(tenantId, clientIp);

        return R.ok(Boolean.TRUE);
    }

    /**
     * 判断当前IP是否达到限流阈值。
     *
     * @param tenantId 租户ID
     * @param clientIp 客户端IP
     * @return 是否达到限流阈值
     */
    private boolean isIpLimited(String tenantId, String clientIp) {
        EmailProperties.VerificationCodeLimit limit = emailProperties.getVerificationCodeLimit();
        if (limit == null || !Boolean.TRUE.equals(limit.getIpEnabled())) {
            return false;
        }

        String minuteKey = EmailConstants.verificationCodeIpMinuteLimitKey(tenantId, clientIp);
        String hourKey = EmailConstants.verificationCodeIpHourLimitKey(tenantId, clientIp);

        return getRedisCount(minuteKey) >= safeLimit(limit.getIpMinuteLimit())
                || getRedisCount(hourKey) >= safeLimit(limit.getIpHourLimit());
    }

    /**
     * 记录IP限流次数。
     *
     * @param tenantId 租户ID
     * @param clientIp 客户端IP
     */
    private void recordIpLimit(String tenantId, String clientIp) {
        EmailProperties.VerificationCodeLimit limit = emailProperties.getVerificationCodeLimit();
        if (limit == null || !Boolean.TRUE.equals(limit.getIpEnabled())) {
            return;
        }

        increaseWithTtl(EmailConstants.verificationCodeIpMinuteLimitKey(tenantId, clientIp),
                EmailConstants.VERIFICATION_CODE_IP_MINUTE_LIMIT_TTL);
        increaseWithTtl(EmailConstants.verificationCodeIpHourLimitKey(tenantId, clientIp),
                EmailConstants.VERIFICATION_CODE_IP_HOUR_LIMIT_TTL);
    }

    /**
     * Redis计数自增并设置过期时间。
     *
     * @param redisKey Redis Key
     * @param ttl 过期时间
     */
    private void increaseWithTtl(String redisKey, java.time.Duration ttl) {
        Long count = stringRedisTemplate.opsForValue().increment(redisKey);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(redisKey, ttl);
        }
    }

    /**
     * 获取Redis计数。
     *
     * @param redisKey Redis Key
     * @return 计数值
     */
    private int getRedisCount(String redisKey) {
        String value = stringRedisTemplate.opsForValue().get(redisKey);
        if (!StringUtils.hasText(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    /**
     * 获取安全限流阈值。
     *
     * @param value 配置值
     * @return 阈值
     */
    private int safeLimit(Integer value) {
        return value == null || value <= 0 ? Integer.MAX_VALUE : value;
    }

    /**
     * 判断当前邮箱是否处于发送冷却中。
     *
     * @param tenantId 租户ID
     * @param email 邮箱
     * @return 是否处于冷却中
     */
    private boolean isInCooldown(String tenantId, String email) {
        String redisKey = EmailConstants.verificationCodeCooldownKey(tenantId, email);
        return StringUtils.hasText(stringRedisTemplate.opsForValue().get(redisKey));
    }

    /**
     * 缓存邮箱验证码。
     *
     * @param tenantId 租户ID
     * @param email 邮箱
     * @param code 验证码
     */
    private void cacheVerificationCode(String tenantId, String email, String code) {
        String redisKey = EmailConstants.verificationCodeKey(tenantId, email);
        stringRedisTemplate.opsForValue().set(redisKey, code, EmailConstants.VERIFICATION_CODE_TTL);
    }

    /**
     * 缓存发送冷却状态。
     *
     * @param tenantId 租户ID
     * @param email 邮箱
     */
    private void cacheCooldown(String tenantId, String email) {
        String redisKey = EmailConstants.verificationCodeCooldownKey(tenantId, email);
        stringRedisTemplate.opsForValue().set(redisKey, "1", EmailConstants.VERIFICATION_CODE_COOLDOWN_TTL);
    }

    /**
     * 获取客户端IP。
     *
     * @param request HTTP请求
     * @return 客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = firstValidIp(request.getHeader("X-Forwarded-For"));
        if (!StringUtils.hasText(ip)) {
            ip = firstValidIp(request.getHeader("X-Real-IP"));
        }
        if (!StringUtils.hasText(ip)) {
            ip = request.getRemoteAddr();
        }
        return StringUtils.hasText(ip) ? ip : UNKNOWN_IP;
    }

    /**
     * 获取第一个有效IP。
     *
     * @param value 请求头值
     * @return 第一个有效IP
     */
    private String firstValidIp(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String first = value.split(",")[0].trim();
        return StringUtils.hasText(first) && !UNKNOWN_IP.equalsIgnoreCase(first) ? first : null;
    }

    /**
     * 生成六位数字验证码。
     *
     * @return 六位数字验证码
     */
    private String generateSixDigitCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(VERIFICATION_CODE_BOUND));
    }
}
