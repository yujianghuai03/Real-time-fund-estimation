package com.yujianghuai.auth.support.core;

import com.yujianghuai.auth.support.email.OAuth2ResourceOwnerEmailAuthenticationProvider;
import java.util.Set;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class PublicClientCustomGrantAuthenticationProvider implements AuthenticationProvider {

    private static final Set<String> SUPPORTED_GRANT_TYPES = Set.of(
            AuthorizationGrantType.PASSWORD.getValue(),
            OAuth2ResourceOwnerEmailAuthenticationProvider.GRANT_TYPE
    );

    private final RegisteredClientRepository registeredClientRepository;

    public PublicClientCustomGrantAuthenticationProvider(RegisteredClientRepository registeredClientRepository) {
        Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null");
        this.registeredClientRepository = registeredClientRepository;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2ClientAuthenticationToken clientAuthentication =
                (OAuth2ClientAuthenticationToken) authentication;
        if (!ClientAuthenticationMethod.NONE.equals(clientAuthentication.getClientAuthenticationMethod())) {
            return null;
        }

        String grantType = (String) clientAuthentication.getAdditionalParameters()
                .get(OAuth2ParameterNames.GRANT_TYPE);
        if (!SUPPORTED_GRANT_TYPES.contains(grantType)) {
            return null;
        }

        String clientId = clientAuthentication.getPrincipal().toString();
        RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }
        if (!registeredClient.getClientAuthenticationMethods().contains(ClientAuthenticationMethod.NONE)) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }
        if (!hasGrantType(registeredClient, grantType)) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        }

        return new OAuth2ClientAuthenticationToken(
                registeredClient, ClientAuthenticationMethod.NONE, null);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private boolean hasGrantType(RegisteredClient registeredClient, String grantType) {
        if (!StringUtils.hasText(grantType)) {
            return false;
        }
        return registeredClient.getAuthorizationGrantTypes()
                .contains(new AuthorizationGrantType(grantType));
    }
}
