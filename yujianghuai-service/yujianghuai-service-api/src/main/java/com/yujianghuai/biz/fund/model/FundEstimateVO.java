package com.yujianghuai.biz.fund.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(description = "基金估值视图对象")
public class FundEstimateVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "基金代码")
    private String code;

    @Schema(description = "基金名称")
    private String name;

    @Schema(description = "持有金额")
    private BigDecimal holdingAmount;

    @Schema(description = "净值日期")
    private String navDate;

    @Schema(description = "上一交易日净值")
    private BigDecimal previousNav;

    @Schema(description = "预估净值")
    private BigDecimal estimateNav;

    @Schema(description = "预估涨跌幅")
    private BigDecimal estimateRate;

    @Schema(description = "预估收益")
    private BigDecimal estimateProfit;

    @Schema(description = "预估市值")
    private BigDecimal estimateMarketValue;

    @Schema(description = "预估时间")
    private String estimateTime;

    @Schema(description = "错误信息")
    private String error;
}
