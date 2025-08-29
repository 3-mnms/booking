package com.mnms.booking.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mnms.booking.dto.response.PersonInfoResponseDTO;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OcrParserService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<PersonInfoResponseDTO> parseOcrResult(
            String ocrJson, Map<String, String> targetInfo) throws IOException {

        List<String> ocrTexts = extractOcrTexts(ocrJson);
        return targetInfo.entrySet().stream()
                .map(entry -> matchPersonInfo(entry.getKey(), entry.getValue(), ocrTexts))
                .collect(Collectors.toList());
    }

    /// OCR 2차 추출
    private static List<String> extractOcrTexts(String ocrJson) throws IOException {
        JsonNode root = objectMapper.readTree(ocrJson);
        JsonNode images = root.path("images");

        List<String> texts = new ArrayList<>();
        for (JsonNode imageNode : images) {
            JsonNode fields = imageNode.path("fields");
            for (JsonNode field : fields) {
                texts.add(field.path("inferText").asText());
            }
        }
        return texts;
    }

    /// OCR 3차 추출
    private static PersonInfoResponseDTO matchPersonInfo(
            String name, String rrn, List<String> ocrTexts) {

        // 임시 수정
        String normalizedNamePattern = name.replaceAll("\\s", "").replaceAll("\\\\[bQE]", "");
        log.info("normalizedNamePattern : {}", normalizedNamePattern);

        String matchedName = ocrTexts.stream()
                .map(t -> t.replaceAll("\\([^)]*\\)", "")
                        .replaceAll("[\\s\\(\\)\\[\\]{}]", ""))
                .filter(t -> t.contains(normalizedNamePattern))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSFER_NOT_FOUND_NAME));

        String matchedRrn = ocrTexts.stream()
                .filter(t -> t.contains(rrn) && t.matches(".*\\d{6}-[1-4].*"))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSFER_NOT_FOUND_RRN));

        /* 임시 삭제
        if (!name.equals(matchedName)) {
            throw new BusinessException(ErrorCode.TRANSFER_NOT_FOUND_NAME);
        }*/

        return new PersonInfoResponseDTO(matchedName, matchedRrn);
    }
}