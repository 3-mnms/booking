package com.mnms.booking.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TargetInfoRequestDTO {
    private Map<String, String> targetInfo;
}