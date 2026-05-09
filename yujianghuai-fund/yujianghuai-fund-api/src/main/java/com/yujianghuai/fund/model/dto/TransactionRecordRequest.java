package com.yujianghuai.fund.model.dto;

import com.yujianghuai.fund.model.enums.TransactionStatusEnum;
import com.yujianghuai.fund.model.enums.TransactionTypeEnum;
import com.yujianghuai.fund.model.validation.CreateGroup;
import com.yujianghuai.fund.model.validation.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Schema(description = "交易记录新增或修改请求")
public class TransactionRecordRequest {

    @Schema(description = "投资组合ID")
    private Long portfolioId;

    @Schema(description = "持仓ID")
    private Long holdingId;

    @NotNull(groups = CreateGroup.class)
    @Schema(description = "基金ID")
    private Long fundId;

    @NotNull(groups = CreateGroup.class)
    @Schema(description = "交易类型")
    private TransactionTypeEnum tradeType;

    @Schema(description = "交易状态")
    private TransactionStatusEnum tradeStatus;

    @NotNull(groups = CreateGroup.class)
    @Schema(description = "交易时间")
    private LocalDateTime tradeTime;

    @Schema(description = "确认日期")
    private LocalDate confirmDate;

    @NotNull(groups = CreateGroup.class)
    @DecimalMin(value = "0.0000", groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "交易金额")
    private BigDecimal amount;

    @DecimalMin(value = "0.0000", groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "手续费")
    private BigDecimal fee;

    @DecimalMin(value = "0.000000", groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "确认净值")
    private BigDecimal confirmNav;

    @DecimalMin(value = "0.0000", groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "确认份额")
    private BigDecimal confirmShares;

    @Schema(description = "交易前份额")
    private BigDecimal beforeShares;

    @Schema(description = "交易后份额")
    private BigDecimal afterShares;

    @Schema(description = "交易前金额")
    private BigDecimal beforeAmount;

    @Schema(description = "交易后金额")
    private BigDecimal afterAmount;

    @Schema(description = "目标基金ID")
    private Long targetFundId;

    @Schema(description = "交易渠道")
    private String sourceChannel;

    @Schema(description = "外部交易流水号")
    private String externalTradeNo;

    @Schema(description = "备注")
    private String remark;
}
