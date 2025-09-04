package com.mnms.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);           // 수신자
        message.setSubject(subject); // 제목
        message.setText(text);       // 내용
        message.setFrom("your_email@gmail.com"); // 발신자 (설정 가능)

        mailSender.send(message);
    }

    public static String loadTemplate(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}