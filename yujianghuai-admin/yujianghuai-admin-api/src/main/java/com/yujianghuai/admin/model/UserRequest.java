package com.yujianghuai.admin.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "用户新增或修改请求")
public class UserRequest {

    @Schema(description = "部门ID")
    private Long deptId;

    @NotBlank
    @Schema(description = "登录账号")
    private String username;

    @Schema(description = "登录密码")
    private String password;

    @NotBlank
    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "状态")
    private Integer status = 1;

    @Schema(description = "角色ID列表")
    private List<Long> roleIds = new ArrayList<>();
}
