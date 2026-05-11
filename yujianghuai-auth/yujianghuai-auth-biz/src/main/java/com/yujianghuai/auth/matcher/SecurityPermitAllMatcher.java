package com.yujianghuai.auth.matcher;

import com.yujianghuai.auth.config.SecurityPermitAllProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.AntPathMatcher;

import java.util.Collections;
import java.util.List;

/**
 * @author yxh
 * @date 2026/5/11 15:19
 */
public class SecurityPermitAllMatcher {

    private final SecurityPermitAllProperties properties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public SecurityPermitAllMatcher(SecurityPermitAllProperties properties) {
        this.properties = properties;
    }

    public boolean matches(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        if (matches(properties.getPaths(), requestUri)) {
            return true;
        }

        List<String> methodPaths = properties.getMethods()
                .getOrDefault(method.toUpperCase(), Collections.emptyList());

        return matches(methodPaths, requestUri);
    }

    private boolean matches(List<String> patterns, String requestUri) {
        return patterns != null && patterns.stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, requestUri));
    }
}