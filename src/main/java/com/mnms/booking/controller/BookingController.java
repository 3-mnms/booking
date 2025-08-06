package com.mnms.booking.controller;

import com.mnms.booking.dto.response.WaitingNumberDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.mnms.booking.service.WaitingService;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/booking")
public class BookingController {

    private final WaitingService waitingService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    /**
     * 예매하기 버튼 클릭 시 호출되는 API
     * userId는 수정할 예정 (추후)
     */
    @GetMapping("/enter")
    @ResponseBody // JSON 응답을 위해 추가
    public ResponseEntity<WaitingNumberDTO> enterBookingPage(@RequestParam("userId") String userId) {
        long waitingNumber = waitingService.enterWaitingQueue(userId);

        if (waitingNumber == 0) {
            // 즉시 입장 가능한 경우
            return ResponseEntity.ok(new WaitingNumberDTO(userId, 0, true, "REDIRECT_TO_BOOKING_PAGE"));
        } else {
            // 대기열에 진입한 경우
            return ResponseEntity.ok(new WaitingNumberDTO(userId, waitingNumber, false, "WAITING_QUEUE_ENTERED"));
        }
    }

    /**
     * 대기열에 있는 사용자가 예매 페이지로 진입 완료 후 호출
     * (이 사용자는 대기열에서 제거됨)
     * userId는 수정할 예정 (추후)
     */
    @GetMapping("/release/{userId}")
    public ResponseEntity<String> releaseUser(@PathVariable("userId") String userId) {
        try {
            boolean removed = waitingService.userExitBookingPage(userId);
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
     * userId는 수정할 예정 (추후)
     */
    @GetMapping("/exit/{userId}")
    public ResponseEntity<String> exitWaitingUser(@PathVariable("userId") String userId) {
        try {
            boolean removed = waitingService.removeUserFromQueue(userId);
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
     * userId는 수정할 예정 (추후)
     */
    @MessageMapping("/subscribe/waiting/{userId}")
    public void subscribeWaitingQueue(@DestinationVariable("userId") String userId) {
        log.info("User {} subscribed to waiting queue updates.", userId);
        waitingService.getAndPublishWaitingNumber(userId);
    }
}