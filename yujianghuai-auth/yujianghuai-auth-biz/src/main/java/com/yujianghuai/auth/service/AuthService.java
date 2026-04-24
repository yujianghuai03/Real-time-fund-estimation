package com.yujianghuai.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yujianghuai.admin.entity.SysUser;
import com.yujianghuai.admin.mapper.SysRelationMapper;
import com.yujianghuai.admin.mapper.SysUserMapper;
import com.yujianghuai.auth.config.AuthProperties;
import com.yujianghuai.auth.model.LoginRequest;
import com.yujianghuai.auth.model.TokenResponse;
import com.yujianghuai.common.exception.BizException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthProperties properties;
    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AuthTenantService authTenantService;
    private final SysUserMapper userMapper;
    private final SysRelationMapper relationMapper;

    public AuthService(AuthProperties properties,
                       TokenService tokenService,
                       UserDetailsService userDetailsService,
                       PasswordEncoder passwordEncoder,
                       AuthTenantService authTenantService,
                       SysUserMapper userMapper,
                       SysRelationMapper relationMapper) {
        this.properties = properties;
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.authTenantService = authTenantService;
        this.userMapper = userMapper;
        this.relationMapper = relationMapper;
    }

    public TokenResponse login(LoginRequest request) {
        var tenant = authTenantService.resolveTenantByIdentifier(request.getTenantId());
        UserDetails userDetails = authTenantService.runWithTenantContext(tenant.getId(), () -> {
            try {
                return userDetailsService.loadUserByUsername(request.getUsername());
            } catch (UsernameNotFoundException exception) {
                throw new BizException(401, "username or password is invalid");
            }
        });
        if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
            throw new BizException(401, "username or password is invalid");
        }

        ensureLoginPermission(tenant.getId(), request);

        List<String> authorities = userDetails.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .toList();
        String token = tokenService.createToken(
                request.getUsername(),
                String.valueOf(tenant.getId()),
                tenant.getTenantName(),
                authorities,
                properties.getTokenExpireSeconds());
        return new TokenResponse(token, properties.getTokenExpireSeconds());
    }

    public Map<String, Object> check(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BizException(401, "token is missing");
        }

        TokenService.TokenPayload payload = tokenService.verify(authorization.substring(BEARER_PREFIX.length()));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("username", payload.username());
        result.put("expiresAt", payload.expiresAt());
        return result;
    }

    private void ensureLoginPermission(Long tenantId, LoginRequest request) {
        SysUser user = authTenantService.runWithTenantContext(tenantId, () -> userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, request.getUsername())
                        .eq(SysUser::getStatus, 1)
                        .last("limit 1")));
        if (user == null) {
            throw new BizException(401, "username or password is invalid");
        }

        String loginType = normalizeLoginType(request.getLoginType());
        Long menuCount = authTenantService.runWithTenantContext(
                tenantId,
                () -> relationMapper.countLoginMenusByUserId(tenantId, user.getId(), loginType));
        if (menuCount == null || menuCount <= 0) {
            throw new BizException(403, "没有登录权限");
        }
    }

    private String normalizeLoginType(String loginType) {
        return StringUtils.hasText(loginType) && "PORTAL".equalsIgnoreCase(loginType) ? "PORTAL" : "ADMIN";
    }
}
