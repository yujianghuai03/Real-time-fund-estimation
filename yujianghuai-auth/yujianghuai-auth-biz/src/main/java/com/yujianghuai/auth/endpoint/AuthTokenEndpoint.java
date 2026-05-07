//package com.yujianghuai.auth.endpoint;
//
//import com.yujianghuai.auth.model.*;
//import com.yujianghuai.auth.service.AuthAccountService;
//import com.yujianghuai.auth.service.AuthService;
//import com.yujianghuai.auth.service.AuthTenantService;
//import com.yujianghuai.auth.service.TokenService;
//import com.yujianghuai.common.web.R;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.servlet.http.HttpServletResponse;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
//import jakarta.validation.Valid;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.oauth2.core.OAuth2AccessToken;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.jwt.JwtDecoder;
//import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
//import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
//import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
//import org.springframework.util.StringUtils;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/token")
//@Tag(name = "令牌管理", description = "OAuth2 令牌相关接口")
//public class AuthTokenEndpoint {
//
//    private static final String BEARER_PREFIX = OAuth2AccessToken.TokenType.BEARER.getValue() + " ";
//    private static final String UNAUTHORIZED_MESSAGE = "权限不足，请登录后再试！";
//
//    private final OAuth2AuthorizationService authorizationService;
//    private final JwtDecoder jwtDecoder;
//    private final TokenService tokenService;
//    private final AuthTenantService authTenantService;
//    private final AuthService authService;
//    private final AuthAccountService authAccountService;
//
//    public AuthTokenEndpoint(OAuth2AuthorizationService authorizationService,
//                             JwtDecoder jwtDecoder,
//                             TokenService tokenService,
//                             AuthTenantService authTenantService,
//                             AuthService authService,
//                             AuthAccountService authAccountService) {
//        this.authorizationService = authorizationService;
//        this.jwtDecoder = jwtDecoder;
//        this.tokenService = tokenService;
//        this.authTenantService = authTenantService;
//        this.authService = authService;
//        this.authAccountService = authAccountService;
//    }
//
//    @DeleteMapping("/logout")
//    @Operation(summary = "退出登录", description = "根据 Authorization 请求头移除访问令牌")
//    public R<Boolean> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
//        if (!StringUtils.hasText(authHeader)) {
//            return R.ok(Boolean.TRUE);
//        }
//        String tokenValue = authHeader.startsWith(BEARER_PREFIX)
//                ? authHeader.substring(BEARER_PREFIX.length()).trim()
//                : authHeader.trim();
//        OAuth2Authorization authorization = authorizationService.findByToken(tokenValue, OAuth2TokenType.ACCESS_TOKEN);
//        if (authorization != null) {
//            authorizationService.remove(authorization);
//        }
//        return R.ok(Boolean.TRUE);
//    }
//
//    @GetMapping("/check_token")
//    @Operation(summary = "校验访问令牌", description = "校验 token 参数中的访问令牌并返回声明信息")
//    public R<Map<String, Object>> checkToken(
//            @Parameter(description = "访问令牌", required = true) @RequestParam String token,
//            HttpServletResponse response) {
//        if (!StringUtils.hasText(token)) {
//            response.setStatus(HttpStatus.UNAUTHORIZED.value());
//            return R.fail(401, UNAUTHORIZED_MESSAGE);
//        }
//
//        OAuth2Authorization authorization = authorizationService.findByToken(token, OAuth2TokenType.ACCESS_TOKEN);
//        if (authorization != null && authorization.getAccessToken() != null) {
//            return R.ok(authorization.getAccessToken().getClaims());
//        }
//
//        try {
//            Jwt jwt = jwtDecoder.decode(token);
//            return R.ok(jwt.getClaims());
//        } catch (RuntimeException exception) {
//            response.setStatus(HttpStatus.UNAUTHORIZED.value());
//            return R.fail(401, UNAUTHORIZED_MESSAGE);
//        }
//    }
//
//    @GetMapping("/userinfo")
//    @Operation(summary = "查询用户信息", description = "根据 Bearer Token 获取当前用户声明信息")
//    public R<Map<String, Object>> userinfo(
//            @Parameter(description = "Bearer Token", required = true)
//            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
//        TokenService.TokenPayload payload = tokenService.verify(authorization.replace(BEARER_PREFIX, "").trim());
//        Map<String, Object> claims = new LinkedHashMap<>(payload.claims());
//        Object tenantId = claims.get("tenant_id");
//        if (!claims.containsKey("tenant_name")) {
//            String tenantName = authTenantService.resolveTenantName(tenantId);
//            if (StringUtils.hasText(tenantName)) {
//                claims.put("tenant_name", tenantName);
//            }
//        }
//        return R.ok(claims);
//    }
//    @GetMapping("/check")
//    @Operation(summary = "校验令牌", description = "校验 Authorization 请求头中的访问令牌")
//    public R<Map<String, Object>> check(
//            @Parameter(description = "Bearer Token", required = true)
//            @RequestHeader("Authorization") String authorization) {
//        return R.ok(authService.check(authorization));
//    }
//    @PostMapping("/login")
//    @Operation(summary = "用户登录", description = "根据租户、用户名和密码进行登录并返回访问令牌")
//    public R<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
//        return R.ok(authService.login(request));
//    }
//    @GetMapping("/tenants")
//    @Operation(summary = "查询租户列表", description = "获取启用状态的租户列表，用于登录和注册选择")
//    public R<List<TenantOptionVO>> tenants() {
//        return R.ok(authTenantService.listTenants());
//    }
//
//    @PostMapping("/register")
//    @Operation(summary = "用户注册", description = "根据租户创建一个新的普通用户账号")
//    public R<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
//        return R.ok(authAccountService.register(request));
//    }
//}
