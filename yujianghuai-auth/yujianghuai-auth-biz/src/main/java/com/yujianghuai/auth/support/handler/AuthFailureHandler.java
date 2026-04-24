package com.yujianghuai.auth.support.handler;

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
        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            String description = oauth2Exception.getError().getDescription();
            String message = StringUtils.hasText(description)
                    ? description
                    : oauth2Exception.getError().getErrorCode();
            ERROR_RESPONSE_CONVERTER.write(R.fail(401, message), MediaType.APPLICATION_JSON, httpResponse);
            return;
        }
        ERROR_RESPONSE_CONVERTER.write(R.fail(401, exception.getMessage()), MediaType.APPLICATION_JSON, httpResponse);
    }
}
