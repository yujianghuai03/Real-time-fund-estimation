package com.yujianghuai.auth.support.password;

import com.yujianghuai.auth.service.AuthLoginPermissionService;
import com.yujianghuai.auth.support.base.OAuth2ResourceOwnerBaseAuthenticationProvider;
import com.yujianghuai.auth.support.core.AuthTokenCustomizer;
import com.yujianghuai.common.exception.BizException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

public class OAuth2ResourceOwnerPasswordAuthenticationProvider
        extends OAuth2ResourceOwnerBaseAuthenticationProvider<OAuth2ResourceOwnerPasswordAuthenticationToken> {

    private static final String PARAM_LOGIN_TYPE = "LOGIN-TYPE";

    private final AuthLoginPermissionService loginPermissionService;

    public OAuth2ResourceOwnerPasswordAuthenticationProvider(AuthenticationManager authenticationManager,
                                                            OAuth2AuthorizationService authorizationService,
                                                            OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
                                                            AuthLoginPermissionService loginPermissionService) {
        super(authenticationManager, authorizationService, tokenGenerator);
        this.loginPermissionService = loginPermissionService;
    }

    @Override
    public UsernamePasswordAuthenticationToken buildToken(OAuth2ResourceOwnerPasswordAuthenticationToken authentication) {
        String username = (String) authentication.getAdditionalParameters().get(OAuth2ParameterNames.USERNAME);
        String password = (String) authentication.getAdditionalParameters().get(OAuth2ParameterNames.PASSWORD);
        return UsernamePasswordAuthenticationToken.unauthenticated(username, password);
    }

    @Override
    protected Authentication authenticateUser(OAuth2ResourceOwnerPasswordAuthenticationToken authentication) {
        Authentication userAuthentication = super.authenticateUser(authentication);
        try {
            String username = (String) authentication.getAdditionalParameters().get(OAuth2ParameterNames.USERNAME);
            String tenantId = (String) authentication.getAdditionalParameters().get(AuthTokenCustomizer.PARAM_TENANT_ID);
            String loginType = (String) authentication.getAdditionalParameters().get(PARAM_LOGIN_TYPE);
            loginPermissionService.ensureLoginPermission(tenantId, username, loginType);
        } catch (BizException exception) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT, exception.getMessage(), null), exception);
        }
        return userAuthentication;
    }

    @Override
    public void checkClient(RegisteredClient registeredClient) {
        if (!registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.PASSWORD)) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2ResourceOwnerPasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
