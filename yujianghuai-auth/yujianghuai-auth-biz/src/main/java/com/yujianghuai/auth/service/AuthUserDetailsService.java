package com.yujianghuai.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yujianghuai.admin.entity.SysUser;
import com.yujianghuai.admin.mapper.SysRelationMapper;
import com.yujianghuai.admin.mapper.SysUserMapper;
import com.yujianghuai.auth.support.core.LoginUserDetails;
import com.yujianghuai.common.tenant.TenantContext;
import com.yujianghuai.common.utils.CurrentUserInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthUserDetailsService implements UserDetailsService {

    private final AuthTenantService authTenantService;
    private final SysUserMapper userMapper;
    private final SysRelationMapper relationMapper;

    public AuthUserDetailsService(AuthTenantService authTenantService,
                                  SysUserMapper userMapper,
                                  SysRelationMapper relationMapper) {
        this.authTenantService = authTenantService;
        this.userMapper = userMapper;
        this.relationMapper = relationMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return loadUser(() -> userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getStatus, 1)
                .last("limit 1")), username, true);
    }

    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        return loadUser(() -> userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmail, email)
                .eq(SysUser::getStatus, 1)
                .last("limit 1")), email, false);
    }

    private UserDetails loadUser(Supplier<SysUser> userSupplier, String identifier, boolean requirePassword) {
        var tenant = authTenantService.resolveTenantByIdentifier(TenantContext.getTenantId());
        return authTenantService.runWithTenantContext(tenant.getId(), () -> {
            SysUser user = userSupplier.get();
            if (user == null || (requirePassword && !StringUtils.hasText(user.getPassword()))) {
                throw new UsernameNotFoundException("User not found: " + identifier);
            }
            List<String> roles = relationMapper.selectRoleCodesByUserId(tenant.getId(), user.getId());
            if (roles == null || roles.isEmpty()) {
                roles = List.of("USER");
            }
            List<GrantedAuthority> authorities = new ArrayList<>();
            for (String role : roles) {
                if (StringUtils.hasText(role)) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
            }
            List<String> permissions = relationMapper.selectPermissionsByUserId(tenant.getId(), user.getId());
            if (permissions != null) {
                permissions.stream()
                        .filter(StringUtils::hasText)
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);
            }
            CurrentUserInfo currentUserInfo = new CurrentUserInfo(
                    user.getId(),
                    user.getUsername(),
                    user.getNickname(),
                    user.getRealName(),
                    tenant.getTenantCode());
            return new LoginUserDetails(
                    user.getUsername(),
                    StringUtils.hasText(user.getPassword()) ? user.getPassword() : "",
                    user.getStatus() != null && user.getStatus() == 1,
                    authorities,
                    currentUserInfo);
        });
    }
}
