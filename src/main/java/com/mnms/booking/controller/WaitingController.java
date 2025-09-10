package com.mnms.booking.controller;

import com.mnms.booking.dto.response.WaitingNumberResponseDTO;
import com.mnms.booking.exception.global.SuccessResponse;
import com.mnms.booking.service.FestivalService;
import com.mnms.booking.service.WaitingNotificationService;
import com.mnms.booking.service.WaitingQueueKeyGenerator;
import com.mnms.booking.specification.WaitingSpecification;
import com.mnms.booking.util.ApiResponseUtil;
import com.mnms.booking.util.SecurityResponseUtil;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.mnms.booking.service.WaitingService;

import java.time.LocalDateTime;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/booking")
public class WaitingController implements WaitingSpecification {

    private final WaitingService waitingService;
    private final FestivalService festivalService;
    private final WaitingQueueKeyGenerator waitingQueueKeyGenerator;
    private final WaitingNotificationService waitingNotificationService;
    private final SecurityResponseUtil securityResponseUtil;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /// 예매하기 버튼(front) 클릭 시 호출되는 API
    @Override
    public ResponseEntity<SuccessResponse<WaitingNumberResponseDTO>> enterBookingPage(
            @RequestParam String festivalId,
            @RequestParam LocalDateTime reservationDate,
            Authentication authentication) {

        String userId =  authentication != null ? getUserId(authentication) : "swagger-test-user";

        int availableNOP = festivalService.getCapacity(festivalId); // 수용 인원 가져오기
        long waitingNumber = waitingService.enterWaitingQueue(festivalId, reservationDate, userId, availableNOP);

        if (waitingNumber == 0) {
            return ApiResponseUtil.success(new WaitingNumberResponseDTO(userId, 0, true, "REDIRECT_TO_BOOKING_PAGE"));
        } else {
            return ApiResponseUtil.success(new WaitingNumberResponseDTO(userId, waitingNumber, false, "WAITING_QUEUE_ENTERED"));
        }
    }

    /// 예매 페이지 퇴장
    @Override
    public ResponseEntity<SuccessResponse<String>> releaseUser(
            @RequestParam String festivalId,
            @RequestParam LocalDateTime reservationDate,
            @Parameter(hidden = true) Authentication authentication) { ///  예매칸에 있는 예매자 accessToken
        try {
            boolean removed = waitingService.userExitBookingPage(festivalId, reservationDate, getUserId(authentication));
            if (removed) {
                return ApiResponseUtil.success("사용자가 예매 페이지를 나갔고, 다음 대기자가 입장했습니다.");
            } else {
                return ApiResponseUtil.fail("해당 사용자는 예매 사용자 목록에 없습니다.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return ApiResponseUtil.fail("서버 오류로 인해 사용자를 처리하지 못했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /// 대기열에서 퇴장
    @Override
    public ResponseEntity<SuccessResponse<String>> exitWaitingUser(
            @RequestParam String festivalId,
            @RequestParam LocalDateTime reservationDate,
            @Parameter(hidden = true) Authentication authentication) {
        try {
            boolean removed = waitingService.removeUserFromQueue(festivalId, reservationDate, getUserId(authentication));
            if (removed) {
                return ApiResponseUtil.success("대기하던 사용자가 대기열을 나갔습니다.");
            } else {
                return ApiResponseUtil.fail("해당 사용자는 대기열 목록에 없습니다.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return ApiResponseUtil.fail("서버 오류로 인해 사용자를 처리하지 못했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * WebSocket: 특정 사용자의 대기 순번 구독 엔드포인트
     * 클라이언트가 /app/subscribe/waiting로 메시지를 보냄 (최초 구독 요청)
     */
    @MessageMapping("/subscribe/waiting")
    public void subscribeWaitingQueue(
            @Header("festivalId") String festivalId,
            @Header("reservationDate") LocalDateTime reservationDate,
            Authentication authentication) {
        String userId = getUserId(authentication);
        log.info("User {} subscribed to waiting queue updates.", userId);

        String waitingQueueKey = waitingQueueKeyGenerator.getWaitingQueueKey(festivalId, reservationDate);
        String notificationChannelKey = waitingQueueKeyGenerator.getNotificationChannelKey(festivalId, reservationDate);
        waitingNotificationService.getAndPublishWaitingNumber(waitingQueueKey, notificationChannelKey, userId);
    }


    public String getUserId(Authentication authentication) {
        return String.valueOf(securityResponseUtil.requireUserId(authentication));
    }
}