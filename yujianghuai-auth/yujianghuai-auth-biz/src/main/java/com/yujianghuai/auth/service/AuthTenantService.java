package com.yujianghuai.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yujianghuai.admin.entity.SysTenant;
import com.yujianghuai.admin.mapper.SysTenantMapper;
import com.yujianghuai.auth.model.TenantOptionVO;
import com.yujianghuai.common.exception.BizException;
import com.yujianghuai.common.tenant.TenantContext;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthTenantService {

    private final SysTenantMapper tenantMapper;

    public AuthTenantService(SysTenantMapper tenantMapper) {
        this.tenantMapper = tenantMapper;
    }

    public List<TenantOptionVO> listTenants() {
        return tenantMapper.selectList(new LambdaQueryWrapper<SysTenant>()
                        .eq(SysTenant::getStatus, 1)
                        .orderByAsc(SysTenant::getId))
                .stream()
                .map(this::toOption)
                .toList();
    }

    public SysTenant resolveTenantById(Long tenantId) {
        if (tenantId == null) {
            throw new BizException(400, "tenant is required");
        }
        SysTenant tenant = tenantMapper.selectOne(new LambdaQueryWrapper<SysTenant>()
                .eq(SysTenant::getId, tenantId)
                .eq(SysTenant::getStatus, 1)
                .last("limit 1"));
        if (tenant == null) {
            throw new BizException(400, "tenant is invalid");
        }
        return tenant;
    }

    public SysTenant resolveTenantByIdentifier(String tenantIdentifier) {
        String value = StringUtils.hasText(tenantIdentifier) ? tenantIdentifier.trim() : "1";
        LambdaQueryWrapper<SysTenant> query = new LambdaQueryWrapper<SysTenant>()
                .eq(SysTenant::getStatus, 1)
                .last("limit 1");
        if (value.chars().allMatch(Character::isDigit)) {
            query.eq(SysTenant::getId, Long.parseLong(value));
        } else {
            query.eq(SysTenant::getTenantCode, value);
        }
        SysTenant tenant = tenantMapper.selectOne(query);
        if (tenant == null) {
            throw new BizException(400, "tenant is invalid");
        }
        return tenant;
    }

    public String resolveTenantName(Object tenantIdentifier) {
        if (tenantIdentifier == null) {
            return null;
        }
        try {
            return resolveTenantByIdentifier(String.valueOf(tenantIdentifier)).getTenantName();
        } catch (RuntimeException exception) {
            return null;
        }
    }

    public <T> T runWithTenantContext(Long tenantId, Supplier<T> supplier) {
        String previousTenantId = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(String.valueOf(tenantId));
            return supplier.get();
        } finally {
            if (StringUtils.hasText(previousTenantId)) {
                TenantContext.setTenantId(previousTenantId);
            } else {
                TenantContext.clear();
            }
        }
    }

    private TenantOptionVO toOption(SysTenant tenant) {
        TenantOptionVO option = new TenantOptionVO();
        option.setId(tenant.getId());
        option.setTenantCode(tenant.getTenantCode());
        option.setTenantName(tenant.getTenantName());
        return option;
    }
}
