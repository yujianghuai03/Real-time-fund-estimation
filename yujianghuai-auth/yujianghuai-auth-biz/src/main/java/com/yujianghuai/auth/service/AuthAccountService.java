package com.yujianghuai.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yujianghuai.admin.entity.SysRole;
import com.yujianghuai.admin.entity.SysUser;
import com.yujianghuai.admin.mapper.SysRelationMapper;
import com.yujianghuai.admin.mapper.SysRoleMapper;
import com.yujianghuai.admin.mapper.SysUserMapper;
import com.yujianghuai.auth.model.RegisterRequest;
import com.yujianghuai.auth.model.RegisterResponse;
import com.yujianghuai.common.exception.BizException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthAccountService {

    private final AuthTenantService authTenantService;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysRelationMapper relationMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthAccountService(AuthTenantService authTenantService,
                              SysUserMapper userMapper,
                              SysRoleMapper roleMapper,
                              SysRelationMapper relationMapper,
                              PasswordEncoder passwordEncoder) {
        this.authTenantService = authTenantService;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.relationMapper = relationMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        var tenant = authTenantService.resolveTenantById(request.getTenantId());
        return authTenantService.runWithTenantContext(tenant.getId(), () -> {
            String username = request.getUsername().trim();
            long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, username));
            if (exists > 0) {
                throw new BizException(400, "username already exists");
            }

            SysUser user = new SysUser();
            user.setTenantId(tenant.getId());
            user.setUsername(username);
            user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname().trim() : username);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setUserType(1);
            user.setStatus(1);
            user.setEmail(request.getEmail());
            userMapper.insert(user);

            SysRole defaultRole = resolveDefaultRole(tenant.getId());
            if (defaultRole != null) {
                relationMapper.insertUserRole(tenant.getId(), user.getId(), defaultRole.getId());
            }

            return new RegisterResponse(user.getUsername(), user.getNickname(), tenant.getTenantName());
        });
    }

    private SysRole resolveDefaultRole(Long tenantId) {
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getRoleCode, "USER")
                .eq(SysRole::getStatus, 1)
                .last("limit 1"));
        if (role != null) {
            return role;
        }
        return roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getStatus, 1)
                .orderByAsc(SysRole::getSortOrder)
                .last("limit 1"));
    }
}
