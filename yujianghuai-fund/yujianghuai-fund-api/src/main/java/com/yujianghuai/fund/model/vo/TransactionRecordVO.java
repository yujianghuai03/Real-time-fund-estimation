package com.yujianghuai.fund.model.vo;

import com.yujianghuai.fund.model.enums.TransactionStatusEnum;
import com.yujianghuai.fund.model.enums.TransactionTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Schema(description = "交易记录响应")
public class TransactionRecordVO {

    @Schema(description = "交易记录ID")
    private Long id;

    @Schema(description = "投资组合ID")
    private Long portfolioId;

    @Schema(description = "持仓ID")
    private Long holdingId;

    @Schema(description = "基金ID")
    private Long fundId;

    @Schema(description = "基金代码")
    private String fundCode;

    @Schema(description = "基金名称")
    private String fundName;

    @Schema(description = "交易类型")
    private TransactionTypeEnum tradeType;

    @Schema(description = "交易类型名称")
    private String tradeTypeName;

    @Schema(description = "交易状态")
    private TransactionStatusEnum tradeStatus;

    @Schema(description = "交易状态名称")
    private String tradeStatusName;

    @Schema(description = "交易时间")
    private LocalDateTime tradeTime;

    @Schema(description = "确认日期")
    private LocalDate confirmDate;

    @Schema(description = "交易金额")
    private BigDecimal amount;

    @Schema(description = "手续费")
    private BigDecimal fee;

    @Schema(description = "确认净值")
    private BigDecimal confirmNav;

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

    @Schema(description = "目标基金代码")
    private String targetFundCode;

    @Schema(description = "目标基金名称")
    private String targetFundName;

    @Schema(description = "交易渠道")
    private String sourceChannel;

    @Schema(description = "外部交易流水号")
    private String externalTradeNo;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
