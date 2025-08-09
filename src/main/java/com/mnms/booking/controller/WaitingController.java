package com.mnms.booking.controller;

import com.mnms.booking.dto.response.WaitingNumberResponseDTO;
import com.mnms.booking.util.JwtPrincipal;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/booking")
@Tag(name = "대기열 API", description = "대기열 입장, 예매 화면 입장, 대기번호 조회(websocket), 대기열 퇴장")
public class WaitingController {

    private final WaitingService waitingService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    // 예매하기 버튼 클릭 시 호출되는 API
    @GetMapping("/enter")
    @ResponseBody
    @Operation(summary = "대기열/예매 페이지 진입",
            description = "페스티벌 수용 인원만큼 예매 페이지 진입하며," +
                    "나머지 사용자는 대기열에 들어가 대기하게 됩니다.")
    public ResponseEntity<WaitingNumberResponseDTO> enterBookingPage(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal) {

        String loginId = (principal != null) ? principal.loginId() : "swagger-test-user";
        long waitingNumber = waitingService.enterWaitingQueue(loginId);

        if (waitingNumber == 0) {
            return ResponseEntity.ok(new WaitingNumberResponseDTO(loginId, 0, true, "REDIRECT_TO_BOOKING_PAGE"));
        } else {
            return ResponseEntity.ok(new WaitingNumberResponseDTO(loginId, waitingNumber, false, "WAITING_QUEUE_ENTERED"));
        }
    }


    // 예매 페이지 퇴장 - 대기열 대기자 예매 페이지 진입 (추후 수정 가능)
    @GetMapping("/release")
    @Operation(
            summary = "예매 페이지 진입 완료 처리",
            description = "예매 페이지에 있던 사용자가 예매 페이지에서 나가면, " +
                    "대기열에 있던 사용자가 예매 페이지로 입장 하게 됩니다. " +
                    "대기열에 있던 모든 대기자의 대기번호가 변경됩니다."
    )
    public ResponseEntity<String> releaseUser(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal) {
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


    // 대기열에서 퇴장
    @Operation(
            summary = "대기열 퇴장",
            description = "대기 중인 사용자가 스스로 대기열에서 나갈 때 호출됩니다. " +
                    "호출 시 해당 사용자는 대기열에서 제거됩니다."
    )
    @GetMapping("/exit")
    public ResponseEntity<String> exitWaitingUser(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal principal) {
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
    @Hidden
    @MessageMapping("/subscribe/waiting")
    public void subscribeWaitingQueue(@AuthenticationPrincipal JwtPrincipal principal) {
        String loginId = principal.loginId();
        log.info("User {} subscribed to waiting queue updates.", loginId);
        waitingService.getAndPublishWaitingNumber(loginId);
    }
}