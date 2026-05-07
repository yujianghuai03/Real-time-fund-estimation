package com.yujianghuai.auth.support.email;

import com.yujianghuai.auth.support.base.OAuth2ResourceOwnerBaseAuthenticationConverter;
import com.yujianghuai.auth.support.core.OAuth2EndpointUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

public class OAuth2ResourceOwnerEmailAuthenticationConverter
        extends OAuth2ResourceOwnerBaseAuthenticationConverter<OAuth2ResourceOwnerEmailAuthenticationToken> {

    public static final AuthorizationGrantType EMAIL_CODE =
            new AuthorizationGrantType(OAuth2ResourceOwnerEmailAuthenticationProvider.GRANT_TYPE);

    @Override
    public boolean support(String grantType) {
        return OAuth2ResourceOwnerEmailAuthenticationProvider.GRANT_TYPE.equals(grantType);
    }

    @Override
    public OAuth2ResourceOwnerEmailAuthenticationToken buildToken(Authentication clientPrincipal,
                                                                 Set<String> requestedScopes,
                                                                 Map<String, Object> additionalParameters) {
        return new OAuth2ResourceOwnerEmailAuthenticationToken(EMAIL_CODE,
                clientPrincipal, requestedScopes, additionalParameters);
    }

    @Override
    public void checkParams(HttpServletRequest request) {
        MultiValueMap<String, String> parameters = OAuth2EndpointUtils.getParameters(request);
        checkSingleRequiredParameter(parameters, OAuth2ResourceOwnerEmailAuthenticationProvider.PARAM_EMAIL);
        checkSingleRequiredParameter(parameters, OAuth2ResourceOwnerEmailAuthenticationProvider.PARAM_CODE);
    }

    private void checkSingleRequiredParameter(MultiValueMap<String, String> parameters, String parameterName) {
        String value = parameters.getFirst(parameterName);
        if (!StringUtils.hasText(value) || parameters.get(parameterName).size() != 1) {
            OAuth2EndpointUtils.throwError(OAuth2ErrorCodes.INVALID_REQUEST, parameterName,
                    OAuth2EndpointUtils.ACCESS_TOKEN_REQUEST_ERROR_URI);
        }
    }
}
