package com.yujianghuai.cloud.feign;

import com.yujianghuai.common.tenant.TenantContext;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignTenantConfig {

    @Bean
    public RequestInterceptor tenantRequestInterceptor() {
        return requestTemplate -> {
            String tenantId = TenantContext.getTenantId();
            if (tenantId != null && !tenantId.isBlank()) {
                requestTemplate.header("TENANT-ID", tenantId);
            }
        };
    }
}
