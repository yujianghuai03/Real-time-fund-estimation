package com.yujianghuai.auth.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "用户注册请求")
public class RegisterRequest {

    @NotNull
    @Schema(description = "租户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long tenantId;

    @NotBlank
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank
    @Schema(description = "昵称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nickname;

    @NotBlank
    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "邮箱")
    private String email;
}
