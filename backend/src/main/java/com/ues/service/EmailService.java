package com.ues.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LogManager.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendRegistrationApproved(String email, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Registration Approved - UES Event Finder");
            message.setText("Dear " + firstName + ",\n\nYour registration request has been approved. You can now log in to the UES Event Finder application.\n\nBest regards,\nUES Team");
            mailSender.send(message);
            logger.info("Registration approval email sent to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send registration approval email to {}: {}", email, e.getMessage());
        }
    }

    public void sendRegistrationRejected(String email, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Registration Request - UES Event Finder");
            message.setText("Dear " + firstName + ",\n\nWe regret to inform you that your registration request has been rejected. Please contact support for more information.\n\nBest regards,\nUES Team");
            mailSender.send(message);
            logger.info("Registration rejection email sent to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send registration rejection email to {}: {}", email, e.getMessage());
        }
    }

    public void sendPasswordChanged(String email, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Password Changed - UES Event Finder");
            message.setText("Dear " + firstName + ",\n\nYour password has been successfully changed. If you did not make this change, please contact support immediately.\n\nBest regards,\nUES Team");
            mailSender.send(message);
            logger.info("Password change notification email sent to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send password change email to {}: {}", email, e.getMessage());
        }
    }
}
