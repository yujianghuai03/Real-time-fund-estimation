package com.yujianghuai.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.yujianghuai.common.tenant.TenantContext;
import com.yujianghuai.common.tenant.TenantTable;
import com.yujianghuai.common.tenant.TenantProperties;
import java.util.HashSet;
import java.util.Set;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import com.baomidou.mybatisplus.annotation.TableName;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(TenantProperties tenantProperties) {
        Set<String> ignoreTables = new HashSet<>(tenantProperties.getIgnoreTables());
        ignoreTables.addAll(resolveNonTenantTables());

        TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                String tenantId = TenantContext.getRequiredTenantId();
                try {
                    return new LongValue(Long.parseLong(tenantId));
                } catch (NumberFormatException ex) {
                    throw new IllegalStateException("TENANT-ID must be a numeric tenant id: " + tenantId, ex);
                }
            }

            @Override
            public String getTenantIdColumn() {
                return "tenant_id";
            }

            @Override
            public boolean ignoreTable(String tableName) {
                return ignoreTables.contains(tableName);
            }
        });

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(tenantInterceptor);
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    private Set<String> resolveNonTenantTables() {
        Set<String> ignoreTables = new HashSet<>();
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(TenantTable.class));
        for (BeanDefinition beanDefinition : scanner.findCandidateComponents("com.yujianghuai")) {
            Class<?> entityClass = resolveClass(beanDefinition.getBeanClassName());
            if (entityClass == null) {
                continue;
            }
            TenantTable tenantTable = entityClass.getAnnotation(TenantTable.class);
            TableName tableName = entityClass.getAnnotation(TableName.class);
            if (tenantTable != null && !tenantTable.value() && tableName != null && StringUtils.hasText(tableName.value())) {
                ignoreTables.add(tableName.value());
            }
        }
        return ignoreTables;
    }

    private Class<?> resolveClass(String className) {
        try {
            return ClassUtils.forName(className, getClass().getClassLoader());
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
}
