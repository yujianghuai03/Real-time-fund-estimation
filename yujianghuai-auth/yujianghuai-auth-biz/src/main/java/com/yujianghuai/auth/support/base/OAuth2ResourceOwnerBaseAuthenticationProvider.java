package com.yujianghuai.auth.support.base;

import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.CollectionUtils;

public abstract class OAuth2ResourceOwnerBaseAuthenticationProvider<T extends OAuth2ResourceOwnerBaseAuthenticationToken>
        implements AuthenticationProvider {

    private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";

    private final AuthenticationManager authenticationManager;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<?> tokenGenerator;

    protected OAuth2ResourceOwnerBaseAuthenticationProvider(AuthenticationManager authenticationManager,
                                                           OAuth2AuthorizationService authorizationService,
                                                           OAuth2TokenGenerator<?> tokenGenerator) {
        this.authenticationManager = authenticationManager;
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
    }

    public abstract UsernamePasswordAuthenticationToken buildToken(T authentication);

    public abstract void checkClient(RegisteredClient registeredClient);

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        T resourceOwnerAuthentication = (T) authentication;
        OAuth2ClientAuthenticationToken clientPrincipal =
                getAuthenticatedClientElseThrowInvalidClient(resourceOwnerAuthentication);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        checkClient(registeredClient);

        Set<String> authorizedScopes = resolveAuthorizedScopes(resourceOwnerAuthentication, registeredClient);

        try {
            Authentication userAuthentication = authenticateUser(resourceOwnerAuthentication);
            return generateAuthenticationToken(resourceOwnerAuthentication, clientPrincipal, registeredClient,
                    authorizedScopes, userAuthentication);
        } catch (OAuth2AuthenticationException exception) {
            throw exception;
        } catch (AuthenticationException exception) {
            OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT,
                    exception.getMessage(), ERROR_URI);
            throw new OAuth2AuthenticationException(error, exception);
        }
    }

    protected Authentication authenticateUser(T authentication) {
        return authenticationManager.authenticate(buildToken(authentication));
    }

    private Set<String> resolveAuthorizedScopes(T authentication, RegisteredClient registeredClient) {
        if (CollectionUtils.isEmpty(authentication.getScopes())) {
            return new LinkedHashSet<>(registeredClient.getScopes());
        }
        for (String requestedScope : authentication.getScopes()) {
            if (!registeredClient.getScopes().contains(requestedScope)) {
                throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_SCOPE);
            }
        }
        return new LinkedHashSet<>(authentication.getScopes());
    }

    private OAuth2AccessTokenAuthenticationToken generateAuthenticationToken(
            T resourceOwnerAuthentication,
            OAuth2ClientAuthenticationToken clientPrincipal,
            RegisteredClient registeredClient,
            Set<String> authorizedScopes,
            Authentication userAuthentication) {

        DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(userAuthentication)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .authorizedScopes(authorizedScopes)
                .authorizationGrantType(resourceOwnerAuthentication.getAuthorizationGrantType())
                .authorizationGrant(resourceOwnerAuthentication);

        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(userAuthentication.getName())
                .authorizationGrantType(resourceOwnerAuthentication.getAuthorizationGrantType())
                .authorizedScopes(authorizedScopes)
                .attribute(Principal.class.getName(), userAuthentication);

        OAuth2TokenContext tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();
        OAuth2Token generatedAccessToken = tokenGenerator.generate(tokenContext);
        if (generatedAccessToken == null) {
            OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                    "The token generator failed to generate the access token.", ERROR_URI);
            throw new OAuth2AuthenticationException(error);
        }

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                generatedAccessToken.getTokenValue(), generatedAccessToken.getIssuedAt(),
                generatedAccessToken.getExpiresAt(), tokenContext.getAuthorizedScopes());
        if (generatedAccessToken instanceof ClaimAccessor claimAccessor) {
            authorizationBuilder.token(accessToken,
                    metadata -> metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, claimAccessor.getClaims()));
        } else {
            authorizationBuilder.accessToken(accessToken);
        }

        OAuth2RefreshToken refreshToken = null;
        if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)
                && !ClientAuthenticationMethod.NONE.equals(clientPrincipal.getClientAuthenticationMethod())) {
            OAuth2Token refreshTokenValue = tokenGenerator.generate(
                    tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build());
            if (!(refreshTokenValue instanceof OAuth2RefreshToken)) {
                OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                        "The token generator failed to generate the refresh token.", ERROR_URI);
                throw new OAuth2AuthenticationException(error);
            }
            refreshToken = (OAuth2RefreshToken) refreshTokenValue;
            authorizationBuilder.refreshToken(refreshToken);
        }

        OAuth2Authorization authorization = authorizationBuilder.build();
        authorizationService.save(authorization);

        return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal,
                accessToken, refreshToken, Objects.requireNonNull(authorization.getAccessToken().getClaims()));
    }

    private OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OAuth2ClientAuthenticationToken clientPrincipal
                && clientPrincipal.isAuthenticated()) {
            return clientPrincipal;
        }
        throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
    }
}
