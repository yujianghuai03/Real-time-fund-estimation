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
@Schema(description = "biz_user_fund_transaction")
@TableName("biz_user_fund_transaction")
public class BizUserFundTransaction {

    @Schema(description = "交易记录ID")
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

    @Schema(description = "基金ID，关联biz_fund_info.id")
    private Long fundId;

    @Schema(description = "基金代码，冗余用于查询")
    private String fundCode;

    @Schema(description = "基金名称，冗余用于展示")
    private String fundName;

    @Schema(description = "交易类型: BUY申购 SELL赎回 SWITCH转换 DIVIDEND分红 SIP定投 ADJUST调整")
    private String tradeType;

    @Schema(description = "交易状态: PENDING待确认 CONFIRMED已确认 FAILED失败 CANCELED已取消")
    private String tradeStatus;

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

    @Schema(description = "交易前持仓金额")
    private BigDecimal beforeAmount;

    @Schema(description = "交易后持仓金额")
    private BigDecimal afterAmount;

    @Schema(description = "目标基金ID，转换交易时关联biz_fund_info.id")
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
