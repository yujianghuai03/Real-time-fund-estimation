package com.yujianghuai.common.tenant;

import com.yujianghuai.common.exception.BizException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TenantWebFilter extends OncePerRequestFilter {

    private final TenantProperties tenantProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public TenantWebFilter(TenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        return tenantProperties.getIgnorePaths().stream()
                .anyMatch(pattern -> antPathMatcher.matchStart(pattern, requestUri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String tenantId = request.getHeader(tenantProperties.getHeaderName());
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = request.getParameter(tenantProperties.getHeaderName());
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException(400, tenantProperties.getHeaderName() + " header is required");
        }

        try {
            TenantContext.setTenantId(tenantId);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
