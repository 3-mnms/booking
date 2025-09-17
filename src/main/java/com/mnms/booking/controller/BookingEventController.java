package com.mnms.booking.controller;

import com.mnms.booking.dto.request.LeaveQueueRequestDTO;
import com.mnms.booking.service.WaitingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/// front -> back websocket 소통을 위한 controller입니다.

@RequiredArgsConstructor
@Slf4j
@Controller
public class BookingEventController {

    private final WaitingService waitingService;

    // /app/queue/waiting/leave로 front 메시지 보내기
    // 대기열에서 나갈 때
    @MessageMapping("/queue/waiting/leave")
    public void leaveWaitingQueue(LeaveQueueRequestDTO request, Principal principal) {
        try {
            String userId = principal.getName();
            boolean removed = waitingService.removeUserFromQueue(request.getFestivalId(), request.getReservationDate(), userId);
            if (removed) {
                log.info("대기하던 사용자가 대기열을 나갔습니다."); // 일단 테스트로 log 추가 - 추후 변경 예정
            } else {
                log.info("해당 사용자는 대기열 목록에 없습니다.");
            }
        } catch (Exception e) {
            log.info("서버 오류로 인해 사용자를 처리하지 못했습니다.");
        }
    }

    // 예매 페이지에서 퇴장
    @MessageMapping("/queue/reservation/leave")
    public void leaveReservationQueue(LeaveQueueRequestDTO request, Principal principal) {
        try {
            String userId = principal.getName();
            boolean removed = waitingService.userExitBookingPage(request.getFestivalId(), request.getReservationDate(), userId);
            if (removed) {
                log.info("사용자가 예매 페이지를 나갔고, 다음 대기자가 입장했습니다.");
            } else {
                log.info("해당 사용자는 예매 사용자 목록에 없습니다.");
            }
        } catch (Exception e) {
                log.info("서버 오류로 인해 사용자를 처리하지 못했습니다.");
        }
    }
}
