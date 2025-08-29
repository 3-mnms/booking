package com.mnms.booking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class OcrService {

    @Value("${ocr.key}")
    private String ocrKey;

    @Value("${ocr.invoke_url}")
    private String ocrInvokeUrl;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String callOcr(MultipartFile image){
        try {
            String extension = getExtension(image.getOriginalFilename());
            if (!"pdf".equalsIgnoreCase(extension)) {
                throw new BusinessException(ErrorCode.TRANSFER_NOT_VALID_FILE_TYPE);
            }

            // 2. MultipartFile → Resource
            ByteArrayResource fileResource = new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> multipartBody = getApiBody(image, fileResource);

            // 5. 헤더 구성
            HttpHeaders headers = getApiHeaders();

            return postApi(headers, multipartBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String postApi(HttpHeaders headers, MultiValueMap<String, Object> multipartBody) {
        RestClient restClient=RestClient.create();
        return restClient.post()
                .uri(ocrInvokeUrl)
                .headers(h -> h.addAll(headers))
                .body(multipartBody)
                .retrieve()
                .body(String.class);
    }

    private HttpHeaders getApiHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("X-OCR-SECRET", ocrKey);
        return headers;
    }

    private static MultiValueMap<String, Object> getApiBody(MultipartFile image, ByteArrayResource fileResource) throws JsonProcessingException {
        Map<String,Object> messageBody = new HashMap<>();
        messageBody.put("version","V2");
        messageBody.put("requestId", UUID.randomUUID().toString());
        messageBody.put("timestamp",System.currentTimeMillis());
        messageBody.put("lang","ko");
        messageBody.put("enableTableDetection",false);

        Map<String, Object> imageInfo = new HashMap<>();
        imageInfo.put("format", "pdf");
        imageInfo.put("name", image.getOriginalFilename());

        messageBody.put("images", new Object[]{imageInfo});

        String messageJson = objectMapper.writeValueAsString(messageBody);

        // 4. multipart/form-data body 구성
        MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
        multipartBody.add("file", fileResource);
        multipartBody.add("message", messageJson);
        return multipartBody;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
