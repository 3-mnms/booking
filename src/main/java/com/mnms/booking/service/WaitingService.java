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

    /**
     * 사용자 대기열 진입 처리
     * @return 대기 순번 (즉시 입장 가능 시 0)
     */
    public long enterWaitingQueue(String festivalId, LocalDateTime reservationDate, String loginId, long availableNOP) {
        String bookingUsersKey = waitingQueueKeyGenerator.getBookingUsersKey(festivalId, reservationDate);
        String waitingQueueKey = waitingQueueKeyGenerator.getWaitingQueueKey(festivalId, reservationDate);
        String notificationChannelKey = waitingQueueKeyGenerator.getNotificationChannelKey(festivalId, reservationDate);

        long currentUserCount = waitingQueueRedisService.getBookingUserCount(bookingUsersKey);

        if (currentUserCount < availableNOP) {
            log.info("User {} can enter booking page immediately ({} < {}).", loginId, currentUserCount, availableNOP);
            waitingQueueRedisService.addBookingUser(bookingUsersKey, loginId);
            return 0L;
        } else {
            waitingQueueRedisService.addUserToQueue(waitingQueueKey, loginId);
            log.info("User {} added to waiting queue {}.", loginId, waitingQueueKey);

            waitingQueueSchedulingService.startScheduler(waitingQueueKey, bookingUsersKey, notificationChannelKey, availableNOP);

            return waitingNotificationService.getAndPublishWaitingNumber(waitingQueueKey, notificationChannelKey, loginId);
        }
    }

    /**
     * 예매 페이지에서 사용자 이탈 처리 (예매 완료 또는 타임아웃)
     */
    public boolean userExitBookingPage(String festivalId, LocalDateTime reservationDate, String userId){
        String bookingUsersKey = waitingQueueKeyGenerator.getBookingUsersKey(festivalId, reservationDate);
        boolean removed = waitingQueueRedisService.removeBookingUser(bookingUsersKey, userId);

        if(removed){
            log.info("User {} exited booking page and removed from booking user set.", userId);
            // 대기열에서 다음 사용자 입장 처리
            releaseWaitingUser(festivalId, reservationDate);
            return true;
        }else{
            log.warn("User {} was not found in booking user set on exit.", userId);
            return false;
        }
    }

    /**
     * 대기열에서 다음 사용자 입장 처리
     */
    private void releaseWaitingUser(String festivalId, LocalDateTime reservationDate) {
        String waitingQueueKey = waitingQueueKeyGenerator.getWaitingQueueKey(festivalId, reservationDate);
        String bookingUsersKey = waitingQueueKeyGenerator.getBookingUsersKey(festivalId, reservationDate);
        String notificationChannelKey = waitingQueueKeyGenerator.getNotificationChannelKey(festivalId, reservationDate);

        // 대기열 큐 가장 앞에 있는 사람
        String releasedUser = waitingQueueRedisService.getFirstUserInQueue(waitingQueueKey);

        if (releasedUser != null) {
            boolean removed = waitingQueueRedisService.removeUserFromQueue(waitingQueueKey, releasedUser);

            if (removed) {
                log.info("User {} removed from waiting queue.", releasedUser);

                waitingQueueRedisService.addBookingUser(bookingUsersKey, releasedUser);
                log.info("User {} added to booking user set.", releasedUser);

                waitingNotificationService.notifyAllWaitingUsers(waitingQueueKey, notificationChannelKey);
            } else {
                log.warn("Failed to remove user {} from waiting queue.", releasedUser);
            }
        }
    }

    /**
     * 특정 사용자가 대기열에서 이탈했음을 처리
     */
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