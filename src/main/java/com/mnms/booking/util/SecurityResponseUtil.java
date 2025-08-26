package com.mnms.booking.util;

import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@Slf4j
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

    // Authentication에서 name 추출
    public String requireName(Authentication authentication) {
        log.info("authentication : {}", authentication);
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new BusinessException(ErrorCode.USER_INVALID);
        }

        Jwt jwt = jwtAuth.getToken();
        String name = jwt.getClaimAsString("name");

        if (name == null || name.isEmpty()) {
            throw new BusinessException(ErrorCode.USER_INVALID);
        }

        return name;
    }

}
