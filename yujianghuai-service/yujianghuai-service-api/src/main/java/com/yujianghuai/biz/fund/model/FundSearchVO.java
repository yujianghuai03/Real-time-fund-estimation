package com.yujianghuai.biz.fund.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(description = "基金搜索结果对象")
public class FundSearchVO {

    @Schema(description = "基金代码")
    private String code;

    @Schema(description = "基金名称")
    private String name;

    @Schema(description = "基金类型")
    private String type;

    @Schema(description = "基金公司")
    private String company;

    @Schema(description = "最新净值")
    private BigDecimal nav;

    @Schema(description = "净值日期")
    private String navDate;
}
