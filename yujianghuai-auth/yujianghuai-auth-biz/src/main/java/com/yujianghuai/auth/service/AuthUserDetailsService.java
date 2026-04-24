package com.yujianghuai.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yujianghuai.admin.entity.SysUser;
import com.yujianghuai.admin.mapper.SysRelationMapper;
import com.yujianghuai.admin.mapper.SysUserMapper;
import com.yujianghuai.common.tenant.TenantContext;
import java.util.List;
import org.springframework.security.core.userdetails.User;
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
        var tenant = authTenantService.resolveTenantByIdentifier(TenantContext.getTenantId());
        return authTenantService.runWithTenantContext(tenant.getId(), () -> {
            SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, username)
                    .eq(SysUser::getStatus, 1)
                    .last("limit 1"));
            if (user == null || !StringUtils.hasText(user.getPassword())) {
                throw new UsernameNotFoundException("User not found: " + username);
            }
            List<String> roles = relationMapper.selectRoleCodesByUserId(tenant.getId(), user.getId());
            if (roles == null || roles.isEmpty()) {
                roles = List.of("USER");
            }
            return User.withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles(roles.toArray(String[]::new))
                    .disabled(user.getStatus() == null || user.getStatus() != 1)
                    .build();
        });
    }
}
