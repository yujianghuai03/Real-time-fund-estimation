package com.yujianghuai.fund.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
@Schema(description = "交易确认请求")
public class TransactionConfirmRequest {

    @Schema(description = "确认日期，不传默认当前日期")
    private LocalDate confirmDate;

    @DecimalMin("0.000000")
    @Schema(description = "确认净值")
    private BigDecimal confirmNav;

    @DecimalMin("0.0000")
    @Schema(description = "确认份额")
    private BigDecimal confirmShares;

    @DecimalMin("0.0000")
    @Schema(description = "手续费")
    private BigDecimal fee;

    @Schema(description = "交易前份额")
    private BigDecimal beforeShares;

    @Schema(description = "交易后份额")
    private BigDecimal afterShares;

    @Schema(description = "交易前金额")
    private BigDecimal beforeAmount;

    @Schema(description = "交易后金额")
    private BigDecimal afterAmount;
}
