package com.mnms.booking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mnms.booking.dto.response.WaitingNumberResponseDTO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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

    // 예매 페이지 수용인원
    private static final long PERFORMANCE_CAPACITY = 2; // 공연 수용 인원
    private static final double IMMEDIATE_ENTRY_RATIO = 1; // 기준 : 즉시 입장 비율 (1.5배), 테스트 (1배)
    private static final long IMMEDIATE_ENTRY_COUNT = (long) (PERFORMANCE_CAPACITY * IMMEDIATE_ENTRY_RATIO);

    // scheduled
    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private ScheduledFuture<?> scheduledTask;


    @PostConstruct
    public void init() {
        zSetOperations = redisTemplate.opsForZSet();
        List<String> waitingUsers = getWaitingUsers();
        if (!waitingUsers.isEmpty()) {
            startScheduler();
        }
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
     * 사용자 대기열 진입 처리
     * @param userId 사용자 ID
     * @return 대기 순번 (즉시 입장 가능한 경우 0)
     */
    public long enterWaitingQueue(String userId) {

        // 현재 입장한 사용자 수
        Long currentUserCount = redisTemplate.opsForSet().size(BOOKING_USERS_SET_KEY);
        if (currentUserCount == null) currentUserCount = 0L;

        // 입장한 사용자가 제한보다 작을 때
        if(currentUserCount < IMMEDIATE_ENTRY_COUNT){
            log.info("User {} can enter booking page immediately ({} < {}).", userId, currentUserCount, IMMEDIATE_ENTRY_COUNT);

            redisTemplate.opsForSet().add(BOOKING_USERS_SET_KEY, userId); // 사용자 등록
            return 0L;
        } else{
            // 대기열로 보냄 (ZSet)
            long timestamp = System.currentTimeMillis();
            zSetOperations.add(WAITING_QUEUE_KEY, userId, timestamp);

            log.info("User {} added to waiting queue with timestamp {}.", userId, timestamp);

            // 조건부 스케줄러 시작
            startScheduler();

            return getAndPublishWaitingNumber(userId);
        }
    }

    /**
     * 스케줄러 시작 (중복 시작 방지)
     */
    public synchronized void startScheduler() {
        if (scheduledTask == null || scheduledTask.isCancelled() || scheduledTask.isDone()) {
            log.info("대기열 감시 스케줄러 시작됨.");
            scheduledTask = scheduler.scheduleWithFixedDelay(this::runSchedulerLogic, Duration.ofSeconds(5));
        }
    }

    /**
     * 스케줄러 중지
     */
    public synchronized void stopScheduler() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            log.info("대기열이 비어 스케줄러 중지됨.");
            scheduledTask.cancel(false);
        }
    }

    /**
     * 주기적으로 대기열 순번 발행
     */
    public void runSchedulerLogic() {
        List<String> waitingUsers = getWaitingUsers();
        if (waitingUsers.isEmpty()) {
            stopScheduler(); // 비었으면 중지
            return;
        }

        for (String userId : waitingUsers) {
            getAndPublishWaitingNumber(userId);
        }
        log.info("대기열 사용자 {}명에게 대기번호 발행 완료.", waitingUsers.size());
    }

    public List<String> getWaitingUsers() {
        Set<String> userSet = zSetOperations.range(WAITING_QUEUE_KEY, 0, -1);
        return (userSet == null) ? Collections.emptyList() : new ArrayList<>(userSet);
    }


    /**
     * 사용자 대기 순번 조회 및 Redis Pub/Sub으로 발행
     * @param userId 사용자 ID
     * @return 대기 순번
     */
    public long getAndPublishWaitingNumber(String userId) {
        // 해당 userId의 대기열 순번을 조회
        Long rank = zSetOperations.rank(WAITING_QUEUE_KEY, userId);

        if (rank != null) {
            long waitingNumber = rank + 1;
            //log.info("User {}'s waiting number: {}", userId, waitingNumber);

            // Redis Pub/Sub 채널로 메시지 발행
            publishWaitingNumber(userId, waitingNumber);

            return waitingNumber;
        }
        return -1; // 대기열에 없는 경우
    }

    /**
     * Redis Pub/Sub 채널로 대기 순번 정보 발행
     * @param userId 사용자 ID
     * @param waitingNumber 대기 순번
     */
    private void publishWaitingNumber(String userId, long waitingNumber) {
        try {
            // immediateEntry는 false (이 메서드는 대기 순번 알림용), message는 null
            WaitingNumberResponseDTO dto = new WaitingNumberResponseDTO(userId, waitingNumber, false, null);
            String message = objectMapper.writeValueAsString(dto);
            // Redis 채널로 발행
            stringRedisTemplate.convertAndSend(NOTIFICATION_CHANNEL, message);
        } catch (JsonProcessingException e) {
            log.error("Error converting DTO to JSON: {}", e.getMessage());
        }
    }

    /**
     * 대기열에서 다음 사용자 진입 처리
     * (예매 완료 또는 타임아웃 등으로 인해 예매 페이지에서 나간 경우 호출)
     */
    public boolean userExitBookingPage(String userId){
        // 예매 완료된 Set에서 제거
        Long removed = redisTemplate.opsForSet().remove(BOOKING_USERS_SET_KEY, userId);

        if(removed != null && removed > 0){
            log.info("User {} exited booking page and removed from booking user set.", userId);
            // 대기열에서 다음 사용자 진입 처리
            releaseWaitingUser();
            return true;
        }else{
            log.warn("User {} was not found in booking user set on exit.", userId);
            return false;
        }
    }

    public void releaseWaitingUser() {
        // 대기열 큐 가장 앞에 있는 사람
        Set<String> releasedUsers = zSetOperations.range(WAITING_QUEUE_KEY, 0, 0);

        if (releasedUsers != null && !releasedUsers.isEmpty()) {
            String releasedUser = releasedUsers.iterator().next();

            // 대기열에서 삭제
            Long removedCount = zSetOperations.remove(WAITING_QUEUE_KEY, releasedUser);

            if (removedCount != null && removedCount > 0) {
                log.info("User {} removed from waiting queue.", releasedUser);
                // 예매 중 사용자 set에 추가
                redisTemplate.opsForSet().add(BOOKING_USERS_SET_KEY, releasedUser);
                log.info("User {} added to booking user set.", releasedUser);

                // 남은 대기열 사용자에게 순번 알림
                notifyAllWaitingUsers();

            } else {
                log.warn("Failed to remove user {} from waiting queue. Removed count: {}", releasedUser, removedCount);
            }
        }
    }

    /**
     * 모든 대기열 사용자에게 순번 업데이트 알림
     */
    public void notifyAllWaitingUsers() {
        // 대기열 사용자 수 전체 범위 조회
        Set<String> allUsersInQueue = zSetOperations.range(WAITING_QUEUE_KEY, 0, -1);

        if (allUsersInQueue != null) {
            for (String userId : allUsersInQueue) {
                // 각 사용자에게 순번 알림을 Redis Pub/Sub으로 발행
                getAndPublishWaitingNumber(userId);
            }
        }
    }

    /**
     * 특정 사용자가 대기열에서 이탈했음을 처리
     * @param userId
     * @return boolean - 사용자가 대기열에서 성공적으로 제거되었으면 true, 아니면 false
     */
    public boolean removeUserFromQueue(String userId) {
        Long removedCount = zSetOperations.remove(WAITING_QUEUE_KEY, userId);
        if (removedCount != null && removedCount > 0) {
            log.info("User {} removed from waiting queue (manual removal).", userId);
            notifyAllWaitingUsers(); // 순번 업데이트 알림
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
}