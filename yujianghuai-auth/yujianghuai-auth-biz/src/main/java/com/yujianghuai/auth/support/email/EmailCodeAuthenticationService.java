package com.yujianghuai.auth.support.email;

import com.yujianghuai.common.constant.EmailConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EmailCodeAuthenticationService {

    private final StringRedisTemplate stringRedisTemplate;

    public void validate(String email, String code) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(code)) {
            throw new BadCredentialsException("邮箱或验证码不能为空");
        }
        String redisKey = EmailConstants.verificationCodeKey(email.trim());
        String cachedCode = stringRedisTemplate.opsForValue().get(redisKey);
        if (!StringUtils.hasText(cachedCode) || !cachedCode.equals(code.trim())) {
            throw new BadCredentialsException("邮箱验证码错误或已过期");
        }
        stringRedisTemplate.delete(redisKey);
    }
}
