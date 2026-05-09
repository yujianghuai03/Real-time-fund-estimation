package com.yujianghuai.common.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 邮件发送配置。
 */
@Data
@ConfigurationProperties(prefix = "app.email")
public class EmailProperties {

    /**
     * SMTP 服务器地址。
     */
    private String host;

    /**
     * SMTP 服务器端口。
     */
    private Integer port = 465;

    /**
     * 发件人邮箱账号。
     */
    private String username;

    /**
     * 发件人邮箱密码或授权码。
     */
    private String password;

    /**
     * 发件人展示名称。
     */
    private String fromName = "Yujianghuai";

    /**
     * 验证码邮件主题。
     */
    private String verificationSubject = "邮箱验证码";

    /**
     * 协议。
     */
    private String protocol = "smtp";

    /**
     * 编码。
     */
    private String defaultEncoding = "UTF-8";

    /**
     * 是否启用 SMTP 认证。
     */
    private Boolean auth = true;

    /**
     * 是否启用 SSL。
     */
    private Boolean sslEnable = true;

    /**
     * FreeMarker 模板路径。
     */
    private String templatePath = "email/email.ftl";


    /**
     * 验证码发送限制配置。
     */
    private VerificationCodeLimit verificationCodeLimit = new VerificationCodeLimit();

    /**
     * 验证码发送限制配置。
     */
    @Data
    public static class VerificationCodeLimit {

        /**
         * 是否启用 IP 限流。
         */
        private Boolean ipEnabled = true;

        /**
         * 同一 IP 每分钟最多发送次数。
         */
        private Integer ipMinuteLimit = 5;

        /**
         * 同一 IP 每小时最多发送次数。
         */
        private Integer ipHourLimit = 30;

        /**
         * 可信代理IP列表。
         * 只有请求来自这些代理时，才信任 X-Forwarded-For 或 X-Real-IP。
         */
        private List<String> trustedProxies = new ArrayList<>();
    }
}
