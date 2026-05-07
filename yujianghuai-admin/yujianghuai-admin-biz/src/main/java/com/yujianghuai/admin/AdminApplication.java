package com.yujianghuai.admin;

import com.yujianghuai.common.tenant.TenantProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "com.yujianghuai.admin",
        "com.yujianghuai.common"
})
@MapperScan("com.yujianghuai.admin.mapper")
@EnableConfigurationProperties(TenantProperties.class)
@EnableFeignClients
public class AdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
