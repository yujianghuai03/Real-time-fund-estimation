package com.yujianghuai.admin.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "菜单树视图对象")
public class MenuVO {

    @Schema(description = "菜单ID")
    private Long id;

    @Schema(description = "父级菜单ID")
    private Long parentId;

    @Schema(description = "菜单类型")
    private String type;

    @Schema(description = "菜单归属端 ADMIN/PORTAL")
    private String scope;

    @Schema(description = "菜单标题")
    private String title;

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

    @Schema(description = "是否显示")
    private Boolean visible;

    @Schema(description = "显示排序")
    private Integer sort;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "子菜单列表")
    private List<MenuVO> children = new ArrayList<>();
}
