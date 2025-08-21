package com.mnms.booking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
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

        // 게이트웨이가 붙여주는 헤더 (✅ userId 사용)
        final String userIdHeader = trimToNull(request.getHeader("X-User-Id"));
        final String rolesHdr     = trimToNull(request.getHeader("X-User-Role"));

        Authentication current = SecurityContextHolder.getContext().getAuthentication();
        boolean isAnonymous = (current instanceof AnonymousAuthenticationToken);
        boolean canSetAuth = (current == null) || isAnonymous;

        if (canSetAuth && userIdHeader != null && rolesHdr != null) {
            // userId는 숫자여야 하므로 안전하게 파싱
            final Long userId;
            try {
                userId = Long.valueOf(userIdHeader);
            } catch (NumberFormatException e) {
                // 잘못된 헤더면 인증 세팅 없이 통과
                logger.warn("[HeaderAuth] invalid X-User-Id: " + userIdHeader);
                chain.doFilter(request, response);
                return;
            }

            List<GrantedAuthority> authorities = Arrays.stream(rolesHdr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toUpperCase)
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            logger.info("[HeaderAuth] userId=" + userId + ", rolesHeader=" + rolesHdr
                    + " -> authorities=" + authorities);

            // principal = userId(String) → Controller에서 Long.parseLong(principal.getName()) 가능
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(String.valueOf(userId), null, authorities);
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
