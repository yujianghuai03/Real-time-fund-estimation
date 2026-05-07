package com.yujianghuai.fund.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(description = "用户基金持仓汇总响应")
public class FundHoldingSummaryVO {

    @Schema(description = "汇总维度")
    private String dimension;

    @Schema(description = "维度ID")
    private Long dimensionId;

    @Schema(description = "维度名称")
    private String dimensionName;

    @Schema(description = "持仓基金数量")
    private Long holdingCount;

    @Schema(description = "持有份额合计")
    private BigDecimal totalHoldingShares;

    @Schema(description = "持仓成本合计")
    private BigDecimal totalCostAmount;

    @Schema(description = "持仓市值合计")
    private BigDecimal totalMarketValue;

    @Schema(description = "当日估算市值合计")
    private BigDecimal totalTodayEstimatedValue;

    @Schema(description = "当日收益合计")
    private BigDecimal totalTodayProfit;

    @Schema(description = "持仓收益合计")
    private BigDecimal totalHoldingProfit;

    @Schema(description = "盈亏金额合计")
    private BigDecimal totalProfitLossAmount;

    @Schema(description = "持仓收益率百分比")
    private BigDecimal totalHoldingProfitRate;
}
