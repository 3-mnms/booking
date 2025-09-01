package com.mnms.booking.service;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class WaitingQueueSchedulingService {
    private final WaitingQueueRedisService waitingQueueRedisService;
    private final WaitingNotificationService waitingNotificationService;
    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    /// 스케줄러 시작 (중복 시작 방지)
    public synchronized void startScheduler(String waitingQueueKey, String bookingUsersKey, String notificationChannelKey, long availableNOP) {
        if (scheduledTasks.containsKey(waitingQueueKey) && !scheduledTasks.get(waitingQueueKey).isDone()) {
            return;
        }
        log.info("Starting scheduler for queue: {}", waitingQueueKey);
        ScheduledFuture<?> task = scheduler.scheduleWithFixedDelay(
                () -> runSchedulerLogic(waitingQueueKey, bookingUsersKey, notificationChannelKey, availableNOP),
                Duration.ofSeconds(5)
        );
        scheduledTasks.put(waitingQueueKey, task);
    }

    /**
     * 스케줄러 중지
     */
    public synchronized void stopScheduler(String waitingQueueKey) {
        ScheduledFuture<?> task = scheduledTasks.get(waitingQueueKey);
        if (task != null && !task.isCancelled()) {
            task.cancel(false);
            scheduledTasks.remove(waitingQueueKey);
            log.info("Stopped scheduler for queue: {}", waitingQueueKey);
        }
    }

    /// 주기적으로 대기열 순번 발행 및 입장 처리
    private void runSchedulerLogic(String waitingQueueKey, String bookingUsersKey, String notificationChannelKey, long availableNOP) {
        Set<String> waitingUsers = waitingQueueRedisService.getAllUsersInQueue(waitingQueueKey);

        if (waitingUsers == null || waitingUsers.isEmpty()) {
            stopScheduler(waitingQueueKey);

            //


            return;
        }

        for (String loginId : waitingUsers) {
            waitingNotificationService.getAndPublishWaitingNumber(waitingQueueKey, notificationChannelKey, loginId);
        }

        long currentBookingCount = waitingQueueRedisService.getBookingUserCount(bookingUsersKey);

        while (currentBookingCount < availableNOP) {
            // 대기번호 1 유저
            String nextUser = waitingQueueRedisService.getFirstUserInQueue(waitingQueueKey);
            if (nextUser == null) {
                break;
            }

            boolean removed = waitingQueueRedisService.removeUserFromQueue(waitingQueueKey, nextUser);

            if (removed) {
                waitingQueueRedisService.addBookingUser(bookingUsersKey, nextUser);
                log.info("User {} moved from waiting queue to booking users for queue {}", nextUser, waitingQueueKey);
                waitingNotificationService.notifyAllWaitingUsers(waitingQueueKey, notificationChannelKey);
                currentBookingCount++;
            } else {
                break;
            }
        }
    }

    @PreDestroy
    public void cleanup() {
        scheduler.shutdown();
    }
}
