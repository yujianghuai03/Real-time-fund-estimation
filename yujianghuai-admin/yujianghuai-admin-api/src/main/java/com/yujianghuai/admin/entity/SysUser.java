package com.yujianghuai.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yujianghuai.common.entity.BaseEntity;
import com.yujianghuai.common.tenant.TenantTable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TenantTable
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统用户实体")
@TableName("sys_user")
public class SysUser extends BaseEntity {

    @Schema(description = "用户ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;
    @Schema(description = "部门ID")
    private Long deptId;
    @Schema(description = "登录账号")
    private String username;
    @Schema(description = "登录密码")
    private String password;
    @Schema(description = "用户昵称")
    private String nickname;
    @Schema(description = "真实姓名")
    private String realName;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "手机号")
    private String mobile;
    @Schema(description = "用户类型")
    private Integer userType;
    @Schema(description = "状态")
    private Integer status;
}
