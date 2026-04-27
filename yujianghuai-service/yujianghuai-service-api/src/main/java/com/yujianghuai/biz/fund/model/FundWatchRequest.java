package com.yujianghuai.biz.fund.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(description = "新增自选基金请求")
public class FundWatchRequest {

    @NotBlank
    @Schema(description = "基金代码")
    private String code;

    @Schema(description = "基金名称")
    private String name;

    @DecimalMin("0")
    @Schema(description = "持有金额")
    private BigDecimal holdingAmount = BigDecimal.ZERO;

    @DecimalMin("0")
    @Schema(description = "持仓成本")
    private BigDecimal holdingCost = BigDecimal.ZERO;
}
