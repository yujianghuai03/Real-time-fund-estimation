package com.yujianghuai.common.tenant;

public final class TenantContext {

    private static final ThreadLocal<String> TENANT_HOLDER = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(String tenantId) {
        TENANT_HOLDER.set(tenantId);
    }

    public static String getTenantId() {
        return TENANT_HOLDER.get();
    }

    public static String getRequiredTenantId() {
        String tenantId = getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalStateException("TENANT-ID is missing");
        }
        return tenantId;
    }

    public static void clear() {
        TENANT_HOLDER.remove();
    }
}
