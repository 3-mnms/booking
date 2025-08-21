package com.mnms.booking.controller;

import com.mnms.booking.exception.global.SuccessResponse;
import com.mnms.booking.service.TransferService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
@Tag(name = "양도 API", description = "양도 및 OCR")
@Slf4j
public class TransferController {
    private final TransferService transferService;

    @PostMapping
    public void transferController(@RequestParam("file") MultipartFile image, HttpServletRequest request){
        transferService.callOcr(image);
    }
}
