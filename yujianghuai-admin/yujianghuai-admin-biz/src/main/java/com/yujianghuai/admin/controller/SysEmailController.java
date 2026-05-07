package com.yujianghuai.admin.controller;

import java.security.SecureRandom;

import com.yujianghuai.common.constant.EmailConstants;
import com.yujianghuai.common.email.EmailService;
import com.yujianghuai.common.web.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
     * @param email 收件人邮箱
     * @return 发送结果
     */
    @PostMapping("/verification-code")
    @Operation(summary = "发送邮箱验证码", description = "生成六位数字验证码并发送到指定邮箱")
    public R<Boolean> sendVerificationCode(
            @Parameter(description = "收件人邮箱", required = true)
            @NotBlank(message = "收件人邮箱不能为空")
            @Email(message = "收件人邮箱格式不正确")
            @RequestParam String email) {
        if (hasCachedVerificationCode(email)) {
            return R.ok(Boolean.TRUE);
        }

        String code = generateSixDigitCode();
        emailService.sendVerificationCode(email, code);
        cacheVerificationCode(email, code);
        return R.ok(Boolean.TRUE);
    }

    private boolean hasCachedVerificationCode(String email) {
        String redisKey = EmailConstants.verificationCodeKey(email);
        return StringUtils.hasText(stringRedisTemplate.opsForValue().get(redisKey));
    }

    private void cacheVerificationCode(String email, String code) {
        String redisKey = EmailConstants.verificationCodeKey(email);
        stringRedisTemplate.opsForValue().set(redisKey, code, EmailConstants.VERIFICATION_CODE_TTL);
    }

    private String generateSixDigitCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(VERIFICATION_CODE_BOUND));
    }
}
