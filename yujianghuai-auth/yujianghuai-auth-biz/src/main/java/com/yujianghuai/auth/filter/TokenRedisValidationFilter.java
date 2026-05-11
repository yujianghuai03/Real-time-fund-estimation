package com.yujianghuai.auth.filter;

import com.yujianghuai.auth.matcher.SecurityPermitAllMatcher;
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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author yxh
 * @date 2026/5/11 14:21
 */

@RequiredArgsConstructor
public class TokenRedisValidationFilter extends OncePerRequestFilter {
    private final OAuth2AuthorizationService authorizationService;

    private final SecurityPermitAllMatcher permitAllMatcher;
    /**
     * 判断当前请求是否跳过该过滤器。
     *
     * <p>
     * 如果请求路径命中 permit-all 白名单，则不执行 token Redis 校验逻辑。
     * </p>
     *
     * @param request 当前 HTTP 请求
     * @return true 表示跳过过滤器，false 表示继续执行过滤器
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return permitAllMatcher.matches(request);
    }
    /**
     * 执行 token 二次校验。
     *
     * <p>
     * 当 Spring Security 上下文中已经存在 JwtAuthenticationToken 时，
     * 说明当前请求已经通过 JWT 解析。此时再拿到原始 tokenValue，
     * 到 OAuth2AuthorizationService 中查询服务端是否仍保存该 token。
     * </p>
     *
     * <p>
     * 如果查询不到授权记录，说明该 token 已经无效，
     * 直接返回 401，不再继续执行后续过滤器链。
     * </p>
     *
     * @param request 当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param filterChain 过滤器链
     */
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
        filterChain.doFilter(request, response);
    }
}