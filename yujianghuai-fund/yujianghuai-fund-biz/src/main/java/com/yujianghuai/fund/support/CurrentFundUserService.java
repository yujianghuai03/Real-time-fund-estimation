package com.yujianghuai.fund.support;

import com.yujianghuai.common.exception.BusinessException;
import com.yujianghuai.common.exception.ErrorCode;
import com.yujianghuai.common.tenant.TenantContext;
import com.yujianghuai.fund.mapper.CurrentUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class CurrentFundUserService {

    private final CurrentUserMapper currentUserMapper;

    public Long currentTenantId() {
        String tenantId = TenantContext.getTenantId();
        if (!StringUtils.hasText(tenantId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "租户ID不能为空");
        }
        try {
            return Long.valueOf(tenantId);
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "租户ID格式错误");
        }
    }

    public Long currentUserId(Long tenantId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        String username = authentication.getName();
        if (!StringUtils.hasText(username)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        Long userId = currentUserMapper.selectUserIdByUsername(tenantId, username);
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "当前用户不存在或已禁用");
        }
        return userId;
    }
}
