package com.yujianghuai.admin.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "用户视图对象")
public class UserVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "登录账号")
    private String username;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "租户编码")
    private String tenant;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "角色ID列表")
    private List<Long> roleIds = new ArrayList<>();

    @Schema(description = "角色编码列表")
    private List<String> roles = new ArrayList<>();
}
