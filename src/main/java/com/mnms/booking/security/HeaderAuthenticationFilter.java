package com.mnms.booking.security;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mnms.booking.exception.global.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        // Actuator는 건너뜀
        String uri = request.getRequestURI();
        if (uri != null && uri.startsWith("/actuator")) {
            chain.doFilter(request, response);
            return;
        }

        // 게이트웨이가 붙여주는 헤더
        final String userIdHeader = trimToNull(request.getHeader("X-User-Id"));
        final String rolesHdr = trimToNull(request.getHeader("X-User-Role"));
        final String userNameHeader = trimToNull(request.getHeader("X-User-Name"));

        Authentication current = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isAnonymous = (current instanceof AnonymousAuthenticationToken);
        boolean canSetAuth = (current == null) || isAnonymous;

        if (canSetAuth) {
            // 헤더가 없으면 401
            if (userIdHeader == null || rolesHdr == null) {
                sendUnauthorized(response, "X-User-Id 또는 X-User-Role 헤더가 없습니다.");
                return;
            }

            // userId 파싱
            final Long userId;
            try {
                userId = Long.valueOf(userIdHeader);
            } catch (NumberFormatException e) {
                sendUnauthorized(response, "X-User-Id가 숫자가 아닙니다.");
                return;
            }

            // userName 디코딩
            String userName = "";
            if (userNameHeader != null) {
                try {
                    userName = new String(
                            Base64.getUrlDecoder().decode(userNameHeader),
                            StandardCharsets.UTF_8
                    );
                } catch (IllegalArgumentException e) {
                    log.warn("[HeaderAuth] userName 디코딩 실패: {}", userNameHeader);
                }
            }

            // 권한 세팅
            List<GrantedAuthority> authorities = Arrays.stream(rolesHdr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toUpperCase)
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            log.info("[HeaderAuth] userId={}, roles={} -> authorities={}", userId, rolesHdr, authorities);

            // 인증 정보 세팅
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(String.valueOf(userId), null, authorities);
            auth.setDetails(new AuthDetails(request, userName));
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }

    // 401 응답 직접 처리
    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse error = ErrorResponse.builder()
                .success(false)
                .data("UNAUTHORIZED")
                .message(message)
                .build();
        String body = new ObjectMapper().writeValueAsString(error);
        response.getWriter().write(body);
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}