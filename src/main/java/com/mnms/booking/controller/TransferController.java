package com.mnms.booking.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mnms.booking.dto.request.TicketTransferRequestDTO;
import com.mnms.booking.dto.request.UpdateTicketRequestDTO;
import com.mnms.booking.dto.response.PersonInfoResponseDTO;
import com.mnms.booking.dto.response.TicketResponseDTO;
import com.mnms.booking.dto.response.TicketTransferResponseDTO;
import com.mnms.booking.dto.response.TransferOthersResponseDTO;
import com.mnms.booking.exception.global.SuccessResponse;
import com.mnms.booking.service.OcrParserService;
import com.mnms.booking.service.OcrService;
import com.mnms.booking.service.TransferCompletionService;
import com.mnms.booking.service.TransferService;
import com.mnms.booking.specification.TransferSpecification;
import com.mnms.booking.util.ApiResponseUtil;
import com.mnms.booking.util.SecurityResponseUtil;
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
public class TransferController implements TransferSpecification {
    private final OcrService ocrService;
    private final TransferService transferService;
    private final SecurityResponseUtil securityResponseUtil;
    private final TransferCompletionService transferCompletionService;


    @GetMapping("/transferor")
    public ResponseEntity<SuccessResponse<List<TicketResponseDTO>>> getUserTickets(Authentication authentication) {
        List<TicketResponseDTO> tickets = transferService.getTicketsByUser(securityResponseUtil.requireUserId(authentication));
        return ApiResponseUtil.success(tickets);
    }


    ///  가족관계증명서 인증
    @PostMapping("/extract")
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
    public ResponseEntity<SuccessResponse<Void>> requestTransfer(
            @RequestBody @Valid TicketTransferRequestDTO dto,
            Authentication authentication
    ){
        transferService.requestTransfer(dto, securityResponseUtil.requireUserId(authentication));
        return ApiResponseUtil.success(null, "티켓 양도 요청이 완료되었습니다.");
    }

    /// 양도 요청 받기 (조회)
    @GetMapping("/watch")
    public ResponseEntity<SuccessResponse<List<TicketTransferResponseDTO>>> watchTransfer(Authentication authentication){
        List<TicketTransferResponseDTO> response = transferService.watchTransfer(securityResponseUtil.requireUserId(authentication));
        return ApiResponseUtil.success(response);
    }

    /// 가족 간 양도 요청 승인
    @PutMapping("/acceptance/family")
    public ResponseEntity<SuccessResponse<Void>> responseTicketFamily(
            @RequestBody UpdateTicketRequestDTO request,
            Authentication authentication) {
        transferCompletionService.updateFamilyTicket(request, securityResponseUtil.requireUserId(authentication));
        return ApiResponseUtil.success(null, "티켓 양도가 성공적으로 진행되었습니다.");
    }

    /// 지인간 양도 요청 승인
    @PutMapping("/acceptance/others")
    public ResponseEntity<SuccessResponse<TransferOthersResponseDTO>> responseTicketOthers(
            @RequestBody UpdateTicketRequestDTO request,
            Authentication authentication) {
            TransferOthersResponseDTO response = transferCompletionService.proceedOthersTicket(request, securityResponseUtil.requireUserId(authentication));
        return ApiResponseUtil.success(response);
    }


    ///  Websocket 메시지 누락 방지 api요청
    @GetMapping("/reservation/status")
    public ResponseEntity<SuccessResponse<Boolean>> checkStatus(@RequestParam Long transferId){
        return ApiResponseUtil.success(transferService.checkStatus(transferId));
    }
}
