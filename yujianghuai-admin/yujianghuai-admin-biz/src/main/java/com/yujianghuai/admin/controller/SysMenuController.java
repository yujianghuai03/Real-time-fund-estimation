package com.yujianghuai.admin.controller;

import com.yujianghuai.admin.entity.SysMenu;
import com.yujianghuai.admin.model.MenuRequest;
import com.yujianghuai.admin.model.MenuVO;
import com.yujianghuai.admin.service.SysMenuService;
import com.yujianghuai.common.web.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin-api/menus")
@Tag(name = "菜单管理", description = "菜单相关管理接口")
public class SysMenuController {

    private final SysMenuService menuService;

    public SysMenuController(SysMenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    @Operation(summary = "查询菜单树", description = "按当前租户和权限归属查询菜单树")
    public R<List<MenuVO>> tree(@RequestParam(value = "scope", required = false) String scope) {
        return R.ok(menuService.tree(scope));
    }

    @PostMapping
    @Operation(summary = "创建菜单", description = "创建一个新的菜单节点")
    public R<SysMenu> create(@Valid @RequestBody MenuRequest request) {
        return R.ok(menuService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新菜单", description = "根据菜单ID更新菜单信息")
    public R<SysMenu> update(
            @Parameter(description = "菜单ID", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "菜单更新请求",
                    required = true,
                    content = @Content(schema = @Schema(implementation = MenuRequest.class)))
            @Valid @RequestBody MenuRequest request) {
        return R.ok(menuService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜单", description = "根据菜单ID执行逻辑删除")
    public R<Boolean> delete(@Parameter(description = "菜单ID", required = true) @PathVariable Long id) {
        return R.ok(menuService.delete(id));
    }
}
