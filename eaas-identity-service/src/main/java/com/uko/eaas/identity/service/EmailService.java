package com.uko.eaas.identity.service;

import com.uko.eaas.identity.model.entity.User;

public interface EmailService {
    
    void sendVerificationEmail(User user, String verificationToken);
    
    void sendWelcomeEmail(User user);
    
    void sendKycApprovedEmail(User user, String businessName);
    
    void sendKycRejectedEmail(User user, String businessName, String reason);
    
    void sendPasswordResetEmail(User user, String resetToken);
    
    void sendApiKeyRegeneratedEmail(User user, String apiKeyPrefix);
}
