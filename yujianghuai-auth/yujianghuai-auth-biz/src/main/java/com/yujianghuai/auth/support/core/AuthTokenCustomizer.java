package com.yujianghuai.auth.support.core;

import com.yujianghuai.auth.support.base.OAuth2ResourceOwnerBaseAuthenticationToken;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    public static final String CLAIM_CLIENT_ID = "client_id";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_AUTHORITIES = "authorities";
    public static final String CLAIM_TENANT_ID = "tenant_id";
    public static final String PARAM_TENANT_ID = "TENANT-ID";

    @Override
    public void customize(JwtEncodingContext context) {
        context.getClaims().claim(CLAIM_CLIENT_ID, context.getRegisteredClient().getClientId());

        if (context.getPrincipal() != null
                && context.getPrincipal().getPrincipal() instanceof UserDetails userDetails) {
            List<String> authorities = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            context.getClaims()
                    .claim(CLAIM_USERNAME, userDetails.getUsername())
                    .claim(CLAIM_AUTHORITIES, authorities);
        }

        if (context.getAuthorizationGrant() instanceof OAuth2ResourceOwnerBaseAuthenticationToken grantAuthentication) {
            Object tenantId = grantAuthentication.getAdditionalParameters().get(PARAM_TENANT_ID);
            if (tenantId != null && !tenantId.toString().isBlank()) {
                context.getClaims().claim(CLAIM_TENANT_ID, tenantId.toString());
            }
        }
    }
}
