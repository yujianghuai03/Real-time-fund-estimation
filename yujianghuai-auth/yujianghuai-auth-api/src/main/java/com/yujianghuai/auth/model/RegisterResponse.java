package com.yujianghuai.auth.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "注册结果")
public class RegisterResponse {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "租户名称")
    private String tenantName;

    public RegisterResponse() {
    }

    public RegisterResponse(String username, String nickname, String tenantName) {
        this.username = username;
        this.nickname = nickname;
        this.tenantName = tenantName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }
}
