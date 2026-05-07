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
@Schema(description = "biz_user_fund_sip_plan")
@TableName("biz_user_fund_sip_plan")
public class BizUserFundSipPlan {

    @Schema(description = "定投计划ID")
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

    @Schema(description = "定投计划名称")
    private String planName;

    @Schema(description = "每期定投金额")
    private BigDecimal amount;

    @Schema(description = "定投频率: DAILY每天 WEEKLY每周 MONTHLY每月")
    private String frequency;

    @Schema(description = "执行日: 周几或月内日期")
    private String executeDay;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "下次执行日期")
    private LocalDate nextExecuteDate;

    @Schema(description = "上次执行日期")
    private LocalDate lastExecuteDate;

    @Schema(description = "状态: 0终止 1启用 2暂停")
    private Integer status;

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
