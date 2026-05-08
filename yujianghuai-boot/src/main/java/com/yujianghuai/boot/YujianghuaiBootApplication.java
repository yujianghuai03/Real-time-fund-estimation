package com.yujianghuai.boot;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yujianghuai.common.tenant.TenantProperties;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(
        scanBasePackages = "com.yujianghuai",
        exclude = UserDetailsServiceAutoConfiguration.class
)
@MapperScan(
        basePackages = {
                "com.yujianghuai.auth.mapper",
                "com.yujianghuai.admin.mapper",
                "com.yujianghuai.fund.mapper"
        },
        annotationClass = Mapper.class,
        markerInterface = BaseMapper.class
)
@EnableConfigurationProperties(TenantProperties.class)
public class YujianghuaiBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(YujianghuaiBootApplication.class, args);
    }
}
