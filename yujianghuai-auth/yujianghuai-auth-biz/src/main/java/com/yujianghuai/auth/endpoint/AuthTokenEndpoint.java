package com.yujianghuai.auth.endpoint;

import com.yujianghuai.auth.service.AuthTenantService;
import com.yujianghuai.auth.service.TokenService;
import com.yujianghuai.common.web.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/token")
@Tag(name = "令牌管理", description = "OAuth2 令牌相关接口")
public class AuthTokenEndpoint {

    private static final String BEARER_PREFIX = OAuth2AccessToken.TokenType.BEARER.getValue() + " ";

    private final OAuth2AuthorizationService authorizationService;
    private final JwtDecoder jwtDecoder;
    private final TokenService tokenService;
    private final AuthTenantService authTenantService;

    public AuthTokenEndpoint(OAuth2AuthorizationService authorizationService,
                             JwtDecoder jwtDecoder,
                             TokenService tokenService,
                             AuthTenantService authTenantService) {
        this.authorizationService = authorizationService;
        this.jwtDecoder = jwtDecoder;
        this.tokenService = tokenService;
        this.authTenantService = authTenantService;
    }

    @DeleteMapping("/logout")
    @Operation(summary = "退出登录", description = "根据 Authorization 请求头移除访问令牌")
    public R<Boolean> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        if (!StringUtils.hasText(authHeader)) {
            return R.ok(Boolean.TRUE);
        }
        String tokenValue = authHeader.startsWith(BEARER_PREFIX)
                ? authHeader.substring(BEARER_PREFIX.length()).trim()
                : authHeader.trim();
        OAuth2Authorization authorization = authorizationService.findByToken(tokenValue, OAuth2TokenType.ACCESS_TOKEN);
        if (authorization != null) {
            authorizationService.remove(authorization);
        }
        return R.ok(Boolean.TRUE);
    }

    @GetMapping("/check_token")
    @Operation(summary = "校验访问令牌", description = "校验 token 参数中的访问令牌并返回声明信息")
    public R<Map<String, Object>> checkToken(
            @Parameter(description = "访问令牌", required = true) @RequestParam String token,
            HttpServletResponse response) {
        if (!StringUtils.hasText(token)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return R.fail(401, "token is missing");
        }

        OAuth2Authorization authorization = authorizationService.findByToken(token, OAuth2TokenType.ACCESS_TOKEN);
        if (authorization != null && authorization.getAccessToken() != null) {
            return R.ok(authorization.getAccessToken().getClaims());
        }

        try {
            Jwt jwt = jwtDecoder.decode(token);
            return R.ok(jwt.getClaims());
        } catch (RuntimeException exception) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return R.fail(401, "token is invalid");
        }
    }

    @GetMapping("/userinfo")
    @Operation(summary = "查询用户信息", description = "根据 Bearer Token 获取当前用户声明信息")
    public R<Map<String, Object>> userinfo(
            @Parameter(description = "Bearer Token", required = true)
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        TokenService.TokenPayload payload = tokenService.verify(authorization.replace(BEARER_PREFIX, "").trim());
        Map<String, Object> claims = new LinkedHashMap<>(payload.claims());
        Object tenantId = claims.get("tenant_id");
        if (!claims.containsKey("tenant_name")) {
            String tenantName = authTenantService.resolveTenantName(tenantId);
            if (StringUtils.hasText(tenantName)) {
                claims.put("tenant_name", tenantName);
            }
        }
        return R.ok(claims);
    }
}
