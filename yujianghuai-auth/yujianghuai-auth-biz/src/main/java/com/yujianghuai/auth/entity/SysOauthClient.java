package com.yujianghuai.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yujianghuai.common.entity.BaseEntity;
import com.yujianghuai.common.tenant.TenantTable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TenantTable(false)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_oauth_client")
public class SysOauthClient extends BaseEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;
    private String clientId;
    private String clientSecret;
    private String clientName;
    private String clientAuthenticationMethods;
    private String authorizationGrantTypes;
    private String redirectUris;
    private String postLogoutRedirectUris;
    private String scopes;
    private Integer accessTokenTtl;
    private Integer refreshTokenTtl;
    private Integer requireAuthorizationConsent;
    private Integer reuseRefreshTokens;
    private Integer status;
}
