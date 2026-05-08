package com.yujianghuai.common.utils;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 当前登录用户信息。
 *
 * @author yxh
 * @date 2026/5/8
 */
@Schema(description = "当前登录用户信息")
public class CurrentUserInfo {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "登录账号")
    private String username;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "租户编码")
    private String tenant;

    public CurrentUserInfo() {
    }

    public CurrentUserInfo(Long id, String username, String nickname, String realName, String tenant) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.realName = realName;
        this.tenant = tenant;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
}
