package com.mnms.booking.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.PublicKey;
import org.springframework.core.io.Resource;

@Component
public class RsaJwtVerifier {
    @Value("${jwt.public-pem-path}")
    private Resource publicPemPath;

    @Value("${jwt.issuer:festival-user-service}")
    private String issuer;

    private PublicKey publicKey;

//    @PostConstruct
//    public void init() {
//        try {
//            String pem = new String(publicPemPath.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
//            this.publicKey = loadPublicKeyFromPem(pem);
//        } catch (IOException e) {
//            throw new IllegalStateException("Failed to read public PEM", e);
//        }
//    }
    @PostConstruct
    public void init() {
        try {
            System.out.println("[DEBUG] resource = " + publicPemPath);
            System.out.println("[DEBUG] exists   = " + publicPemPath.exists());
            byte[] bytes = publicPemPath.getInputStream().readAllBytes();
            System.out.println("[DEBUG] size     = " + bytes.length);

            String peek = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            System.out.println("[DEBUG] first line = " + peek.lines().findFirst().orElse("(empty)"));
            // ↓ 여기서 바로 던지지 말고, 현재 로더로 시도
            this.publicKey = loadPublicKeyFromPem(peek);
            System.out.println("[DEBUG] loaded PublicKey: " + publicKey.getAlgorithm());
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Failed to init RsaJwtVerifier", e);
        }
    }

    public Claims parse(String token) {
        JwtParserBuilder builder = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .setAllowedClockSkewSeconds(30); // 시계 오차 허용
        if (!issuer.isBlank()) builder.requireIssuer(issuer); // 발급자 강제(옵션)
        return builder.build().parseClaimsJws(token).getBody();
    }

    public Long getUserId(String token) {
        return parse(token).get("userId", Long.class);
    }

    public String getLoginId(String token) {
        return parse(token).getSubject(); // sub = loginId
    }

    // --- PEM 로더
    private static PublicKey loadPublicKeyFromPem(String pem) {
        try {
            String content = pem.replace("-----BEGIN PUBLIC KEY-----","")
                    .replace("-----END PUBLIC KEY-----","")
                    .replaceAll("\\s","");
            byte[] der = java.util.Base64.getDecoder().decode(content);
            var spec = new java.security.spec.X509EncodedKeySpec(der);
            return java.security.KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA public key", e);
        }
    }
}