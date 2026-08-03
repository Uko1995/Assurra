package com.uko.eaas.identity.config;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.io.InputStream;

@Slf4j
@Configuration
@Profile("dev")
public class DevMailConfig {

    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender devMailSender(
            @Value("${app.email.from:[EMAIL_REDACTED]}") String fromEmail) {
        log.warn("=================================================================");
        log.warn("DEVELOPMENT MODE: Using NoOp JavaMailSender");
        log.warn("Emails will be logged but NOT sent.");
        log.warn("Set MAIL_USERNAME and MAIL_PASSWORD to enable real email sending.");
        log.warn("=================================================================");
        
        return new NoOpMailSender(fromEmail);
    }

    @Slf4j
    static class NoOpMailSender implements JavaMailSender {
        private final String fromEmail;

        public NoOpMailSender(String fromEmail) {
            this.fromEmail = fromEmail;
        }

        @Override
        public void send(SimpleMailMessage simpleMessage) throws MailException {
            log.info("[DEV MODE] Email would be sent:");
            log.info("  From: {}", fromEmail);
            log.info("  To: {}", String.join(", ", simpleMessage.getTo()));
            log.info("  Subject: {}", simpleMessage.getSubject());
            log.info("  Body: {}", simpleMessage.getText());
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) throws MailException {
            for (SimpleMailMessage msg : simpleMessages) {
                send(msg);
            }
        }

        @Override
        public MimeMessage createMimeMessage() {
            return null;
        }

        @Override
        public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
            return null;
        }

        @Override
        public void send(MimeMessage mimeMessage) throws MailException {
            log.info("[DEV MODE] MimeMessage would be sent");
        }

        @Override
        public void send(MimeMessage... mimeMessages) throws MailException {
            for (MimeMessage msg : mimeMessages) {
                send(msg);
            }
        }

        @Override
        public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
            log.info("[DEV MODE] MimeMessagePreparator would be sent");
        }

        @Override
        public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
            for (MimeMessagePreparator prep : mimeMessagePreparators) {
                send(prep);
            }
        }
    }
}
