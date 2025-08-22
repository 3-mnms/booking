package com.mnms.booking.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mnms.booking.dto.request.UpdateTicketRequestDTO;
import com.mnms.booking.dto.response.PersonInfoResponseDTO;
import com.mnms.booking.exception.global.SuccessResponse;
import com.mnms.booking.service.OcrParserService;
import com.mnms.booking.service.OcrService;
import com.mnms.booking.service.TransferService;
import com.mnms.booking.util.ApiResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
@Tag(name = "양도 API", description = "양도 및 OCR, Ticket 재생성")
@Slf4j
public class TransferController {
    private final OcrService ocrService;
    private final TransferService transferService;

    @PostMapping("/extract")
    public ResponseEntity<SuccessResponse<List<PersonInfoResponseDTO>>> extractPersonInfo(
            @RequestPart("file") MultipartFile file,
            @RequestPart("targetInfo") String targetInfoJson) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> targetInfo = objectMapper.readValue(targetInfoJson, new TypeReference<>() {});

        String ocrJson = ocrService.callOcr(file);
        List<PersonInfoResponseDTO> people = OcrParserService.parseOcrResult(ocrJson, targetInfo);

        return ApiResponseUtil.success(people, "가족관계증명서 인증이 완료되었습니다.");
    }

    @PutMapping("/{ticketId}")
    public ResponseEntity<SuccessResponse<Void>> updateTicket(
            @PathVariable Long ticketId,
            @RequestBody UpdateTicketRequestDTO request) {
        transferService.updateTicket(ticketId, request);
        return ApiResponseUtil.success(null, "티켓 양도가 성공적으로 진행되었습니다.");
    }
}
