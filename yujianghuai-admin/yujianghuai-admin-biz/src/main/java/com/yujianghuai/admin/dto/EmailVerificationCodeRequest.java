package com.yujianghuai.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 邮箱验证码发送请求参数。
 */
@Data
@Schema(description = "邮箱验证码发送请求参数")
public class EmailVerificationCodeRequest {

    /**
     * 收件人邮箱。
     */
    @NotBlank(message = "收件人邮箱不能为空")
    @Email(message = "收件人邮箱格式不正确")
    @Schema(description = "收件人邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "test@example.com")
    private String email;
}
