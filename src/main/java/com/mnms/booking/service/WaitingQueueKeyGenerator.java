package com.mnms.booking.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class WaitingQueueKeyGenerator {

    private static final String WAITING_QUEUE_KEY = "waiting_queue";
    private static final String BOOKING_USERS_SET_KEY = "booking_users";
    private static final String NOTIFICATION_CHANNEL = "waiting_notification";

    public String getWaitingQueueKey(String festivalId, LocalDateTime reservationDate) {
        String dateStr = reservationDate.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        return WAITING_QUEUE_KEY + ":" + festivalId + ":" + dateStr;
    }

    public String getBookingUsersKey(String festivalId, LocalDateTime reservationDate) {
        String dateStr = reservationDate.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        return BOOKING_USERS_SET_KEY + ":" + festivalId + ":" + dateStr;
    }

    public String getNotificationChannelKey(String festivalId, LocalDateTime reservationDate) {
        String dateStr = reservationDate.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        return NOTIFICATION_CHANNEL + "/" + festivalId + "/" + dateStr;
    }
}