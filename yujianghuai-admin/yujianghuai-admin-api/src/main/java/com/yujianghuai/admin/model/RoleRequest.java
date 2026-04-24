package com.yujianghuai.admin.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "角色新增或修改请求")
public class RoleRequest {

    @NotBlank
    @Schema(description = "角色编码")
    private String roleCode;

    @NotBlank
    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "数据权限范围")
    private Integer dataScope = 5;

    @Schema(description = "显示排序")
    private Integer sortOrder = 0;

    @Schema(description = "状态")
    private Integer status = 1;
}
