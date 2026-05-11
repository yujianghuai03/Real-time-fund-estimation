package com.yujianghuai.auth.filter;

import com.yujianghuai.common.constant.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author yxh
 * @date 2026/5/11 14:21
 */
@Component
@RequiredArgsConstructor
public class TokenRedisValidationFilter  extends OncePerRequestFilter {
    private final OAuth2AuthorizationService authorizationService;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            String tokenValue = jwtAuthentication.getToken().getTokenValue();

            OAuth2Authorization authorization = authorizationService.findByToken(
                    tokenValue,
                    OAuth2TokenType.ACCESS_TOKEN
            );

            if (authorization == null) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"code\":401,\"message\":\""+ SecurityConstants.AUTH_TOKEN_INVALID_MESSAGE+"\",\"data\":null}");
                return;
            }
        }
    }
}