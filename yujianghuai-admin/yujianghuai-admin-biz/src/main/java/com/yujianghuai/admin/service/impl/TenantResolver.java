package com.yujianghuai.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yujianghuai.admin.entity.SysTenant;
import com.yujianghuai.admin.mapper.SysTenantMapper;
import com.yujianghuai.common.exception.BizException;
import com.yujianghuai.common.tenant.TenantContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TenantResolver {

    private final SysTenantMapper tenantMapper;

    public TenantResolver(SysTenantMapper tenantMapper) {
        this.tenantMapper = tenantMapper;
    }

    public SysTenant currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (!StringUtils.hasText(tenantId)) {
            tenantId = "1";
        }
        LambdaQueryWrapper<SysTenant> query = new LambdaQueryWrapper<SysTenant>()
                .eq(SysTenant::getStatus, 1);
        if (tenantId.chars().allMatch(Character::isDigit)) {
            query.eq(SysTenant::getId, Long.parseLong(tenantId));
        } else {
            query.eq(SysTenant::getTenantCode, tenantId);
        }
        SysTenant tenant = tenantMapper.selectOne(query);
        if (tenant == null) {
            throw new BizException(400, "tenant is invalid: " + tenantId);
        }
        return tenant;
    }

    public Long currentTenantId() {
        return currentTenant().getId();
    }
}
