package com.yujianghuai.admin.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "菜单新增或修改请求")
public class MenuRequest {

    @NotNull
    @Schema(description = "父级菜单ID")
    private Long parentId;

    @NotBlank
    @Schema(description = "菜单类型")
    private String menuType;

    @NotBlank
    @Schema(description = "菜单归属端 ADMIN/PORTAL")
    private String menuScope;

    @NotBlank
    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "权限标识")
    private String permission;

    @Schema(description = "路由地址")
    private String path;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "请求方法")
    private String method;

    @Schema(description = "接口路径")
    private String apiPath;

    @Schema(description = "显示排序")
    private Integer sortOrder = 0;

    @Schema(description = "是否显示")
    private Integer visible = 1;

    @Schema(description = "状态")
    private Integer status = 1;
}
