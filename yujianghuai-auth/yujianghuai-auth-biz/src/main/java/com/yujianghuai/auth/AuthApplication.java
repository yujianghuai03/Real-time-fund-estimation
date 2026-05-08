package com.yujianghuai.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yujianghuai.common.tenant.TenantProperties;
import org.apache.ibatis.annotations.Mapper;
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
@MapperScan(
        basePackages = {
                "com.yujianghuai.auth.mapper",
                "com.yujianghuai.admin.mapper"
        },
        annotationClass = Mapper.class,
        markerInterface = BaseMapper.class
)
@EnableConfigurationProperties(TenantProperties.class)
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
