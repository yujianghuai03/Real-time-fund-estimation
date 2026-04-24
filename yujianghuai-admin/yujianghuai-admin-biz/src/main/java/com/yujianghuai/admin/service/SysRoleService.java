package com.yujianghuai.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yujianghuai.admin.entity.SysMenu;
import com.yujianghuai.admin.entity.SysRole;
import com.yujianghuai.admin.mapper.SysMenuMapper;
import com.yujianghuai.admin.mapper.SysRelationMapper;
import com.yujianghuai.admin.mapper.SysRoleMapper;
import com.yujianghuai.admin.model.RoleMenuRequest;
import com.yujianghuai.admin.model.RoleRequest;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRelationMapper relationMapper;
    private final TenantResolver tenantResolver;
    private final SysMenuMapper menuMapper;
    private final SysMenuService menuService;

    public SysRoleService(SysRoleMapper roleMapper,
                          SysRelationMapper relationMapper,
                          TenantResolver tenantResolver,
                          SysMenuMapper menuMapper,
                          SysMenuService menuService) {
        this.roleMapper = roleMapper;
        this.relationMapper = relationMapper;
        this.tenantResolver = tenantResolver;
        this.menuMapper = menuMapper;
        this.menuService = menuService;
    }

    public List<SysRole> list() {
        Long tenantId = tenantResolver.currentTenantId();
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getDelFlag, "0")
                .orderByAsc(SysRole::getSortOrder)
                .orderByAsc(SysRole::getId));
    }

    @Transactional
    public SysRole create(RoleRequest request) {
        SysRole role = new SysRole();
        role.setTenantId(tenantResolver.currentTenantId());
        role.setRoleType(1);
        role.setDelFlag("0");
        copy(request, role);
        roleMapper.insert(role);
        return role;
    }

    @Transactional
    public SysRole update(Long id, RoleRequest request) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            return null;
        }
        copy(request, role);
        roleMapper.updateById(role);
        return role;
    }

    @Transactional
    public Boolean delete(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role != null) {
            role.setDelFlag("1");
            roleMapper.updateById(role);
        }
        return Boolean.TRUE;
    }

    public List<Long> roleMenuIds(Long roleId, String scope) {
        Long tenantId = tenantResolver.currentTenantId();
        Set<Long> scopedMenuIds = queryScopedMenuIds(tenantId, scope);
        return relationMapper.selectMenuIdsByRoleId(tenantId, roleId).stream()
                .filter(scopedMenuIds::contains)
                .toList();
    }

    @Transactional
    public Boolean saveRoleMenus(Long roleId, String scope, RoleMenuRequest request) {
        Long tenantId = tenantResolver.currentTenantId();
        Set<Long> scopedMenuIds = queryScopedMenuIds(tenantId, scope);
        List<Long> existingIds = relationMapper.selectMenuIdsByRoleId(tenantId, roleId);

        relationMapper.deleteRoleMenus(tenantId, roleId);
        for (Long existingId : existingIds) {
            if (!scopedMenuIds.contains(existingId)) {
                relationMapper.insertRoleMenu(tenantId, roleId, existingId);
            }
        }
        if (request.getMenuIds() != null) {
            for (Long menuId : request.getMenuIds()) {
                relationMapper.insertRoleMenu(tenantId, roleId, menuId);
            }
        }
        return Boolean.TRUE;
    }

    private Set<Long> queryScopedMenuIds(Long tenantId, String scope) {
        return menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getTenantId, tenantId)
                        .eq(SysMenu::getDelFlag, "0")
                        .eq(SysMenu::getMenuScope, menuService.normalizeScope(scope)))
                .stream()
                .map(SysMenu::getId)
                .collect(Collectors.toSet());
    }

    private void copy(RoleRequest request, SysRole role) {
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setDataScope(request.getDataScope());
        role.setSortOrder(request.getSortOrder());
        role.setStatus(request.getStatus());
    }
}
