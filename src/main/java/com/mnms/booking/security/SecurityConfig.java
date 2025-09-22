package com.mnms.booking.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                .cors(AbstractHttpConfigurer::disable) // gateway CORS 중복 방지
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/host/list").permitAll()
                        .requestMatchers("/api/host/booking/list").hasAnyRole("HOST", "ADMIN")
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/v3/api-docs","/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(
                                "/public/**",
                                "/h2-console/**",
                                "/api/captcha/**",
                                "/api/qr/**",
                                "/api/booking/detail/phases/1",
                                "/api/booking/confirm",
                                "/api/transfer/**",
                                "/api/transfer/**",
                                "/api/ws/**"
                        ).permitAll() // 하위 경로 포함 허용
                        .anyRequest().permitAll()
                )
                .addFilterBefore(headerAuthFilter, AuthorizationFilter.class)
                .build();
    }
}