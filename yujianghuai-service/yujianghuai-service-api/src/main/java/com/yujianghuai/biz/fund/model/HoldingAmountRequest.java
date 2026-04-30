package com.yujianghuai.biz.fund.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
@Schema(description = "更新持有金额请求")
public class HoldingAmountRequest {

    @DecimalMin("0")
    @Schema(description = "持有金额")
    private BigDecimal holdingAmount = BigDecimal.ZERO;

    @DecimalMin("0")
    @Schema(description = "持仓成本")
    private BigDecimal holdingCost = BigDecimal.ZERO;

    @DecimalMin("0")
    @Schema(description = "持仓成本净值")
    private BigDecimal holdingCostNav = BigDecimal.ZERO;

    @DecimalMin("0")
    @Schema(description = "持有份额")
    private BigDecimal holdingShares = BigDecimal.ZERO;

    @Schema(description = "首次买入日期")
    private LocalDate firstBuyDate;
}
