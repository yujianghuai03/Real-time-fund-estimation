package com.yujianghuai.biz.fund.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(description = "基金交易记录视图对象")
public class FundTransactionVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "基金代码")
    private String fundCode;

    @Schema(description = "基金名称")
    private String fundName;

    @Schema(description = "交易类型")
    private String tradeType;

    @Schema(description = "交易金额")
    private BigDecimal amount;

    @Schema(description = "操作前持仓金额")
    private BigDecimal beforeAmount;

    @Schema(description = "操作后持仓金额")
    private BigDecimal afterAmount;

    @Schema(description = "目标基金代码")
    private String targetFundCode;

    @Schema(description = "目标基金名称")
    private String targetFundName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "交易时间")
    private String tradeTime;
}
