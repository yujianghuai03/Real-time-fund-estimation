package com.yujianghuai.auth.support.email;

import com.yujianghuai.auth.support.base.OAuth2ResourceOwnerBaseAuthenticationToken;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

public class OAuth2ResourceOwnerEmailAuthenticationToken extends OAuth2ResourceOwnerBaseAuthenticationToken {

    public OAuth2ResourceOwnerEmailAuthenticationToken(AuthorizationGrantType authorizationGrantType,
                                                      Authentication clientPrincipal,
                                                      Set<String> scopes,
                                                      Map<String, Object> additionalParameters) {
        super(authorizationGrantType, clientPrincipal, scopes, additionalParameters);
    }
}
