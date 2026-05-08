package com.yujianghuai.auth.support.handler;

import com.yujianghuai.common.constant.SecurityConstants;
import com.yujianghuai.common.web.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AuthFailureHandler implements AuthenticationFailureHandler {

    private static final MappingJackson2HttpMessageConverter ERROR_RESPONSE_CONVERTER =
            new MappingJackson2HttpMessageConverter();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        httpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
        ERROR_RESPONSE_CONVERTER.write(R.fail(401, resolveMessage(exception)), MediaType.APPLICATION_JSON, httpResponse);
    }

    private String resolveMessage(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            String errorCode = oauth2Exception.getError().getErrorCode();
            String description = oauth2Exception.getError().getDescription();
            if (OAuth2ErrorCodes.INVALID_CLIENT.equals(errorCode)) {
                return SecurityConstants.AUTH_INVALID_CLIENT_MESSAGE;
            }
            if (OAuth2ErrorCodes.UNAUTHORIZED_CLIENT.equals(errorCode)) {
                return SecurityConstants.AUTH_UNAUTHORIZED_CLIENT_MESSAGE;
            }
            if (OAuth2ErrorCodes.INVALID_SCOPE.equals(errorCode)) {
                return SecurityConstants.AUTH_INVALID_SCOPE_MESSAGE;
            }
            if (OAuth2ErrorCodes.INVALID_REQUEST.equals(errorCode)) {
                return StringUtils.hasText(description)
                        ? description
                        : SecurityConstants.AUTH_INVALID_REQUEST_MESSAGE;
            }
            if (OAuth2ErrorCodes.INVALID_GRANT.equals(errorCode)) {
                return StringUtils.hasText(description)
                        ? description
                        : SecurityConstants.AUTH_INVALID_GRANT_MESSAGE;
            }
        }
        return StringUtils.hasText(exception.getMessage())
                ? exception.getMessage()
                : SecurityConstants.AUTH_UNAUTHORIZED_MESSAGE;
    }
}
