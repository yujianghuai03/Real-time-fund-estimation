package com.yujianghuai.biz.fund.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "基金分组视图对象")
public class FundGroupVO {

    @Schema(description = "分组ID")
    private Long id;

    @Schema(description = "分组名称")
    private String name;

    @Schema(description = "基金数量")
    private Long count;
}
