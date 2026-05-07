package com.yujianghuai.biz.fund.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yujianghuai.common.tenant.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TenantTable
@Schema(description = "biz_user_fund_holding")
@TableName("biz_user_fund_holding")
public class BizUserFundHolding {

    @Schema(description = "持仓ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "租户ID，关联sys_tenant.id")
    private Long tenantId;

    @Schema(description = "用户ID，关联sys_user.id")
    private Long userId;

    @Schema(description = "投资组合ID，关联biz_user_portfolio.id")
    private Long portfolioId;

    @Schema(description = "基金ID，关联biz_fund_info.id")
    private Long fundId;

    @Schema(description = "基金代码，冗余用于查询")
    private String fundCode;

    @Schema(description = "基金名称，冗余用于展示")
    private String fundName;

    @Schema(description = "持有份额")
    private BigDecimal holdingShares;

    @Schema(description = "可用份额")
    private BigDecimal availableShares;

    @Schema(description = "冻结份额")
    private BigDecimal frozenShares;

    @Schema(description = "持仓成本金额")
    private BigDecimal costAmount;

    @Schema(description = "持仓成本净值")
    private BigDecimal costNav;

    @Schema(description = "当前市值")
    private BigDecimal marketValue;

    @Schema(description = "最新单位净值")
    private BigDecimal latestNav;

    @Schema(description = "最新净值日期")
    private LocalDate latestNavDate;

    @Schema(description = "当日估算市值")
    private BigDecimal todayEstimatedValue;

    @Schema(description = "当日收益")
    private BigDecimal todayProfit;

    @Schema(description = "持有收益")
    private BigDecimal holdingProfit;

    @Schema(description = "持有收益率百分比")
    private BigDecimal holdingProfitRate;

    @Schema(description = "首次买入日期")
    private LocalDate firstBuyDate;

    @Schema(description = "最近交易时间")
    private LocalDateTime lastTradeTime;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态: 0清仓 1持有")
    private Integer status;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "修改人")
    private String updateBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "删除标记，0未删除，1已删除")
    @TableLogic
    private String delFlag;

}
