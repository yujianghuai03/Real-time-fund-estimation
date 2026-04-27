package com.yujianghuai.biz.fund.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "基金分组请求")
public class FundGroupRequest {

    @NotBlank
    @Schema(description = "分组名称")
    private String name;
}
