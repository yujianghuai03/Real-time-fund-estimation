package com.yujianghuai.auth.support.email;

import com.yujianghuai.common.constant.EmailConstants;
import com.yujianghuai.common.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailCodeAuthenticationService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final DefaultRedisScript<Long> INCREMENT_WITH_EXPIRE_SCRIPT = new DefaultRedisScript<>("""
            local current = redis.call('INCR', KEYS[1])
            if current == 1 then
                redis.call('EXPIRE', KEYS[1], ARGV[1])
            end
            return current
            """, Long.class);

    public void validate(String email, String code) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(code)) {
            throw new BadCredentialsException("邮箱或验证码不能为空");
        }

        String tenantId = TenantContext.getRequiredTenantId();
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String redisKey = EmailConstants.verificationCodeKey(tenantId, normalizedEmail);
        String failCountKey = EmailConstants.verificationCodeFailCountKey(tenantId, normalizedEmail);
        String cachedCode = stringRedisTemplate.opsForValue().get(redisKey);

        if (!StringUtils.hasText(cachedCode)) {
            throw new BadCredentialsException("邮箱验证码错误或已过期");
        }

        if (!cachedCode.equals(code.trim())) {
            Long failCount = increaseWithTtl(failCountKey);

            if (failCount != null && failCount >= EmailConstants.VERIFICATION_CODE_MAX_FAIL_COUNT) {
                stringRedisTemplate.delete(redisKey);
                stringRedisTemplate.delete(failCountKey);
                throw new BadCredentialsException("邮箱验证码错误次数过多，请重新获取");
            }

            throw new BadCredentialsException("邮箱验证码错误或已过期");
        }

        stringRedisTemplate.delete(redisKey);
        stringRedisTemplate.delete(failCountKey);
    }

    private Long increaseWithTtl(String redisKey) {
        return stringRedisTemplate.execute(
                INCREMENT_WITH_EXPIRE_SCRIPT,
                Collections.singletonList(redisKey),
                String.valueOf(EmailConstants.VERIFICATION_CODE_FAIL_COUNT_TTL.toSeconds())
        );
    }
}
