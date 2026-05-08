package com.yujianghuai.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yujianghuai.admin.entity.SysUser;
import com.yujianghuai.admin.mapper.SysRelationMapper;
import com.yujianghuai.admin.mapper.SysUserMapper;
import com.yujianghuai.common.constant.SecurityConstants;
import com.yujianghuai.common.exception.BizException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthLoginPermissionService {

    private final AuthTenantService authTenantService;
    private final SysUserMapper userMapper;
    private final SysRelationMapper relationMapper;

    public AuthLoginPermissionService(AuthTenantService authTenantService,
                                      SysUserMapper userMapper,
                                      SysRelationMapper relationMapper) {
        this.authTenantService = authTenantService;
        this.userMapper = userMapper;
        this.relationMapper = relationMapper;
    }

    public void ensureLoginPermission(String tenantIdentifier, String username, String loginType) {
        var tenant = authTenantService.resolveTenantByIdentifier(tenantIdentifier);
        SysUser user = authTenantService.runWithTenantContext(tenant.getId(), () -> userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .eq(SysUser::getStatus, 1)
                        .last("limit 1")));
        if (user == null) {
            throw new BizException(401, SecurityConstants.AUTH_USER_NOT_FOUND_MESSAGE);
        }

        String menuScope = normalizeLoginType(loginType);
        Long menuCount = authTenantService.runWithTenantContext(
                tenant.getId(),
                () -> relationMapper.countLoginMenusByUserId(tenant.getId(), user.getId(), menuScope));
        if (menuCount == null || menuCount <= 0) {
            throw new BizException(403, SecurityConstants.AUTH_NO_LOGIN_PERMISSION_MESSAGE);
        }
    }

    private String normalizeLoginType(String loginType) {
        return StringUtils.hasText(loginType) && "PORTAL".equalsIgnoreCase(loginType) ? "PORTAL" : "ADMIN";
    }
}
