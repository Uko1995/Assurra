package com.uko.eaas.identity.service.impl;

import com.uko.eaas.identity.model.entity.User;
import com.uko.eaas.identity.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.application.name:EaaS}")
    private String appName;

    @Value("${app.email.from:noreply@eaas.africa}")
    private String fromEmail;

    @Override
    @Async
    public void sendVerificationEmail(User user, String verificationToken) {
        try {
            String verificationUrl = frontendUrl + "/verify-email?token=" + verificationToken;
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Verify your email - " + appName);
            message.setText(String.format(
                "Hi %s,%n%n" +
                "Welcome to %s! Please verify your email address by clicking the link below:%n%n" +
                "%s%n%n" +
                "This link will expire in 24 hours.%n%n" +
                "If you didn't create an account, please ignore this email.%n%n" +
                "Best regards,%n" +
                "The %s Team",
                user.getFullName(),
                appName,
                verificationUrl,
                appName
            ));
            
            mailSender.send(message);
            log.info("Verification email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", user.getEmail(), e);
        }
    }

    @Override
    @Async
    public void sendWelcomeEmail(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Welcome to " + appName);
            message.setText(String.format(
                "Hi %s,%n%n" +
                "Welcome to %s! Your email has been successfully verified.%n%n" +
                "You can now start using our platform to make secure escrow transactions.%n%n" +
                "Best regards,%n" +
                "The %s Team",
                user.getFullName(),
                appName,
                appName
            ));
            
            mailSender.send(message);
            log.info("Welcome email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", user.getEmail(), e);
        }
    }

    @Override
    @Async
    public void sendKycApprovedEmail(User user, String businessName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("KYC Approved - " + appName);
            message.setText(String.format(
                "Hi %s,%n%n" +
                "Great news! Your KYC verification for '%s' has been approved.%n%n" +
                "You can now:%n" +
                "- Receive escrow payments%n" +
                "- Access your API keys%n" +
                "- Configure webhooks%n%n" +
                "Login to your dashboard to get started: %s/dashboard%n%n" +
                "Best regards,%n" +
                "The %s Team",
                user.getFullName(),
                businessName,
                frontendUrl,
                appName
            ));
            
            mailSender.send(message);
            log.info("KYC approved email sent to: {} (business: {})", user.getEmail(), businessName);
        } catch (Exception e) {
            log.error("Failed to send KYC approved email to: {}", user.getEmail(), e);
        }
    }

    @Override
    @Async
    public void sendKycRejectedEmail(User user, String businessName, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("KYC Requires Attention - " + appName);
            message.setText(String.format(
                "Hi %s,%n%n" +
                "Your KYC verification for '%s' requires attention.%n%n" +
                "Reason: %s%n%n" +
                "Please review the requirements and resubmit your documents:%n" +
                "%s/kyc-resubmit%n%n" +
                "If you have any questions, please contact our support team.%n%n" +
                "Best regards,%n" +
                "The %s Team",
                user.getFullName(),
                businessName,
                reason,
                frontendUrl,
                appName
            ));
            
            mailSender.send(message);
            log.info("KYC rejected email sent to: {} (business: {})", user.getEmail(), businessName);
        } catch (Exception e) {
            log.error("Failed to send KYC rejected email to: {}", user.getEmail(), e);
        }
    }

    @Override
    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Password Reset Request - " + appName);
            message.setText(String.format(
                "Hi %s,%n%n" +
                "We received a request to reset your password. Click the link below to set a new password:%n%n" +
                "%s%n%n" +
                "This link will expire in 1 hour.%n%n" +
                "If you didn't request this, please ignore this email or contact support if you have concerns.%n%n" +
                "Best regards,%n" +
                "The %s Team",
                user.getFullName(),
                resetUrl,
                appName
            ));
            
            mailSender.send(message);
            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
        }
    }

    @Override
    @Async
    public void sendApiKeyRegeneratedEmail(User user, String apiKeyPrefix) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("API Key Regenerated - " + appName);
            message.setText(String.format(
                "Hi %s,%n%n" +
                "Your API key has been regenerated for security purposes.%n%n" +
                "New API Key Prefix: %s%n%n" +
                "Your old API key has been invalidated immediately.%n" +
                "Please update your integrations with the new API key.%n%n" +
                "If you didn't request this change, please contact support immediately.%n%n" +
                "Best regards,%n" +
                "The %s Team",
                user.getFullName(),
                apiKeyPrefix,
                appName
            ));
            
            mailSender.send(message);
            log.info("API key regenerated email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send API key regenerated email to: {}", user.getEmail(), e);
        }
    }
}
