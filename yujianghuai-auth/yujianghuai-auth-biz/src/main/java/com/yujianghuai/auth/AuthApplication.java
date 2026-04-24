package com.yujianghuai.auth;

import com.yujianghuai.auth.config.AuthProperties;
import com.yujianghuai.common.tenant.TenantProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(
        scanBasePackages = {
                "com.yujianghuai.auth",
                "com.yujianghuai.common"
        },
        exclude = UserDetailsServiceAutoConfiguration.class
)
@MapperScan("com.yujianghuai")
@EnableConfigurationProperties({AuthProperties.class, TenantProperties.class})
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
