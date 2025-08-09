package com.mnms.booking.controller;

import com.mnms.booking.dto.response.WaitingNumberResponseDTO;
import com.mnms.booking.util.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.mnms.booking.service.WaitingService;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/booking")
public class WaitingController {

    private final WaitingService waitingService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    /**
     * 예매하기 버튼 클릭 시 호출되는 API
     */
    @GetMapping("/enter")
    @ResponseBody // JSON 응답을 위해 추가
    public ResponseEntity<WaitingNumberResponseDTO> enterBookingPage(@AuthenticationPrincipal JwtPrincipal principal) {
        String loginId = principal.loginId();
        long waitingNumber = waitingService.enterWaitingQueue(loginId);

        if (waitingNumber == 0) {
            // 즉시 입장 가능한 경우
            return ResponseEntity.ok(new WaitingNumberResponseDTO(loginId, 0, true, "REDIRECT_TO_BOOKING_PAGE"));
        } else {
            // 대기열에 진입한 경우
            return ResponseEntity.ok(new WaitingNumberResponseDTO(loginId, waitingNumber, false, "WAITING_QUEUE_ENTERED"));
        }
    }

    /**
     * 대기열에 있는 사용자가 예매 페이지로 진입 완료 후 호출
     * (이 사용자는 대기열에서 제거됨)
     */
    @GetMapping("/release")
    public ResponseEntity<String> releaseUser(@AuthenticationPrincipal JwtPrincipal principal) {
        String loginId = principal.loginId();
        try {
            boolean removed = waitingService.userExitBookingPage(loginId);
            if (removed) {
                return ResponseEntity.ok("사용자가 예매 페이지를 나갔고, 다음 대기자가 입장했습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("해당 사용자는 예매 사용자 목록에 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 오류로 인해 사용자를 처리하지 못했습니다.");
        }
    }

    /**
     * 대기열에 있는 사용자가 대기열을 나간다 (예매 입장 아님)
     * (이 사용자는 대기열에서 제거됨)
     */
    @GetMapping("/exit")
    public ResponseEntity<String> exitWaitingUser(@AuthenticationPrincipal JwtPrincipal principal) {
        String loginId = principal.loginId();
        try {
            boolean removed = waitingService.removeUserFromQueue(loginId);
            if (removed) {
                return ResponseEntity.ok("대기하던 사용자가 대기열을 나갔습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("해당 사용자는 대기열 목록에 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 오류로 인해 사용자를 처리하지 못했습니다.");
        }
    }

    /**
     * WebSocket: 특정 사용자의 대기 순번 구독 엔드포인트
     * 클라이언트가 /app/subscribe/waiting/{userId} 로 메시지를 보냄 (최초 구독 요청)
     */
    @MessageMapping("/subscribe/waiting")
    public void subscribeWaitingQueue(@AuthenticationPrincipal JwtPrincipal principal) {
        String loginId = principal.loginId();
        log.info("User {} subscribed to waiting queue updates.", loginId);
        waitingService.getAndPublishWaitingNumber(loginId);
    }
}