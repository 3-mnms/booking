package com.mnms.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class WaitingService {

    private final WaitingQueueRedisService waitingQueueRedisService;
    private final WaitingQueueSchedulingService waitingQueueSchedulingService;
    private final WaitingNotificationService waitingNotificationService;
    private final WaitingQueueKeyGenerator waitingQueueKeyGenerator;

    /// 사용자 대기열 진입 처리
    public long enterWaitingQueue(String festivalId, LocalDateTime reservationDate, String userId, long availableNOP) {
        String bookingUsersKey = waitingQueueKeyGenerator.getBookingUsersKey(festivalId, reservationDate);
        String waitingQueueKey = waitingQueueKeyGenerator.getWaitingQueueKey(festivalId, reservationDate);
        String notificationChannelKey = waitingQueueKeyGenerator.getNotificationChannelKey(festivalId, reservationDate);

        boolean entered = waitingQueueRedisService.tryEnterBooking(bookingUsersKey, availableNOP, userId);

        if (entered) {
            log.info("User {} entered booking page immediately.", userId);
            return 0L; // 즉시 입장
        } else { // 대기열 입장
            waitingQueueRedisService.addUserToQueue(waitingQueueKey, userId);
            log.info("User {} added to waiting queue {}.", userId, waitingQueueKey);
            waitingQueueSchedulingService.startScheduler(waitingQueueKey, bookingUsersKey, notificationChannelKey, availableNOP);
            return waitingNotificationService.getAndPublishWaitingNumber(waitingQueueKey, notificationChannelKey, userId);
        }
    }

    /// 예매 페이지에서 사용자 퇴장 처리 (예매 완료 또는 타임아웃)
    public boolean userExitBookingPage(String festivalId, LocalDateTime reservationDate, String userId){
        String bookingUsersKey = waitingQueueKeyGenerator.getBookingUsersKey(festivalId, reservationDate);
        boolean removed = waitingQueueRedisService.removeBookingUser(bookingUsersKey, userId);

        if(removed){
            log.info("User {} exited booking page and removed from booking user set.", userId);
            return true;
        }else{
            log.warn("User {} was not found in booking user set on exit.", userId);
            return false;
        }
    }

    /// 특정 사용자가 대기열에서 이탈했음을 처리
    public boolean removeUserFromQueue(String festivalId, LocalDateTime reservationDate, String userId) {
        String waitingQueueKey = waitingQueueKeyGenerator.getWaitingQueueKey(festivalId, reservationDate);
        String notificationChannelKey = waitingQueueKeyGenerator.getNotificationChannelKey(festivalId, reservationDate);

        boolean removed = waitingQueueRedisService.removeUserFromQueue(waitingQueueKey, userId);

        if (removed) {
            log.info("User {} removed from waiting queue (manual removal).", userId);
            waitingNotificationService.notifyAllWaitingUsers(waitingQueueKey, notificationChannelKey);
            return true;
        } else {
            log.warn("Attempted to remove user {} from queue, but user was not found or removal failed.", userId);
            return false;
        }
    }
}