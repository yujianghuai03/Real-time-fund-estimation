package com.yujianghuai.biz.fund.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yujianghuai.common.tenant.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TenantTable
@Schema(description = "biz_fund_realtime_estimate")
@TableName("biz_fund_realtime_estimate")
public class BizFundRealtimeEstimate {

    @Schema(description = "实时估值ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "租户ID，关联sys_tenant.id")
    private Long tenantId;

    @Schema(description = "基金ID，关联biz_fund_info.id")
    private Long fundId;

    @Schema(description = "基金代码，冗余用于查询")
    private String fundCode;

    @Schema(description = "估值时间")
    private LocalDateTime estimateTime;

    @Schema(description = "预估单位净值")
    private BigDecimal estimateNav;

    @Schema(description = "预估涨跌幅百分比")
    private BigDecimal estimateGrowthRate;

    @Schema(description = "预估净值变动金额")
    private BigDecimal estimateAmount;

    @Schema(description = "上一交易日单位净值")
    private BigDecimal previousNav;

    @Schema(description = "市场状态: TRADING交易中 CLOSED已收盘")
    private String marketStatus;

    @Schema(description = "数据来源")
    private String source;

    @Schema(description = "来源更新时间")
    private LocalDateTime sourceUpdateTime;

    @Schema(description = "异常标记: 0正常 1异常")
    private Integer abnormalFlag;

    @Schema(description = "异常原因")
    private String abnormalReason;

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
