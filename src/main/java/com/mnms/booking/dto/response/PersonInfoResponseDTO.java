package com.mnms.booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Data
@Getter
@AllArgsConstructor
@ToString
public class PersonInfoResponseDTO {
        private final String name;
        private final String rrnFront;
}