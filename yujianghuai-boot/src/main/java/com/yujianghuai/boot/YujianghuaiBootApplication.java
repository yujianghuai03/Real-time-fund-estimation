package com.yujianghuai.boot;

import com.yujianghuai.auth.config.AuthProperties;
import com.yujianghuai.common.tenant.TenantProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(
        scanBasePackages = "com.yujianghuai",
        exclude = UserDetailsServiceAutoConfiguration.class
)
@MapperScan("com.yujianghuai")
@EnableConfigurationProperties({AuthProperties.class, TenantProperties.class})
public class YujianghuaiBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(YujianghuaiBootApplication.class, args);
    }
}
