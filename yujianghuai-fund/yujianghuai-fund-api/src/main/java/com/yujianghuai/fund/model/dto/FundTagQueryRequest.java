package com.yujianghuai.fund.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "基金标签查询请求")
public class FundTagQueryRequest {

    @Schema(description = "标签名称")
    private String tagName;

    @Schema(description = "标签类型")
    private String tagType;

    @Schema(description = "状态，0禁用 1启用")
    private Integer status;
}
