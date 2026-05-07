package com.yujianghuai.common.email;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.yujianghuai.common.exception.BusinessException;
import com.yujianghuai.common.exception.ErrorCode;
import freemarker.template.Template;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StringUtils;

/**
 * FreeMarker 邮件发送服务实现。
 */
@Slf4j
@RequiredArgsConstructor
class EmailServiceImpl implements EmailService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JavaMailSender javaMailSender;
    private final freemarker.template.Configuration freemarkerConfiguration;
    private final EmailProperties emailProperties;

    @Override
    public void sendVerificationCode(String targetEmail, String code) {
        validateSendParameter(targetEmail, code);
        try {
            String html = renderVerificationTemplate(targetEmail, code);
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    emailProperties.getDefaultEncoding()
            );
            helper.setFrom(emailProperties.getUsername(), emailProperties.getFromName());
            helper.setTo(targetEmail);
            helper.setSubject(emailProperties.getVerificationSubject());
            helper.setText(html, true);
            javaMailSender.send(message);
            log.info("验证码邮件发送成功，targetEmail={}", targetEmail);
        } catch (Exception ex) {
            log.error("验证码邮件发送失败，targetEmail={}", targetEmail, ex);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码邮件发送失败，请稍后重试");
        }
    }

    private String renderVerificationTemplate(String targetEmail, String code) throws Exception {
        Template template = freemarkerConfiguration.getTemplate(emailProperties.getTemplatePath());
        Map<String, Object> model = new HashMap<>();
        model.put("type", "邮箱验证码");
        model.put("time", LocalDateTime.now().format(TIME_FORMATTER));
        model.put("email", targetEmail);
        model.put("code", code);
        model.put("content", "您的验证码为：" + code + "，有效期为10分钟。若非本人操作，请忽略此邮件。");
        model.put("year", Year.now().getValue());

        StringWriter writer = new StringWriter();
        template.process(model, writer);
        return writer.toString();
    }

    private void validateSendParameter(String targetEmail, String code) {
        if (!StringUtils.hasText(targetEmail)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "收件人邮箱不能为空");
        }
        if (!StringUtils.hasText(code)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码不能为空");
        }
        if (!StringUtils.hasText(emailProperties.getHost())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "邮件 SMTP 服务器未配置");
        }
        if (!StringUtils.hasText(emailProperties.getUsername())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发件人邮箱未配置");
        }
        if (!StringUtils.hasText(emailProperties.getPassword())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发件人邮箱密码未配置");
        }
    }
}
