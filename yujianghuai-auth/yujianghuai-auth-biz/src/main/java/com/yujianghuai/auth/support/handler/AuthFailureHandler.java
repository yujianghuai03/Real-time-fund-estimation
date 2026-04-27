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
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class AuthFailureHandler implements AuthenticationFailureHandler {

    private static final String UNAUTHORIZED_MESSAGE = "权限不足，请登录后再试！";
    private static final MappingJackson2HttpMessageConverter ERROR_RESPONSE_CONVERTER =
            new MappingJackson2HttpMessageConverter();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        httpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
        ERROR_RESPONSE_CONVERTER.write(R.fail(401, UNAUTHORIZED_MESSAGE), MediaType.APPLICATION_JSON, httpResponse);
    }
}
