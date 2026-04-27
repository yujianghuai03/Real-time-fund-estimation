package com.yujianghuai.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yujianghuai.auth.config.AuthProperties;
import com.yujianghuai.auth.model.LoginRequest;
import com.yujianghuai.auth.model.TokenResponse;
import com.yujianghuai.auth.service.AuthService;
import com.yujianghuai.common.exception.BizException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PasswordGrantTokenEndpointFilter extends OncePerRequestFilter {

    private static final String UNAUTHORIZED_MESSAGE = "权限不足，请登录后再试！";
    private static final String TOKEN_ENDPOINT = "/oauth2/token";
    private static final String GRANT_TYPE = "password";
    private static final String TENANT_PARAM = "TENANT-ID";
    private static final String LOGIN_TYPE_PARAM = "LOGIN-TYPE";
    private static final String BASIC_PREFIX = "Basic ";

    private final AuthService authService;
    private final AuthProperties authProperties;
    private final ObjectMapper objectMapper;

    public PasswordGrantTokenEndpointFilter(AuthService authService,
                                            AuthProperties authProperties,
                                            ObjectMapper objectMapper) {
        this.authService = authService;
        this.authProperties = authProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod())
                && TOKEN_ENDPOINT.equals(request.getRequestURI())
                && GRANT_TYPE.equals(request.getParameter("grant_type")));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            validateClient(request);

            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setTenantId(resolveTenantId(request));
            loginRequest.setUsername(requiredParameter(request, "username"));
            loginRequest.setPassword(requiredParameter(request, "password"));
            loginRequest.setLoginType(resolveLoginType(request));

            TokenResponse token = authService.login(loginRequest);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("access_token", token.getAccessToken());
            payload.put("token_type", token.getTokenType());
            payload.put("expires_in", token.getExpiresIn());
            payload.put("scope", defaultScope(request.getParameter("scope")));

            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), payload);
        } catch (BizException exception) {
            writeOAuthError(response, HttpServletResponse.SC_UNAUTHORIZED, "invalid_grant", UNAUTHORIZED_MESSAGE);
        }
    }

    private void validateClient(HttpServletRequest request) {
        String configuredClientId = authProperties.getOauth2().getClientId();
        String configuredClientSecret = authProperties.getOauth2().getClientSecret();
        String header = request.getHeader("Authorization");

        if (StringUtils.hasText(header) && header.startsWith(BASIC_PREFIX)) {
            String credentials = new String(Base64.getDecoder()
                    .decode(header.substring(BASIC_PREFIX.length())), StandardCharsets.UTF_8);
            int separatorIndex = credentials.indexOf(':');
            String clientId = separatorIndex >= 0 ? credentials.substring(0, separatorIndex) : credentials;
            String clientSecret = separatorIndex >= 0 ? credentials.substring(separatorIndex + 1) : "";
            if (!configuredClientId.equals(clientId) || !configuredClientSecret.equals(clientSecret)) {
                throw new BizException(401, UNAUTHORIZED_MESSAGE);
            }
            return;
        }

        String clientId = request.getParameter("client_id");
        String clientSecret = request.getParameter("client_secret");
        if (StringUtils.hasText(clientId) || StringUtils.hasText(clientSecret)) {
            if (!configuredClientId.equals(clientId) || !configuredClientSecret.equals(clientSecret)) {
                throw new BizException(401, UNAUTHORIZED_MESSAGE);
            }
        }
    }

    private String resolveTenantId(HttpServletRequest request) {
        String tenantId = request.getHeader(TENANT_PARAM);
        if (!StringUtils.hasText(tenantId)) {
            tenantId = request.getParameter(TENANT_PARAM);
        }
        return StringUtils.hasText(tenantId) ? tenantId : "1";
    }

    private String resolveLoginType(HttpServletRequest request) {
        String loginType = request.getHeader(LOGIN_TYPE_PARAM);
        if (!StringUtils.hasText(loginType)) {
            loginType = request.getParameter(LOGIN_TYPE_PARAM);
        }
        return StringUtils.hasText(loginType) ? loginType.trim().toUpperCase() : "ADMIN";
    }

    private String requiredParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (!StringUtils.hasText(value)) {
            throw new BizException(400, name + " is required");
        }
        return value;
    }

    private String defaultScope(String scope) {
        return StringUtils.hasText(scope)
                ? scope
                : String.join(" ", authProperties.getOauth2().getScopes());
    }

    private void writeOAuthError(HttpServletResponse response,
                                 int status,
                                 String error,
                                 String description) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("error", error);
        payload.put("error_description", description);
        objectMapper.writeValue(response.getWriter(), payload);
    }
}
