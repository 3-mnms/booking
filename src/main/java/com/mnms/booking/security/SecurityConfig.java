package com.mnms.booking.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final HeaderAuthenticationFilter headerAuthFilter;

    public SecurityConfig(HeaderAuthenticationFilter headerAuthFilter) {
        this.headerAuthFilter = headerAuthFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/host/booking/list").hasRole("HOST") // HOST ROLE 설정
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(
                                "/public/**",
                                "/h2-console/**",
                                "/api/captcha/**",
                                "/api/qr/**",
                                "/api/booking/detail/phases/1",
                                "/api/host/**",
                                "/api/booking/confirm"
                        ).permitAll() // 하위 경로 포함 허용
                        .anyRequest().permitAll()
                )
                .addFilterBefore(headerAuthFilter, AuthorizationFilter.class)
                .build();
    }
}