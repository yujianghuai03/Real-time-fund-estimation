package com.yujianghuai.nacos;

import com.yujianghuai.nacos.config.NacosManagerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "com.yujianghuai.nacos")
@EnableConfigurationProperties(NacosManagerProperties.class)
public class NacosManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NacosManagerApplication.class, args);
    }
}
