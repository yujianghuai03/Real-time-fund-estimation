package com.yujianghuai.auth.support.core;

import com.yujianghuai.auth.support.email.OAuth2ResourceOwnerEmailAuthenticationProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

public class PublicClientCustomGrantAuthenticationConverter implements AuthenticationConverter {

    private static final Set<String> SUPPORTED_GRANT_TYPES = Set.of(
            AuthorizationGrantType.PASSWORD.getValue(),
            OAuth2ResourceOwnerEmailAuthenticationProvider.GRANT_TYPE
    );

    @Override
    public Authentication convert(HttpServletRequest request) {
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!SUPPORTED_GRANT_TYPES.contains(grantType)) {
            return null;
        }

        Map<String, String[]> parameters = request.getParameterMap();
        String[] clientIds = parameters.get(OAuth2ParameterNames.CLIENT_ID);
        if (clientIds == null || clientIds.length != 1 || !StringUtils.hasText(clientIds[0])) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);
        }

        return new OAuth2ClientAuthenticationToken(
                clientIds[0],
                ClientAuthenticationMethod.NONE,
                null,
                additionalParameters(parameters));
    }

    private Map<String, Object> additionalParameters(Map<String, String[]> parameters) {
        Map<String, Object> additionalParameters = new HashMap<>(parameters.size());
        parameters.forEach((key, values) -> {
            if (!OAuth2ParameterNames.CLIENT_ID.equals(key) && values != null && values.length > 0) {
                additionalParameters.put(key, values.length == 1 ? values[0] : values);
            }
        });
        return additionalParameters;
    }
}
