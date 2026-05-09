package com.yujianghuai.auth.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yujianghuai.auth.entity.SysOauthClient;
import com.yujianghuai.auth.mapper.SysOauthClientMapper;
import com.yujianghuai.auth.support.email.OAuth2ResourceOwnerEmailAuthenticationProvider;
import com.yujianghuai.common.tenant.TenantContext;
import java.time.Duration;
import java.util.Arrays;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class SysOauthRegisteredClientRepository implements RegisteredClientRepository {

    private final SysOauthClientMapper clientMapper;

    public SysOauthRegisteredClientRepository(SysOauthClientMapper clientMapper) {
        this.clientMapper = clientMapper;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        throw new UnsupportedOperationException("OAuth2客户端配置请通过sys_oauth_client表维护");
    }

    @Override
    public RegisteredClient findById(String id) {
        Assert.hasText(id, "id cannot be empty");
        Long clientId = parseClientId(id);
        if (clientId == null) {
            return null;
        }

        SysOauthClient client = clientMapper.selectOne(new LambdaQueryWrapper<SysOauthClient>()
                .eq(SysOauthClient::getId, clientId)
                .eq(SysOauthClient::getStatus, 1)
                .last("limit 1"));
        return client == null ? null : toRegisteredClient(client);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        Assert.hasText(clientId, "clientId cannot be empty");
        LambdaQueryWrapper<SysOauthClient> wrapper = new LambdaQueryWrapper<SysOauthClient>()
                .eq(SysOauthClient::getClientId, clientId)
                .eq(SysOauthClient::getStatus, 1);

        Long tenantId = getCurrentTenantId();
        if (tenantId != null) {
            wrapper.eq(SysOauthClient::getTenantId, tenantId);
        }

        SysOauthClient client = clientMapper.selectOne(wrapper
                .orderByAsc(SysOauthClient::getTenantId)
                .orderByAsc(SysOauthClient::getId)
                .last("limit 1"));
        return client == null ? null : toRegisteredClient(client);
    }

    private RegisteredClient toRegisteredClient(SysOauthClient client) {
        RegisteredClient.Builder builder = RegisteredClient.withId(String.valueOf(client.getId()))
                .clientId(client.getClientId())
                .clientSecret(client.getClientSecret())
                .clientName(client.getClientName())
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(isEnabled(client.getRequireAuthorizationConsent()))
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofSeconds(resolveTtl(client.getAccessTokenTtl(), 7200)))
                        .refreshTokenTimeToLive(Duration.ofSeconds(resolveTtl(client.getRefreshTokenTtl(), 604800)))
                        .reuseRefreshTokens(isEnabled(client.getReuseRefreshTokens()))
                        .build());

        split(client.getClientAuthenticationMethods())
                .map(this::toClientAuthenticationMethod)
                .forEach(builder::clientAuthenticationMethod);
        split(client.getAuthorizationGrantTypes())
                .map(this::toAuthorizationGrantType)
                .forEach(builder::authorizationGrantType);
        split(client.getRedirectUris()).forEach(builder::redirectUri);
        split(client.getPostLogoutRedirectUris()).forEach(builder::postLogoutRedirectUri);
        split(client.getScopes()).forEach(builder::scope);
        return builder.build();
    }

    private java.util.stream.Stream<String> split(String value) {
        if (!StringUtils.hasText(value)) {
            return java.util.stream.Stream.empty();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText);
    }

    private ClientAuthenticationMethod toClientAuthenticationMethod(String value) {
        if (ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue().equals(value)) {
            return ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
        }
        if (ClientAuthenticationMethod.CLIENT_SECRET_POST.getValue().equals(value)) {
            return ClientAuthenticationMethod.CLIENT_SECRET_POST;
        }
        if (ClientAuthenticationMethod.NONE.getValue().equals(value)) {
            return ClientAuthenticationMethod.NONE;
        }
        return new ClientAuthenticationMethod(value);
    }

    private AuthorizationGrantType toAuthorizationGrantType(String value) {
        if (AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(value)) {
            return AuthorizationGrantType.AUTHORIZATION_CODE;
        }
        if (AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(value)) {
            return AuthorizationGrantType.REFRESH_TOKEN;
        }
        if (AuthorizationGrantType.CLIENT_CREDENTIALS.getValue().equals(value)) {
            return AuthorizationGrantType.CLIENT_CREDENTIALS;
        }
        if (AuthorizationGrantType.PASSWORD.getValue().equals(value)) {
            return AuthorizationGrantType.PASSWORD;
        }
        if (OAuth2ResourceOwnerEmailAuthenticationProvider.EMAIL_CODE.getValue().equals(value)) {
            return OAuth2ResourceOwnerEmailAuthenticationProvider.EMAIL_CODE;
        }
        return new AuthorizationGrantType(value);
    }

    private Long parseClientId(String id) {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Long getCurrentTenantId() {
        String tenantId = TenantContext.getTenantId();
        if (!StringUtils.hasText(tenantId)) {
            return null;
        }
        try {
            return Long.valueOf(tenantId);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private long resolveTtl(Integer ttl, long defaultValue) {
        return ttl == null || ttl <= 0 ? defaultValue : ttl;
    }

    private boolean isEnabled(Integer value) {
        return value != null && value == 1;
    }
}
