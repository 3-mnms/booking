package com.mnms.booking.util;

import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.security.AuthDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityResponseUtil {
    // Authentication 에서 userId 추출
    public Long requireUserId(Authentication authentication) {
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.USER_INVALID);
        }
    }

    // Authentication 에서 name 추출
    public String requireName(Authentication authentication) {
        String userName = null;
        log.info("authentication : {}", authentication);
        Object details = authentication.getDetails();
        if (details instanceof AuthDetails d) {
            userName = d.getUserName();
            log.info("authentication name : {}", userName);
        }
        return userName;
    }



    // Authentication에서 ROLE 빼오기
    public List<String> requireRole(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return List.of(); // 빈 리스트 반환
        }

        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

}
