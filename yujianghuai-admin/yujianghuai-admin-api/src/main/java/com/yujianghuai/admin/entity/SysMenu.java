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
@Schema(description = "系统菜单实体")
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    @Schema(description = "菜单ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "父级菜单ID")
    private Long parentId;

    @Schema(description = "菜单类型")
    private String menuType;

    @Schema(description = "菜单归属端 ADMIN/PORTAL")
    private String menuScope;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "权限标识")
    private String permission;

    @Schema(description = "路由地址")
    private String path;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "请求方法")
    private String method;

    @Schema(description = "接口路径")
    private String apiPath;

    @Schema(description = "显示排序")
    private Integer sortOrder;

    @Schema(description = "是否显示")
    private Integer visible;

    @Schema(description = "状态")
    private Integer status;
}
