package com.yujianghuai.auth.repository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class RedisOAuth2AuthorizationService implements OAuth2AuthorizationService {

    private static final String AUTHORIZATION_KEY_PREFIX = "auth:oauth2:authorization:";
    private static final String TOKEN_INDEX_KEY_PREFIX = "auth:oauth2:token:";

    private final StringRedisTemplate stringRedisTemplate;

    public RedisOAuth2AuthorizationService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        OAuth2Authorization existingAuthorization = findById(authorization.getId());
        if (existingAuthorization != null) {
            deleteTokenIndexes(existingAuthorization);
        }

        Duration ttl = resolveAuthorizationTtl(authorization);
        stringRedisTemplate.opsForValue().set(authorizationKey(authorization.getId()), serialize(authorization), ttl);
        saveTokenIndexes(authorization, ttl);
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        stringRedisTemplate.delete(authorizationKey(authorization.getId()));
        deleteTokenIndexes(authorization);
    }

    @Override
    public OAuth2Authorization findById(String id) {
        Assert.hasText(id, "id cannot be empty");
        String value = stringRedisTemplate.opsForValue().get(authorizationKey(id));
        return StringUtils.hasText(value) ? deserialize(value) : null;
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        Assert.hasText(token, "token cannot be empty");
        if (tokenType != null) {
            return findByTokenIndex(tokenType.getValue(), token);
        }

        for (String tokenTypeValue : List.of(
                OAuth2TokenType.ACCESS_TOKEN.getValue(),
                OAuth2TokenType.REFRESH_TOKEN.getValue(),
                "code")) {
            OAuth2Authorization authorization = findByTokenIndex(tokenTypeValue, token);
            if (authorization != null) {
                return authorization;
            }
        }
        return null;
    }

    private void saveTokenIndexes(OAuth2Authorization authorization, Duration ttl) {
        List<TokenIndex> tokenIndexes = resolveTokenIndexes(authorization);
        for (TokenIndex tokenIndex : tokenIndexes) {
            stringRedisTemplate.opsForValue().set(tokenIndexKey(tokenIndex.tokenType(), tokenIndex.tokenValue()),
                    authorization.getId(), ttl);
        }
    }

    private void deleteTokenIndexes(OAuth2Authorization authorization) {
        List<String> keys = resolveTokenIndexes(authorization).stream()
                .map(tokenIndex -> tokenIndexKey(tokenIndex.tokenType(), tokenIndex.tokenValue()))
                .toList();
        if (!keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    private List<TokenIndex> resolveTokenIndexes(OAuth2Authorization authorization) {
        List<TokenIndex> tokenIndexes = new ArrayList<>();
        OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getAccessToken();
        if (accessToken != null) {
            tokenIndexes.add(new TokenIndex(OAuth2TokenType.ACCESS_TOKEN.getValue(), accessToken.getToken().getTokenValue()));
        }
        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = authorization.getRefreshToken();
        if (refreshToken != null) {
            tokenIndexes.add(new TokenIndex(OAuth2TokenType.REFRESH_TOKEN.getValue(), refreshToken.getToken().getTokenValue()));
        }
        OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode =
                authorization.getToken(OAuth2AuthorizationCode.class);
        if (authorizationCode != null) {
            tokenIndexes.add(new TokenIndex("code", authorizationCode.getToken().getTokenValue()));
        }
        return tokenIndexes;
    }

    private OAuth2Authorization findByTokenIndex(String tokenType, String token) {
        String authorizationId = stringRedisTemplate.opsForValue().get(tokenIndexKey(tokenType, token));
        return StringUtils.hasText(authorizationId) ? findById(authorizationId) : null;
    }

    private Duration resolveAuthorizationTtl(OAuth2Authorization authorization) {
        Instant expiresAt = null;
        if (authorization.getRefreshToken() != null) {
            expiresAt = authorization.getRefreshToken().getToken().getExpiresAt();
        }
        if (expiresAt == null && authorization.getAccessToken() != null) {
            expiresAt = authorization.getAccessToken().getToken().getExpiresAt();
        }
        if (expiresAt == null) {
            return Duration.ofMinutes(10);
        }

        long seconds = Duration.between(Instant.now(), expiresAt).getSeconds();
        return Duration.ofSeconds(Math.max(seconds, 1));
    }

    private String authorizationKey(String id) {
        return AUTHORIZATION_KEY_PREFIX + id;
    }

    private String tokenIndexKey(String tokenType, String token) {
        return TOKEN_INDEX_KEY_PREFIX + tokenType + ":" + token;
    }

    private String serialize(OAuth2Authorization authorization) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(authorization);
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (Exception exception) {
            throw new IllegalStateException("序列化OAuth2授权信息失败", exception);
        }
    }

    private OAuth2Authorization deserialize(String value) {
        byte[] bytes = Base64.getDecoder().decode(value);
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (OAuth2Authorization) objectInputStream.readObject();
        } catch (Exception exception) {
            throw new IllegalStateException("反序列化OAuth2授权信息失败", exception);
        }
    }

    private record TokenIndex(String tokenType, String tokenValue) {
    }
}
