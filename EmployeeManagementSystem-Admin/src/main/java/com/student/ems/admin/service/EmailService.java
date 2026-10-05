package com.student.ems.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:YOUR_EMAIL}")
    private String fromEmail;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendLeaveDecisionEmail(String toEmail, String employeeName, String leaveType,
                                       String fromDate, String toDate, String status, String adminNote) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            logger.info("No email address provided for employee {}, skipping email.", employeeName);
            return;
        }

        // Check if user still has placeholder configured
        if (fromEmail == null || fromEmail.contains("YOUR_EMAIL") || mailSender == null) {
            logger.info("Spring Mail placeholder configured ('{}'). In-app notification created. " +
                        "Skipping actual SMTP dispatch. Set real credentials in application.properties to enable live email delivery.", fromEmail);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail.trim());
            message.setSubject("EMS: Leave Request Update - " + status.toUpperCase());

            StringBuilder body = new StringBuilder();
            body.append("Dear ").append(employeeName != null ? employeeName : "Employee").append(",\n\n");
            body.append("Your ").append(leaveType).append(" leave request from ")
                .append(fromDate).append(" to ").append(toDate)
                .append(" has been ").append(status.toUpperCase()).append(".\n\n");

            if (adminNote != null && !adminNote.trim().isEmpty()) {
                body.append("Admin Note: ").append(adminNote.trim()).append("\n\n");
            }

            body.append("Please log in to your Employee Portal to view details.\n\n");
            body.append("Best regards,\nEmployee Management System");

            message.setText(body.toString());
            mailSender.send(message);
            logger.info("Leave notification email sent successfully to {}", toEmail);
        } catch (Exception e) {
            logger.warn("Could not dispatch email to {} (SMTP error: {}). In-app notification is still active.",
                    toEmail, e.getMessage());
        }
    }
}
