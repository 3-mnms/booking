package com.mnms.booking.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mnms.booking.dto.request.TicketTransferRequestDTO;
import com.mnms.booking.dto.request.UpdateTicketRequestDTO;
import com.mnms.booking.dto.response.PersonInfoResponseDTO;
import com.mnms.booking.dto.response.TicketResponseDTO;
import com.mnms.booking.dto.response.TicketTransferResponseDTO;
import com.mnms.booking.dto.response.TransferOthersResponseDTO;
import com.mnms.booking.enums.ReservationStatus;
import com.mnms.booking.exception.global.SuccessResponse;
import com.mnms.booking.service.OcrParserService;
import com.mnms.booking.service.OcrService;
import com.mnms.booking.service.TransferCompletionService;
import com.mnms.booking.service.TransferService;
import com.mnms.booking.util.ApiResponseUtil;
import com.mnms.booking.util.SecurityResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
@Tag(name = "양도 API", description = "양도 및 OCR, Ticket 재생성")
public class TransferController {
    private final OcrService ocrService;
    private final TransferService transferService;
    private final SecurityResponseUtil securityResponseUtil;
    private final TransferCompletionService transferCompletionService;

    ///  양도할 수 있는 티켓 조회
    @GetMapping("/transferor")
    @Operation(summary = "양도 가능한 티켓 정보 조회",
            description = "사용자가 양도 가능한 티켓을 조회할 수 있습니다. (status : 양도 받은 티켓 양도 불가능)"
    )
    public ResponseEntity<SuccessResponse<List<TicketResponseDTO>>> getUserTickets(Authentication authentication) {
        List<TicketResponseDTO> tickets = transferService.getTicketsByUser(securityResponseUtil.requireUserId(authentication));
        return ApiResponseUtil.success(tickets);
    }


    ///  가족관계증명서 인증
    @PostMapping("/extract")
    @Operation(summary = "가족 간 양도 진행 인증 시도",
            description = "가족관계증명서 PDF를 첨부하고 양도자 및 양수자의 이름과 주민등록번호로 요청하고, 인증 완료 응답을 보냅니다."
    )
    public ResponseEntity<SuccessResponse<Void>> extractPersonAuth(
            @RequestPart("file") MultipartFile file,
            @RequestPart("targetInfo") String targetInfoJson) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> targetInfo = objectMapper.readValue(targetInfoJson, new TypeReference<>() {});
        String ocrJson = ocrService.callOcr(file);
        OcrParserService.parseOcrResult(ocrJson, targetInfo);

        return ApiResponseUtil.success(null,"가족관계증명서 인증이 완료되었습니다.");
    }

    @PostMapping("/extract/result")
    @Operation(summary = "가족 간 양도 진행 인증 시도",
            description = "가족관계증명서 PDF를 첨부하고 양도자 및 양수자의 이름과 주민등록번호로 요청하고, 인증 완료 응답과 결과를 보냅니다."
    )
    public ResponseEntity<SuccessResponse<List<PersonInfoResponseDTO>>> extractPersonInfo(
            @RequestPart("file") MultipartFile file,
            @RequestPart("targetInfo") String targetInfoJson) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> targetInfo = objectMapper.readValue(targetInfoJson, new TypeReference<>() {});

        String ocrJson = ocrService.callOcr(file);
        List<PersonInfoResponseDTO> people = OcrParserService.parseOcrResult(ocrJson, targetInfo);

        return ApiResponseUtil.success(people, "가족관계증명서 인증이 완료되었습니다.");
    }


    /// 양도 요청
    @PostMapping("/request")
    @Operation(summary = "양도 요청",
            description = "양도자가 양도 요청 보내기"
    )
    public ResponseEntity<SuccessResponse<Void>> requestTransfer(
            @RequestBody @Valid TicketTransferRequestDTO dto,
            Authentication authentication
    ){
        transferService.requestTransfer(dto, securityResponseUtil.requireUserId(authentication));
        return ApiResponseUtil.success(null, "티켓 양도 요청이 완료되었습니다.");
    }

    /// 양도 요청 받기 (조회)
    @GetMapping("/watch")
    @Operation(summary = "양도 요청 조회",
            description = "양수자가 양도 요청을 조회합니다."
    )
    public ResponseEntity<SuccessResponse<List<TicketTransferResponseDTO>>> watchTransfer(Authentication authentication){
        List<TicketTransferResponseDTO> response = transferService.watchTransfer(securityResponseUtil.requireUserId(authentication));
        return ApiResponseUtil.success(response);
    }

    /// 가족 간 양도 요청 승인
    @PutMapping("/acceptance/family")
    @Operation(summary = "가족 간 양도 요청 수락",
            description = "가족 간 양도 요청 시, 양수자가 요청을 수락하면 양도가 완료되며 티켓과 QR 정보가 업데이트 됩니다."
    )
    public ResponseEntity<SuccessResponse<Void>> responseTicketFamily(
            @RequestBody UpdateTicketRequestDTO request,
            Authentication authentication) {
        transferCompletionService.updateFamilyTicket(request, securityResponseUtil.requireUserId(authentication));
        return ApiResponseUtil.success(null, "티켓 양도가 성공적으로 진행되었습니다.");
    }

    /// 지인간 양도 요청 승인
    @PutMapping("/acceptance/others")
    @Operation(summary = "타인 간 양도 요청 완료",
            description = "타인 간 양도 요청 시, 양수자가 요청을 수락하면 결제 요청이 넘어가게 됩니다. " +
            "결제 완료 후 kafka 메시지 받으면 양도 완료됩니다."
    )
    public ResponseEntity<SuccessResponse<TransferOthersResponseDTO>> responseTicketOthers(
            @RequestBody UpdateTicketRequestDTO request,
            Authentication authentication) {
            TransferOthersResponseDTO response = transferCompletionService.proceedOthersTicket(request, securityResponseUtil.requireUserId(authentication));
        return ApiResponseUtil.success(response);
    }

    ///  Websocket 메시지 누락 방지 api요청
    @GetMapping("/reservation/status")
    @Operation( summary = "양도 결제 완료 조회",
            description = "Websocket 메시지 누락 시, " +
                    "양도 결제 완료 혹은 실패를 확인합니다. ")
    public ResponseEntity<SuccessResponse<Boolean>> checkStatus(@RequestParam Long transferId){
        return ApiResponseUtil.success(transferService.checkStatus(transferId));
    }
}
