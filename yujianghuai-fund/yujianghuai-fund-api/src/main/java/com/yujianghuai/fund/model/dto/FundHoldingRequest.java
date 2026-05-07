package com.yujianghuai.fund.model.dto;

import com.yujianghuai.fund.model.validation.CreateGroup;
import com.yujianghuai.fund.model.validation.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "用户基金持仓新增或修改请求")
public class FundHoldingRequest {

    @Schema(description = "投资组合ID")
    private Long portfolioId;

    @NotNull(groups = CreateGroup.class)
    @Schema(description = "基金ID")
    private Long fundId;

    @NotNull(groups = CreateGroup.class)
    @DecimalMin(value = "0.0000", groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "持有份额")
    private BigDecimal holdingShares;

    @DecimalMin(value = "0.0000", groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "可用份额")
    private BigDecimal availableShares;

    @DecimalMin(value = "0.0000", groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "冻结份额")
    private BigDecimal frozenShares;

    @NotNull(groups = CreateGroup.class)
    @DecimalMin(value = "0.0000", groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "持仓成本金额")
    private BigDecimal costAmount;

    @DecimalMin(value = "0.000000", groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "持仓成本净值")
    private BigDecimal costNav;

    @DecimalMin(value = "0.0000", groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "当前市值")
    private BigDecimal marketValue;

    @DecimalMin(value = "0.000000", groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "最新单位净值")
    private BigDecimal latestNav;

    @Schema(description = "最新净值日期")
    private LocalDate latestNavDate;

    @DecimalMin(value = "0.0000", groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "当日估算市值")
    private BigDecimal todayEstimatedValue;

    @Schema(description = "当日收益")
    private BigDecimal todayProfit;

    @Schema(description = "首次买入日期")
    private LocalDate firstBuyDate;

    @Schema(description = "最近交易时间")
    private LocalDateTime lastTradeTime;

    @Schema(description = "关联分组ID列表")
    private List<Long> groupIds;

    @Min(value = 0, groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "排序")
    private Integer sortOrder;

    @Min(value = 0, groups = {CreateGroup.class, UpdateGroup.class})
    @Max(value = 1, groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "状态，0清仓 1持有")
    private Integer status;
}
