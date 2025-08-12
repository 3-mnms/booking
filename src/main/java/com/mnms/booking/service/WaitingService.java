package com.mnms.booking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mnms.booking.dto.response.WaitingNumberResponseDTO;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@Slf4j
public class WaitingService {

    private final RedisTemplate<String, String> redisTemplate; // ZSet 관리용
    private final StringRedisTemplate stringRedisTemplate; // Pub/Sub 발행용
    private final ObjectMapper objectMapper; // DTO를 JSON으로 변환용
    private ZSetOperations<String, String> zSetOperations;

    // redis message
    // 대기열큐에 진입한 사용자 ZSet
    private static final String WAITING_QUEUE_KEY = "waiting_queue";

    // 예매 페이지에 진입한 사용자 Set
    private static final String BOOKING_USERS_SET_KEY = "booking_users";

    // Redis Pub/Sub 채널 이름
    private static final String NOTIFICATION_CHANNEL = "waiting_notification";

    // scheduled
    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private ScheduledFuture<?> scheduledTask;
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public void init() {
        zSetOperations = redisTemplate.opsForZSet();
        // startScheduler() 호출 코드를 제거합니다.
    }

    public WaitingService(
            @Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate, // <-- redisTemplate 빈 주입
            @Qualifier("stringRedisTemplate") StringRedisTemplate stringRedisTemplate, // <-- stringRedisTemplate 빈 주입
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;

        // scheduler 초기화
        scheduler.initialize();
    }


    /**
     * 사용자 대기열 진입 처리 (공연별·시간별 분리)
     * @param festivalId, reservationDate, loginId, availableNOP
     * @return 대기 순번 (즉시 입장 가능 시 0)
     */
    public long enterWaitingQueue(String festivalId, LocalDateTime reservationDate, String loginId, long availableNOP) {
        String bookingUsersKey = getBookingUsersKey(festivalId, reservationDate);
        String waitingQueueKey = getWaitingQueueKey(festivalId, reservationDate);
        String notificationChannelKey = getNotificationChannelKey(festivalId, reservationDate);

        zSetOperations = redisTemplate.opsForZSet(); // Redis ZSet ops 재설정 필요

        Long currentUserCount = redisTemplate.opsForSet().size(bookingUsersKey);
        if (currentUserCount == null) currentUserCount = 0L;

        if (currentUserCount < availableNOP) {
            log.info("User {} can enter booking page immediately ({} < {}).", loginId, currentUserCount, availableNOP);
            redisTemplate.opsForSet().add(bookingUsersKey, loginId);
            return 0L;
        } else {
            long timestamp = System.currentTimeMillis();
            zSetOperations.add(waitingQueueKey, loginId, timestamp);
            log.info("User {} added to waiting queue {} with timestamp {}.", loginId, waitingQueueKey, timestamp);

            startScheduler(waitingQueueKey, bookingUsersKey, notificationChannelKey, availableNOP);

            return getAndPublishWaitingNumber(waitingQueueKey, notificationChannelKey, loginId);
        }
    }

    /**
     * 스케줄러 시작 (중복 시작 방지)
     */
    public synchronized void startScheduler(String waitingQueueKey, String bookingUsersKey, String notificationChannelKey, long availableNOP) {
        if (scheduledTasks.containsKey(waitingQueueKey) && !scheduledTasks.get(waitingQueueKey).isDone()) {
            // 이미 스케줄러 실행 중
            return;
        }
        log.info("Starting scheduler for queue: {}", waitingQueueKey);
        ScheduledFuture<?> task = scheduler.scheduleWithFixedDelay(() -> runSchedulerLogic(waitingQueueKey, bookingUsersKey, notificationChannelKey, availableNOP), Duration.ofSeconds(5));
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

    /**
     * 주기적으로 대기열 순번 발행
     */
    public void runSchedulerLogic(String waitingQueueKey, String bookingUsersKey, String notificationChannelKey, long availableNOP) {
        zSetOperations = redisTemplate.opsForZSet();
        Set<String> waitingUsers = zSetOperations.range(waitingQueueKey, 0, -1);
        if (waitingUsers == null || waitingUsers.isEmpty()) {
            stopScheduler(waitingQueueKey);
            return;
        }

        for (String loginId : waitingUsers) {
            getAndPublishWaitingNumber(waitingQueueKey, notificationChannelKey, loginId);
        }
        log.info("Published waiting numbers to {} users for queue {}", waitingUsers.size(), waitingQueueKey);

        // 빈자리 생겼을 때 대기열에서 사용자 입장 처리
        Long currentBookingCount = redisTemplate.opsForSet().size(bookingUsersKey);
        if (currentBookingCount == null) currentBookingCount = 0L;

        // 빈자리가 생겼을 때
        while (currentBookingCount < availableNOP) {
            Set<String> nextUsers = zSetOperations.range(waitingQueueKey, 0, 0);
            if (nextUsers == null || nextUsers.isEmpty()) {
                break;
            }
            // 대기열에서 대기자 지우고, 예메 페이지에 입장
            String nextUser = nextUsers.iterator().next();
            Long removed = zSetOperations.remove(waitingQueueKey, nextUser);

            // 대기열에 removed 된 사람이 있으면
            if (removed != null && removed > 0) {
                redisTemplate.opsForSet().add(bookingUsersKey, nextUser);
                log.info("User {} moved from waiting queue to booking users for queue {}", nextUser, waitingQueueKey);
                notifyAllWaitingUsers(waitingQueueKey, notificationChannelKey);
                currentBookingCount++;
            } else {
                break;
            }
        }
    }

    public List<String> getWaitingUsers() {
        Set<String> userSet = zSetOperations.range(WAITING_QUEUE_KEY, 0, -1);
        return (userSet == null) ? Collections.emptyList() : new ArrayList<>(userSet);
    }


    /**
     * 사용자 대기 순번 조회 및 Redis Pub/Sub으로 발행
     * @param waitingQueueKey, userId
     * @return 대기 순번
     * getNotificationChannelKey(festivalId, reservationDate);
     */
    public long getAndPublishWaitingNumber(String waitingQueueKey, String notificationChannelKey, String loginId) {
        zSetOperations = redisTemplate.opsForZSet();
        Long rank = zSetOperations.rank(waitingQueueKey, loginId);
        if (rank != null) {
            long waitingNumber = rank + 1;
            publishWaitingNumber(loginId, waitingNumber, notificationChannelKey);
            return waitingNumber;
        }
        return -1;
    }

    /**
     * Redis Pub/Sub 채널로 대기 순번 정보 발행
     * @param userId 사용자 ID
     * @param waitingNumber 대기 순번
     */
    private void publishWaitingNumber(String userId, long waitingNumber, String notificationChannelKey) {
        try {
            // immediateEntry는 false (이 메서드는 대기 순번 알림용), message는 null
            WaitingNumberResponseDTO waitingNumberDto = new WaitingNumberResponseDTO(userId, waitingNumber, false, null);
            //WaitingNumberResponseDTO waitingNumberDto = new WaitingNumberResponseDTO(userId, waitingNumber);
            String message = objectMapper.writeValueAsString(waitingNumberDto);
            // Redis 채널로 발행
            stringRedisTemplate.convertAndSend(notificationChannelKey, message);
        } catch (JsonProcessingException e) {
            log.error("Error converting DTO to JSON: {}", e.getMessage());
        }
    }

    /**
     * 대기열에서 다음 사용자 진입 처리
     * (예매 완료 또는 타임아웃 등으로 인해 예매 페이지에서 나간 경우 호출)
     */
    public boolean userExitBookingPage(String festivalId, LocalDateTime reservationDate, String userId){
        // 예매 완료된 Set에서 제거
        Long removed = redisTemplate.opsForSet().remove(BOOKING_USERS_SET_KEY, userId);

        if(removed != null && removed > 0){
            log.info("User {} exited booking page and removed from booking user set.", userId);
            // 대기열에서 다음 사용자 진입 처리
            releaseWaitingUser(festivalId, reservationDate);
            return true;
        }else{
            log.warn("User {} was not found in booking user set on exit.", userId);
            return false;
        }
    }

    public void releaseWaitingUser(String festivalId, LocalDateTime reservationDate) {

        String waitingQueueKey = getWaitingQueueKey(festivalId, reservationDate);
        String bookingUserSetKey = getBookingUsersKey(festivalId, reservationDate);
        String notificationChannelKey = getNotificationChannelKey(festivalId, reservationDate);

        // 대기열 큐 가장 앞에 있는 사람
        Set<String> releasedUsers = zSetOperations.range(waitingQueueKey, 0, 0);

        if (releasedUsers != null && !releasedUsers.isEmpty()) {
            String releasedUser = releasedUsers.iterator().next();

            // 대기열에서 삭제
            Long removedCount = zSetOperations.remove(waitingQueueKey, releasedUser);

            if (removedCount != null && removedCount > 0) {
                log.info("User {} removed from waiting queue.", releasedUser);

                // 예매 중 사용자 set에 추가
                redisTemplate.opsForSet().add(bookingUserSetKey, releasedUser);
                log.info("User {} added to booking user set.", releasedUser);

                // 남은 대기열 사용자에게 순번 알림
                notifyAllWaitingUsers(waitingQueueKey, notificationChannelKey);

            } else {
                log.warn("Failed to remove user {} from waiting queue. Removed count: {}", releasedUser, removedCount);
            }
        }
    }

    /**
     * 모든 대기열 사용자에게 순번 업데이트 알림
     */
    public void notifyAllWaitingUsers(String waitingQueueKey, String notificationChannelKey) {
        // 대기열 사용자 수 전체 범위 조회
        Set<String> allUsersInQueue = zSetOperations.range(waitingQueueKey, 0, -1);

        if (allUsersInQueue != null) {
            for (String userId : allUsersInQueue) {
                // 각 사용자에게 순번 알림을 Redis Pub/Sub으로 발행
                getAndPublishWaitingNumber(waitingQueueKey, notificationChannelKey, userId);
            }
        }
    }

    /**
     * 특정 사용자가 대기열에서 이탈했음을 처리
     * @param festivalId, reservationDate, userId
     * @return boolean - 사용자가 대기열에서 성공적으로 제거되었으면 true, 아니면 false
     */
    public boolean removeUserFromQueue(String festivalId, LocalDateTime reservationDate, String userId) {
        Long removedCount = zSetOperations.remove(WAITING_QUEUE_KEY, userId);
        String waitingQueueKey = getWaitingQueueKey(festivalId, reservationDate);
        String notificationChannelKey = getNotificationChannelKey(festivalId, reservationDate);

        if (removedCount != null && removedCount > 0) {
            log.info("User {} removed from waiting queue (manual removal).", userId);
            notifyAllWaitingUsers(waitingQueueKey, notificationChannelKey); // 순번 업데이트 알림
            return true;
        } else {
            // 사용자가 대기열에 없었거나, 제거 과정에서 문제가 발생한 경우
            log.warn("Attempted to remove user {} from queue, but user was not found or removal failed. Removed count: {}", userId, removedCount);
            return false;
        }
    }

    @PreDestroy
    public void cleanup() {
        scheduler.shutdown();
    }

    /// util
    public String getWaitingQueueKey(String festivalId, LocalDateTime reservationDate) {
        // reservationDate는 yyyyMMddHHmm 형태로 저장 (ex: 202509011930)
        String dateStr = reservationDate.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        return WAITING_QUEUE_KEY + festivalId + ":" + dateStr;
    }

    private String getBookingUsersKey(String festivalId, LocalDateTime reservationDate) {
        String dateStr = reservationDate.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        return BOOKING_USERS_SET_KEY + festivalId + ":" + dateStr;
    }

    public String getNotificationChannelKey(String festivalId, LocalDateTime reservationDate) {
        String dateStr = reservationDate.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        return NOTIFICATION_CHANNEL + festivalId + ":" + dateStr;
    }
}