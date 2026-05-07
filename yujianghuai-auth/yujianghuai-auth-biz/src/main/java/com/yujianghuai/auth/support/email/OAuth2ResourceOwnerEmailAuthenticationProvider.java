package com.yujianghuai.auth.support.email;

import com.yujianghuai.auth.service.AuthUserDetailsService;
import com.yujianghuai.auth.support.base.OAuth2ResourceOwnerBaseAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

public class OAuth2ResourceOwnerEmailAuthenticationProvider
        extends OAuth2ResourceOwnerBaseAuthenticationProvider<OAuth2ResourceOwnerEmailAuthenticationToken> {

    public static final String GRANT_TYPE = "email_code";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_CODE = "code";
    public static final AuthorizationGrantType EMAIL_CODE = new AuthorizationGrantType(GRANT_TYPE);

    private final AuthUserDetailsService userDetailsService;
    private final EmailCodeAuthenticationService emailCodeAuthenticationService;

    public OAuth2ResourceOwnerEmailAuthenticationProvider(AuthenticationManager authenticationManager,
                                                         OAuth2AuthorizationService authorizationService,
                                                         OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
                                                         AuthUserDetailsService userDetailsService,
                                                         EmailCodeAuthenticationService emailCodeAuthenticationService) {
        super(authenticationManager, authorizationService, tokenGenerator);
        this.userDetailsService = userDetailsService;
        this.emailCodeAuthenticationService = emailCodeAuthenticationService;
    }

    @Override
    public UsernamePasswordAuthenticationToken buildToken(OAuth2ResourceOwnerEmailAuthenticationToken authentication) {
        String email = (String) authentication.getAdditionalParameters().get(PARAM_EMAIL);
        String code = (String) authentication.getAdditionalParameters().get(PARAM_CODE);
        return UsernamePasswordAuthenticationToken.unauthenticated(email, code);
    }

    @Override
    protected Authentication authenticateUser(OAuth2ResourceOwnerEmailAuthenticationToken authentication) {
        String email = (String) authentication.getAdditionalParameters().get(PARAM_EMAIL);
        String code = (String) authentication.getAdditionalParameters().get(PARAM_CODE);
        emailCodeAuthenticationService.validate(email, code);
        UserDetails userDetails = userDetailsService.loadUserByEmail(email.trim());
        return UsernamePasswordAuthenticationToken.authenticated(
                userDetails, null, userDetails.getAuthorities());
    }

    @Override
    public void checkClient(RegisteredClient registeredClient) {
        if (!registeredClient.getAuthorizationGrantTypes().contains(EMAIL_CODE)) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2ResourceOwnerEmailAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
