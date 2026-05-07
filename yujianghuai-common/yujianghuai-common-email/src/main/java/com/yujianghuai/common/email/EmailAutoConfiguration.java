package com.yujianghuai.common.email;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import freemarker.template.TemplateExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * 邮件模块自动配置。
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(EmailProperties.class)
public class EmailAutoConfiguration {

    private final EmailProperties emailProperties;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(emailProperties.getHost());
        sender.setPort(emailProperties.getPort());
        sender.setUsername(emailProperties.getUsername());
        sender.setPassword(emailProperties.getPassword());
        sender.setProtocol(emailProperties.getProtocol());
        sender.setDefaultEncoding(emailProperties.getDefaultEncoding());

        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.smtp.auth", String.valueOf(emailProperties.getAuth()));
        properties.put("mail.smtp.ssl.enable", String.valueOf(emailProperties.getSslEnable()));
        properties.put("mail.smtp.connectiontimeout", "5000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");
        return sender;
    }

    @Bean
    public freemarker.template.Configuration emailFreemarkerConfiguration() {
        freemarker.template.Configuration configuration =
                new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_32);
        configuration.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "templates");
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setWrapUncheckedExceptions(true);
        return configuration;
    }

    @Bean
    public EmailService emailService(JavaMailSender javaMailSender,
                                     StringRedisTemplate stringRedisTemplate,
                                     freemarker.template.Configuration emailFreemarkerConfiguration) {
        return new EmailServiceImpl(javaMailSender, stringRedisTemplate, emailFreemarkerConfiguration, emailProperties);
    }
}
