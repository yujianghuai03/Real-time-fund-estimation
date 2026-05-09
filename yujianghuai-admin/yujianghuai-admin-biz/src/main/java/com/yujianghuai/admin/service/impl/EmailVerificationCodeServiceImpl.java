package com.yujianghuai.admin.service.impl;

import com.yujianghuai.admin.dto.EmailVerificationCodeRequest;
import com.yujianghuai.admin.service.EmailVerificationCodeService;
import com.yujianghuai.common.constant.EmailConstants;
import com.yujianghuai.common.email.EmailProperties;
import com.yujianghuai.common.email.EmailService;
import com.yujianghuai.common.tenant.TenantContext;
import com.yujianghuai.common.web.R;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Collections;
import java.util.Locale;

/**
 * @author yxh
 * @date 2026/5/9 15:23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationCodeServiceImpl implements EmailVerificationCodeService {
    private final EmailService emailService;
    private final EmailProperties emailProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int VERIFICATION_CODE_BOUND = 1_000_000;
    private static final String UNKNOWN_IP = "unknown";
    private static final DefaultRedisScript<Long> INCREMENT_WITH_EXPIRE_SCRIPT = new DefaultRedisScript<>("""
            local current = redis.call('INCR', KEYS[1])
            if current == 1 then
                redis.call('EXPIRE', KEYS[1], ARGV[1])
            end
            return current
            """, Long.class);
    @Override
    public R sendVerificationCode(EmailVerificationCodeRequest request,
                                        HttpServletRequest servletRequest) {
        String tenantId = TenantContext.getRequiredTenantId();
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String clientIp = getClientIp(servletRequest);

        if (isIpLimited(tenantId, clientIp)) {
            return R.fail("当前IP请求过于频繁，请稍后再试");
        }

        if (isInCooldown(tenantId, email)) {
            return R.fail("验证码发送过于频繁，请60秒后再试");
        }
        recordIpLimit(tenantId, clientIp);
        String code = generateSixDigitCode();
        emailService.sendVerificationCode(email, code);
        cacheVerificationCode(tenantId, email, code);
        cacheCooldown(tenantId, email);
        return R.ok(true);
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
     * Redis计数自增并原子设置过期时间。
     *
     * @param redisKey Redis Key
     * @param ttl 过期时间
     */
    private void increaseWithTtl(String redisKey, Duration ttl) {
        stringRedisTemplate.execute(
                INCREMENT_WITH_EXPIRE_SCRIPT,
                Collections.singletonList(redisKey),
                String.valueOf(ttl.toSeconds())
        );
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
        String remoteAddr = request.getRemoteAddr();

        if (isTrustedProxy(remoteAddr)) {
            String ip = firstValidIp(request.getHeader("X-Forwarded-For"));
            if (!StringUtils.hasText(ip)) {
                ip = firstValidIp(request.getHeader("X-Real-IP"));
            }
            if (StringUtils.hasText(ip)) {
                return ip;
            }
        }
        return StringUtils.hasText(remoteAddr) ? remoteAddr : UNKNOWN_IP;
    }
    private boolean isTrustedProxy(String remoteAddr){
        EmailProperties.VerificationCodeLimit limit = emailProperties.getVerificationCodeLimit();
        if (limit == null || !Boolean.TRUE.equals(limit.getIpEnabled())) {
            return false;
        }
        return limit.getTrustedProxies() != null && limit.getTrustedProxies().contains(remoteAddr);
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