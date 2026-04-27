package com.yujianghuai.auth.service;

import com.yujianghuai.auth.config.AuthProperties;
import com.yujianghuai.common.exception.BizException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private static final String UNAUTHORIZED_MESSAGE = "权限不足，请登录后再试！";

    private final AuthProperties properties;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public TokenService(AuthProperties properties, JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.properties = properties;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    public String createToken(String username,
                              String tenantId,
                              String tenantName,
                              List<String> authorities,
                              long expireSeconds) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(expireSeconds);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getOauth2().getIssuer())
                .subject(username)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("scope", String.join(" ", properties.getOauth2().getScopes()))
                .claim("tenant_id", tenantId)
                .claim("tenant_name", tenantName)
                .claim("authorities", authorities)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public TokenPayload verify(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            Instant expiresAt = jwt.getExpiresAt();
            if (expiresAt == null || expiresAt.isBefore(Instant.now())) {
                throw new BizException(401, UNAUTHORIZED_MESSAGE);
            }
            Map<String, Object> claims = new LinkedHashMap<>(jwt.getClaims());
            return new TokenPayload(jwt.getSubject(), expiresAt.getEpochSecond(), claims);
        } catch (BizException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new BizException(401, UNAUTHORIZED_MESSAGE);
        }
    }

    public record TokenPayload(String username, long expiresAt, Map<String, Object> claims) {
    }
}
