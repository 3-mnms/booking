package com.mnms.booking.service;

import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
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
            boolean added = waitingQueueRedisService.addUserToQueue(waitingQueueKey, userId);
            if (!added) {
                throw new BusinessException(ErrorCode.FAILED_TO_ENTER_QUEUE);
            }
            log.info("User {} added to waiting queue {}.", userId, waitingQueueKey);

            waitingQueueSchedulingService.startScheduler(waitingQueueKey, bookingUsersKey, notificationChannelKey, availableNOP);
            return waitingNotificationService.getAndPublishWaitingNumber(waitingQueueKey, notificationChannelKey, userId);
        }
    }

    /// 예매 페이지에서 사용자 퇴장 처리 (예매 완료 또는 타임아웃)
    public boolean userExitBookingPage(String festivalId, LocalDateTime reservationDate, String userId){
        String bookingUsersKey = waitingQueueKeyGenerator.getBookingUsersKey(festivalId, reservationDate);
        String waitingQueueKey = waitingQueueKeyGenerator.getWaitingQueueKey(festivalId, reservationDate);
        boolean removed = waitingQueueRedisService.removeBookingUser(bookingUsersKey, userId);

        if(!removed){
            throw new BusinessException(ErrorCode.USER_NOT_FOUND_IN_BOOKING);
        }

        log.info("User {} exited booking page and removed from booking user set.", userId);

        // 예약자 Set이 비어 있는지 확인
        long booking_remaining = waitingQueueRedisService.getBookingUserCount(bookingUsersKey);
        long waiting_remaining = waitingQueueRedisService.getWaitingUserCount(waitingQueueKey);

        if (booking_remaining == 0) {
            // 대기열도 비어 있으면 키 삭제
            waitingQueueRedisService.cleanKey(bookingUsersKey);
            log.info("Cleaned up all Booking Redis keys for festival {}.", bookingUsersKey);
        }
        if(waiting_remaining == 0) {
            waitingQueueRedisService.cleanKey(waitingQueueKey);
            log.info("Cleaned up all Waiting Redis keys for festival {}.", waitingQueueKey);
        }
        return true;
    }

    /// 특정 사용자가 대기열에서 이탈했음을 처리
    public boolean removeUserFromQueue(String festivalId, LocalDateTime reservationDate, String userId) {
        String waitingQueueKey = waitingQueueKeyGenerator.getWaitingQueueKey(festivalId, reservationDate);
        String notificationChannelKey = waitingQueueKeyGenerator.getNotificationChannelKey(festivalId, reservationDate);

        Long removedRank = waitingQueueRedisService.getRank(waitingQueueKey, userId);
        boolean removed = waitingQueueRedisService.removeUserFromQueue(waitingQueueKey, userId);

        if (!removed) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND_IN_WAITING);
        }
        log.info("User {} removed from waiting queue (manual removal).", userId);
        waitingNotificationService.notifyAffectedWaitingUsers(waitingQueueKey, notificationChannelKey, removedRank);
        return true;
    }
}