package com.yujianghuai.fund.entity;

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
@Schema(description = "biz_user_fund_daily_snapshot")
@TableName("biz_user_fund_daily_snapshot")
public class BizUserFundDailySnapshot {

    @Schema(description = "每日快照ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "租户ID，关联sys_tenant.id")
    private Long tenantId;

    @Schema(description = "用户ID，关联sys_user.id")
    private Long userId;

    @Schema(description = "投资组合ID，关联biz_user_portfolio.id")
    private Long portfolioId;

    @Schema(description = "持仓ID，关联biz_user_fund_holding.id")
    private Long holdingId;

    @Schema(description = "基金ID，关联biz_fund_info.id；为空表示组合汇总快照")
    private Long fundId;

    @Schema(description = "快照日期")
    private LocalDate snapshotDate;

    @Schema(description = "快照类型: FUND基金 PORTFOLIO组合 USER用户汇总")
    private String snapshotType;

    @Schema(description = "总成本")
    private BigDecimal totalCost;

    @Schema(description = "市值")
    private BigDecimal marketValue;

    @Schema(description = "估算市值")
    private BigDecimal estimatedValue;

    @Schema(description = "当日收益")
    private BigDecimal dailyProfit;

    @Schema(description = "持有收益")
    private BigDecimal holdingProfit;

    @Schema(description = "持有收益率百分比")
    private BigDecimal holdingProfitRate;

    @Schema(description = "年化收益率百分比")
    private BigDecimal annualizedReturnRate;

    @Schema(description = "最大回撤百分比")
    private BigDecimal maxDrawdownRate;

    @Schema(description = "资产配置快照JSON")
    private String assetAllocation;

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
