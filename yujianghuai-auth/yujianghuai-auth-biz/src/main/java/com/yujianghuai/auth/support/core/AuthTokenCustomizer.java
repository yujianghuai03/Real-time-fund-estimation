package com.yujianghuai.auth.support.core;

import com.yujianghuai.auth.support.base.OAuth2ResourceOwnerBaseAuthenticationToken;
import com.yujianghuai.common.utils.CurrentUserInfo;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
            if (userDetails instanceof LoginUserDetails loginUserDetails) {
                context.getClaims().claim("current_user", toCurrentUserClaim(loginUserDetails.getCurrentUserInfo()));
            }
        }

        if (context.getAuthorizationGrant() instanceof OAuth2ResourceOwnerBaseAuthenticationToken grantAuthentication) {
            Object tenantId = grantAuthentication.getAdditionalParameters().get(PARAM_TENANT_ID);
            if (tenantId != null && !tenantId.toString().isBlank()) {
                context.getClaims().claim(CLAIM_TENANT_ID, tenantId.toString());
            }
        }
    }

    private Map<String, Object> toCurrentUserClaim(CurrentUserInfo currentUserInfo) {
        Map<String, Object> claim = new LinkedHashMap<>();
        claim.put("id", currentUserInfo.getId());
        claim.put("username", currentUserInfo.getUsername());
        claim.put("nickname", currentUserInfo.getNickname());
        claim.put("realName", currentUserInfo.getRealName());
        claim.put("tenant", currentUserInfo.getTenant());
        return claim;
    }
}
