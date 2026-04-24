package com.yujianghuai.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String AUTHORIZATION_SCHEME = "Authorization";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("鱼江槐后端接口文档")
                        .description("统一的后端 OpenAPI 文档，支持通过 swagger-ui/index.html 在线调试接口")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(AUTHORIZATION_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(AUTHORIZATION_SCHEME, new SecurityScheme()
                                .name(AUTHORIZATION_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
