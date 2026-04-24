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
@Schema(description = "系统角色实体")
@TableName("sys_role")
public class SysRole extends BaseEntity {

    @Schema(description = "角色ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;
    @Schema(description = "角色编码")
    private String roleCode;
    @Schema(description = "角色名称")
    private String roleName;
    @Schema(description = "角色类型")
    private Integer roleType;
    @Schema(description = "数据权限范围")
    private Integer dataScope;
    @Schema(description = "显示排序")
    private Integer sortOrder;
    @Schema(description = "状态")
    private Integer status;
}
