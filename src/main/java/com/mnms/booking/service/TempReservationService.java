package com.mnms.booking.service;

import com.mnms.booking.entity.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TempReservationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PREFIX = "TEMP_RESERVATION:";

    @Value("${temp-reservation.ttl-minutes:1}")
    private long ttlMinutes;

    // 2차 예매하기 누르면 실행
    public void createTempReservation(Ticket ticket) {
        String key = PREFIX + ticket.getReservationNumber();
        redisTemplate.opsForValue().set(key, ticket, ttlMinutes, TimeUnit.MINUTES);
    }

    // 갱신 - refresh ttl 각자 설정
    public void refreshTempReservation(String reservationNumber, long ttlMinutes) {
        String key = PREFIX + reservationNumber;
        Boolean exists = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            redisTemplate.expire(key, ttlMinutes, TimeUnit.MINUTES);
        }
    }

    // 조회
    public Optional<Ticket> getTempReservation(String reservationNumber) {
        String key = PREFIX + reservationNumber;
        Ticket ticket = (Ticket) redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(ticket);
    }

    // 삭제
    public void deleteTempReservation(String reservationNumber) {
        redisTemplate.delete(PREFIX + reservationNumber);
    }
}
