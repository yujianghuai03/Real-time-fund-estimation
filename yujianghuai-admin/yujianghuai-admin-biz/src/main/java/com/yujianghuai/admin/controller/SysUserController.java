package com.yujianghuai.admin.controller;

import com.yujianghuai.admin.model.UserRequest;
import com.yujianghuai.admin.model.UserVO;
import com.yujianghuai.admin.service.SysUserService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin-api/users")
@Tag(name = "用户管理", description = "用户相关管理接口")
public class SysUserController {

    private final SysUserService userService;

    public SysUserController(SysUserService userService) {
        this.userService = userService;
    }

    /**
     * 查询用户列表。
     */
    @GetMapping
    @Operation(summary = "查询用户列表", description = "按当前租户查询用户列表")
    public R<List<UserVO>> list() {
        return R.ok(userService.list());
    }

    /**
     * 创建用户。
     */
    @PostMapping
    @Operation(summary = "创建用户", description = "创建一个新的租户用户")
    public R<UserVO> create(@Valid @RequestBody UserRequest request) {
        return R.ok(userService.create(request));
    }

    /**
     * 更新用户。
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "根据用户ID更新用户信息")
    public R<UserVO> update(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "用户更新请求",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRequest.class)))
            @Valid @RequestBody UserRequest request) {
        return R.ok(userService.update(id, request));
    }

    /**
     * 删除用户。
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "根据用户ID执行逻辑删除")
    public R<Boolean> delete(@Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        return R.ok(userService.delete(id));
    }
}
