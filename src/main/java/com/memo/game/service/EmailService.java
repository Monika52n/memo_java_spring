package com.memo.game.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import java.util.UUID;
@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public boolean sendVerificationEmail(String email, UUID verificationToken) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject("Verification token");
        mailMessage.setText(verificationToken.toString());

        boolean isSent = false;
        try
        {
            javaMailSender.send(mailMessage);
            isSent = true;
        } catch (MailException e) {
            e.printStackTrace();
        }
        return isSent;
    }
}