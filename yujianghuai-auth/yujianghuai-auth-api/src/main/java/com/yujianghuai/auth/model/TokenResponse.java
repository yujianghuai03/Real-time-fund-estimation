package com.yujianghuai.auth.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "登录令牌响应")
public class TokenResponse {

    @Schema(description = "令牌类型")
    private String tokenType = "Bearer";

    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "过期秒数")
    private long expiresIn;

    public TokenResponse(String accessToken, long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
