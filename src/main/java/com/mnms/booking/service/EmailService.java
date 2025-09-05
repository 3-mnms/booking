package com.mnms.booking.service;

import com.mnms.booking.dto.response.BookingUserResponseDTO;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.enums.TicketType;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendTicketConfirmationEmail(Ticket ticket, BookingUserResponseDTO user) {
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("templates/email/ticket-confirmation.txt")) {

            if (is == null) {
                throw new BusinessException(ErrorCode.TICKET_EMAIL_TEMPLATE_NOT_FOUND);
            }

            String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년MM월dd일 a h시");

            String content = String.format(
                    template,
                    user.getName(),
                    ticket.getReservationNumber(),
                    ticket.getFestival().getFname(),
                    ticket.getPerformanceDate().format(formatter),
                    ticket.getFestival().getFcltynm(),
                    ticket.getFestival().getTicketPrice() * ticket.getSelectedTicketCount(),
                    ticket.getDeliveryMethod() == TicketType.MOBILE ? "모바일" : "지류"
            );

            String subject = String.format("[예매확인] %s 티켓", ticket.getFestival().getFname());
            sendEmail(user.getEmail(), subject, content);

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.TICKET_EMAIL_TEMPLATE_NOT_FOUND);
        }
    }

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);           // 수신자
        message.setSubject(subject); // 제목
        message.setText(text);       // 내용

        mailSender.send(message);
    }

    public static String loadTemplate(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}