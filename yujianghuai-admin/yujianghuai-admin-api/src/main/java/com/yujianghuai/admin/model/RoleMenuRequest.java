package com.yujianghuai.admin.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "角色菜单授权请求")
public class RoleMenuRequest {

    @Schema(description = "菜单ID列表")
    private List<Long> menuIds = new ArrayList<>();
}
