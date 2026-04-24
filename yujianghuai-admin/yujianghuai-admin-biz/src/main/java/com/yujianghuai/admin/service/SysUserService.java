package com.yujianghuai.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yujianghuai.admin.entity.SysTenant;
import com.yujianghuai.admin.entity.SysUser;
import com.yujianghuai.admin.mapper.SysRelationMapper;
import com.yujianghuai.admin.mapper.SysUserMapper;
import com.yujianghuai.admin.model.UserRequest;
import com.yujianghuai.admin.model.UserVO;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SysUserService {

    private final SysUserMapper userMapper;
    private final SysRelationMapper relationMapper;
    private final TenantResolver tenantResolver;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public SysUserService(SysUserMapper userMapper, SysRelationMapper relationMapper, TenantResolver tenantResolver) {
        this.userMapper = userMapper;
        this.relationMapper = relationMapper;
        this.tenantResolver = tenantResolver;
    }

    public List<UserVO> list() {
        SysTenant tenant = tenantResolver.currentTenant();
        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTenantId, tenant.getId())
                .eq(SysUser::getDelFlag, "0")
                .orderByAsc(SysUser::getId));
        List<UserVO> result = new ArrayList<>();
        for (SysUser user : users) {
            result.add(toVO(user, tenant.getTenantCode()));
        }
        return result;
    }

    @Transactional
    public UserVO create(UserRequest request) {
        SysTenant tenant = tenantResolver.currentTenant();
        SysUser user = new SysUser();
        user.setTenantId(tenant.getId());
        user.setUserType(1);
        user.setDelFlag("0");
        copy(request, user);
        user.setPassword(passwordEncoder.encode(StringUtils.hasText(request.getPassword()) ? request.getPassword() : "123456"));
        userMapper.insert(user);
        saveUserRoles(tenant.getId(), user.getId(), request.getRoleIds());
        return toVO(user, tenant.getTenantCode());
    }

    @Transactional
    public UserVO update(Long id, UserRequest request) {
        SysTenant tenant = tenantResolver.currentTenant();
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            return null;
        }
        copy(request, user);
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userMapper.updateById(user);
        saveUserRoles(tenant.getId(), id, request.getRoleIds());
        return toVO(user, tenant.getTenantCode());
    }

    @Transactional
    public Boolean delete(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user != null) {
            user.setDelFlag("1");
            userMapper.updateById(user);
        }
        return Boolean.TRUE;
    }

    private void copy(UserRequest request, SysUser user) {
        user.setDeptId(request.getDeptId());
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());
        user.setStatus(request.getStatus());
    }

    private void saveUserRoles(Long tenantId, Long userId, List<Long> roleIds) {
        relationMapper.deleteUserRoles(tenantId, userId);
        for (Long roleId : roleIds) {
            relationMapper.insertUserRole(tenantId, userId, roleId);
        }
    }

    private UserVO toVO(SysUser user, String tenantCode) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRealName(user.getRealName());
        vo.setEmail(user.getEmail());
        vo.setMobile(user.getMobile());
        vo.setTenant(tenantCode);
        vo.setStatus(user.getStatus());
        vo.setRoleIds(relationMapper.selectRoleIdsByUserId(user.getTenantId(), user.getId()));
        vo.setRoles(relationMapper.selectRoleCodesByUserId(user.getTenantId(), user.getId()));
        return vo;
    }
}
