package com.yujianghuai.auth.controller;

import com.yujianghuai.auth.model.LoginRequest;
import com.yujianghuai.auth.model.RegisterRequest;
import com.yujianghuai.auth.model.RegisterResponse;
import com.yujianghuai.auth.model.TenantOptionVO;
import com.yujianghuai.auth.model.TokenResponse;
import com.yujianghuai.auth.service.AuthAccountService;
import com.yujianghuai.auth.service.AuthService;
import com.yujianghuai.auth.service.AuthTenantService;
import com.yujianghuai.common.web.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理", description = "登录认证相关接口")
public class AuthController {

    private final AuthService authService;
    private final AuthTenantService authTenantService;
    private final AuthAccountService authAccountService;

    public AuthController(AuthService authService,
                          AuthTenantService authTenantService,
                          AuthAccountService authAccountService) {
        this.authService = authService;
        this.authTenantService = authTenantService;
        this.authAccountService = authAccountService;
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "根据租户、用户名和密码进行登录并返回访问令牌")
    public R<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    @GetMapping("/tenants")
    @Operation(summary = "查询租户列表", description = "获取启用状态的租户列表，用于登录和注册选择")
    public R<List<TenantOptionVO>> tenants() {
        return R.ok(authTenantService.listTenants());
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "根据租户创建一个新的普通用户账号")
    public R<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return R.ok(authAccountService.register(request));
    }

    @GetMapping("/check")
    @Operation(summary = "校验令牌", description = "校验 Authorization 请求头中的访问令牌")
    public R<Map<String, Object>> check(
            @Parameter(description = "Bearer Token", required = true)
            @RequestHeader("Authorization") String authorization) {
        return R.ok(authService.check(authorization));
    }
}
