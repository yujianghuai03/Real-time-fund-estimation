package com.yujianghuai.admin.controller;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Collections;

import com.yujianghuai.admin.dto.EmailVerificationCodeRequest;
import com.yujianghuai.admin.service.EmailVerificationCodeService;
import com.yujianghuai.admin.service.impl.EmailVerificationCodeServiceImpl;
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
import org.springframework.data.redis.core.script.DefaultRedisScript;
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

    private final EmailVerificationCodeService verificationCodeService;

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

        return verificationCodeService.sendVerificationCode(request, servletRequest);
    }


}
