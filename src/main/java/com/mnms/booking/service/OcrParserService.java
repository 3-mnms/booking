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

@Service
@Slf4j
public class OcrParserService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<PersonInfoResponseDTO> parseOcrResult(
            String ocrJson, Map<String, String> targetInfo) throws IOException {

        List<PersonInfoResponseDTO> result = new ArrayList<>();

        JsonNode root = objectMapper.readTree(ocrJson);
        JsonNode images = root.path("images");

        // OCR 전체 텍스트 수집
        List<String> ocrTexts = new ArrayList<>();
        for (JsonNode imageNode : images) {
            JsonNode fields = imageNode.path("fields");
            for (JsonNode field : fields) {
                ocrTexts.add(field.path("inferText").asText());
            }
        }

        log.info("ocr : {}", ocrTexts);

        for (Map.Entry<String, String> entry : targetInfo.entrySet()) {
            String name = entry.getKey();
            String rrn = entry.getValue();

            Optional<String> matchedName = ocrTexts.stream()
                    .map(t -> t.replaceAll("[\\s\\(\\)\\[\\]{}]", ""))
                    .filter(t -> t.matches(".*\\b" + Pattern.quote(name) + "\\b.*"))
                    .findFirst();

            Optional<String> matchedRrn = ocrTexts.stream()
                    .filter(t -> t.contains(rrn) && t.matches(".*\\d{6}-[1-4].*"))
                    .findFirst();

            log.info("user : {}, {}", matchedName, matchedRrn);

            // 결과 확인
            if (matchedName.isEmpty() || matchedRrn.isEmpty()) {
                throw new BusinessException(ErrorCode.TRANSFER_NOT_FOUND_INFORM);
            }

            if (!name.equals(matchedName.get())) {
                throw new BusinessException(ErrorCode.TRANSFER_NOT_FOUND_NAME);
            }

            result.add(new PersonInfoResponseDTO(
                    matchedName.get(),
                    matchedRrn.get()
            ));
        }

        return result;
    }
}
