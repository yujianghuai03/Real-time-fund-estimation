package com.yujianghuai.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yujianghuai.admin.entity.SysMenu;
import com.yujianghuai.admin.mapper.SysMenuMapper;
import com.yujianghuai.admin.model.MenuRequest;
import com.yujianghuai.admin.model.MenuVO;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysMenuService {

    public static final String MENU_SCOPE_ADMIN = "ADMIN";
    public static final String MENU_SCOPE_PORTAL = "PORTAL";

    private final SysMenuMapper menuMapper;
    private final TenantResolver tenantResolver;

    public SysMenuService(SysMenuMapper menuMapper, TenantResolver tenantResolver) {
        this.menuMapper = menuMapper;
        this.tenantResolver = tenantResolver;
    }

    public List<MenuVO> tree(String menuScope) {
        Long tenantId = tenantResolver.currentTenantId();
        List<SysMenu> menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getDelFlag, "0")
                .eq(SysMenu::getTenantId, tenantId)
                .eq(SysMenu::getMenuScope, normalizeScope(menuScope))
                .orderByAsc(SysMenu::getSortOrder)
                .orderByAsc(SysMenu::getId));
        Map<Long, MenuVO> map = new LinkedHashMap<>();
        for (SysMenu menu : menus) {
            map.put(menu.getId(), toVO(menu));
        }
        List<MenuVO> roots = new ArrayList<>();
        for (MenuVO menu : map.values()) {
            if (menu.getParentId() == null || menu.getParentId() == 0 || !map.containsKey(menu.getParentId())) {
                roots.add(menu);
            } else {
                map.get(menu.getParentId()).getChildren().add(menu);
            }
        }
        sortTree(roots);
        return roots;
    }

    @Transactional
    public SysMenu create(MenuRequest request) {
        SysMenu menu = new SysMenu();
        copy(request, menu);
        menu.setTenantId(tenantResolver.currentTenantId());
        menu.setDelFlag("0");
        menuMapper.insert(menu);
        return menu;
    }

    @Transactional
    public SysMenu update(Long id, MenuRequest request) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            return null;
        }
        copy(request, menu);
        menuMapper.updateById(menu);
        return menu;
    }

    @Transactional
    public Boolean delete(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu != null) {
            menu.setDelFlag("1");
            menuMapper.updateById(menu);
        }
        return Boolean.TRUE;
    }

    public String normalizeScope(String menuScope) {
        return MENU_SCOPE_PORTAL.equalsIgnoreCase(menuScope) ? MENU_SCOPE_PORTAL : MENU_SCOPE_ADMIN;
    }

    private void copy(MenuRequest request, SysMenu menu) {
        menu.setParentId(request.getParentId());
        menu.setMenuType(request.getMenuType());
        menu.setMenuScope(normalizeScope(request.getMenuScope()));
        menu.setMenuName(request.getMenuName());
        menu.setPermission(request.getPermission());
        menu.setPath(request.getPath());
        menu.setComponent(request.getComponent());
        menu.setIcon(request.getIcon());
        menu.setMethod(request.getMethod());
        menu.setApiPath(request.getApiPath());
        menu.setSortOrder(request.getSortOrder());
        menu.setVisible(request.getVisible());
        menu.setStatus(request.getStatus());
    }

    private MenuVO toVO(SysMenu menu) {
        MenuVO vo = new MenuVO();
        vo.setId(menu.getId());
        vo.setParentId(menu.getParentId());
        vo.setType(menu.getMenuType());
        vo.setScope(menu.getMenuScope());
        vo.setTitle(menu.getMenuName());
        vo.setPermission(menu.getPermission());
        vo.setPath(menu.getPath());
        vo.setComponent(menu.getComponent());
        vo.setIcon(menu.getIcon());
        vo.setMethod(menu.getMethod());
        vo.setApiPath(menu.getApiPath());
        vo.setVisible(menu.getVisible() != null && menu.getVisible() == 1);
        vo.setSort(menu.getSortOrder());
        vo.setStatus(menu.getStatus());
        return vo;
    }

    private void sortTree(List<MenuVO> menus) {
        menus.sort(Comparator.comparing(MenuVO::getSort, Comparator.nullsLast(Integer::compareTo)));
        for (MenuVO menu : menus) {
            sortTree(menu.getChildren());
        }
    }
}
