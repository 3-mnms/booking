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
import java.util.regex.Pattern;
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

        List<String> cleanedOcrTexts = ocrTexts.stream()
                .map(t -> t.replaceAll("\\([^)]*\\)", "")
                        .replaceAll("[\\s\\(\\)\\[\\]{}]", ""))
                .toList();

        String normalizedName = name.replaceAll("\\s", "").replaceAll("\\\\[bQE]", "");
        String foundName = null, foundRrn = null;

        for (String text : cleanedOcrTexts) {
            if (foundName == null && text.contains(normalizedName)) foundName = text;
            if (foundRrn == null && text.contains(rrn) && text.matches(".*\\d{6}-[1-4].*")) {
                foundRrn = text;
            }
            if (foundName != null && foundRrn != null) break;
        }

        if (foundName == null) {
            throw new BusinessException(ErrorCode.TRANSFER_NOT_FOUND_NAME);
        }
        if (foundRrn == null) {
            throw new BusinessException(ErrorCode.TRANSFER_NOT_FOUND_RRN);
        }
        return new PersonInfoResponseDTO(foundName, foundRrn);
    }
}