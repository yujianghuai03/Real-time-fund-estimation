package com.yujianghuai.admin.controller;

import java.security.SecureRandom;

import com.yujianghuai.admin.dto.EmailVerificationCodeRequest;
import com.yujianghuai.common.constant.EmailConstants;
import com.yujianghuai.common.email.EmailService;
import com.yujianghuai.common.tenant.TenantContext;
import com.yujianghuai.common.web.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    private final EmailService emailService;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 发送邮箱验证码。
     *
     * @param request 邮箱验证码发送请求参数
     * @return 发送结果
     */
    @PostMapping("/verification-code")
    @Operation(summary = "发送邮箱验证码", description = "生成六位数字验证码并发送到指定邮箱")
    public R<Boolean> sendVerificationCode(@Valid @RequestBody EmailVerificationCodeRequest request) {
        String tenantId = TenantContext.getRequiredTenantId();
        String email = request.getEmail();

        if (isInCooldown(tenantId, email)) {
            return R.failed("验证码发送过于频繁，请60秒后再试");
        }

        String code = generateSixDigitCode();
        emailService.sendVerificationCode(email, code);

        cacheVerificationCode(tenantId, email, code);
        cacheCooldown(tenantId, email);

        return R.ok(Boolean.TRUE);
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
     * 生成六位数字验证码。
     *
     * @return 六位数字验证码
     */
    private String generateSixDigitCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(VERIFICATION_CODE_BOUND));
    }
}
