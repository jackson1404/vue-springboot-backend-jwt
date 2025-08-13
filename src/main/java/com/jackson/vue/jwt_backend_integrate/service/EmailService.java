package com.jackson.vue.jwt_backend_integrate.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(String to, String resetLink){
        String subject = "Password Reset Request";
        String htmlContent = buildResetEmailTemplate(resetLink);
        String plainText = "You requested a password reset. Click the link below:\n " + resetLink;

        sendHtmlEmail(to, subject, htmlContent, plainText);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent, String plainText) {
        try {

            //used mime message for multipart/alternative container (HTML + plaintext)
            MimeMessage mailMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mailMessage);
            helper.setTo(to);
            helper.setSubject(subject);
            mailMessage.setText(plainText, htmlContent);

            mailSender.send(mailMessage);

        } catch (Exception e){
            throw new RuntimeException("Failed to send email: " + e);
        }
    }

    private String buildResetEmailTemplate(String resetLink) {
        return """
                <html>
                    <body style="font-family: Arial, sans-serif; color: #333;">
                        <h2>Password Reset Request</h2>
                        <p>You requested to reset your password. Click the button below to proceed:</p>
                        <p>
                            <a href="%s" style="display: inline-block; padding: 10px 20px;
                                color: white; background-color: #4CAF50; text-decoration: none;
                                border-radius: 5px;">
                                Reset Password
                            </a>
                        </p>
                        <p>If you didn’t request this, please ignore this email.</p>
                        <hr/>
                        <small>This link will expire in 15 minutes for security reasons.</small>
                    </body>
                </html>
                """.formatted(resetLink);
    }

}
