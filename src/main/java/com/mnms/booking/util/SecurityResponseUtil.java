package com.mnms.booking.util;

import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityResponseUtil {
    // Authentication에서 userId 추출
    public Long requireUserId(Authentication authentication) {
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.USER_INVALID);
        }
    }
}
