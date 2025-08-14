package com.mnms.booking.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final RsaJwtVerifier jwt;

    public JwtAuthFilter(RsaJwtVerifier jwt) { this.jwt = jwt; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader(org.springframework.http.HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims c = jwt.parse(token);
                Long userId = c.get("userId", Long.class);
                String loginId = c.getSubject();
                // B 서비스는 "role": "USER" 형태니까 이렇게 권한 만들기
                String role = c.get("role", String.class);
                List<GrantedAuthority> auths = (role == null) ? List.of() : List.of(new SimpleGrantedAuthority("ROLE_" + role));
                JwtPrincipal principal = new JwtPrincipal(userId, loginId, role == null ? List.of() : List.of(role));

                var authentication = new UsernamePasswordAuthenticationToken(principal, null, auths);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException e) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        chain.doFilter(req, res);
    }
}