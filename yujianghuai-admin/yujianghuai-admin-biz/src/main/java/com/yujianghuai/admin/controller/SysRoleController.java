package com.yujianghuai.admin.controller;

import com.yujianghuai.admin.entity.SysRole;
import com.yujianghuai.admin.model.RoleMenuRequest;
import com.yujianghuai.admin.model.RoleRequest;
import com.yujianghuai.admin.service.SysRoleService;
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
@RequestMapping("/admin-api/roles")
@Tag(name = "角色管理", description = "角色相关管理接口")
public class SysRoleController {

    private final SysRoleService roleService;

    public SysRoleController(SysRoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @Operation(summary = "查询角色列表", description = "按当前租户查询角色列表")
    public R<List<SysRole>> list() {
        return R.ok(roleService.list());
    }

    @PostMapping
    @Operation(summary = "创建角色", description = "创建一个新的角色")
    public R<SysRole> create(@Valid @RequestBody RoleRequest request) {
        return R.ok(roleService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色", description = "根据角色ID更新角色信息")
    public R<SysRole> update(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "角色更新请求",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RoleRequest.class)))
            @Valid @RequestBody RoleRequest request) {
        return R.ok(roleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色", description = "根据角色ID执行逻辑删除")
    public R<Boolean> delete(@Parameter(description = "角色ID", required = true) @PathVariable Long id) {
        return R.ok(roleService.delete(id));
    }

    @GetMapping("/{id}/menus")
    @Operation(summary = "查询角色菜单授权", description = "按归属端查询角色已授权的菜单ID列表")
    public R<List<Long>> roleMenus(@Parameter(description = "角色ID", required = true) @PathVariable Long id,
                                   @RequestParam(value = "scope", required = false) String scope) {
        return R.ok(roleService.roleMenuIds(id, scope));
    }

    @PutMapping("/{id}/menus")
    @Operation(summary = "保存角色菜单授权", description = "按归属端保存角色与菜单的授权关系")
    public R<Boolean> saveRoleMenus(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id,
            @RequestParam(value = "scope", required = false) String scope,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "角色菜单授权请求",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RoleMenuRequest.class)))
            @RequestBody RoleMenuRequest request) {
        return R.ok(roleService.saveRoleMenus(id, scope, request));
    }
}
