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
@Schema(description = "biz_user_fund_sip_execution")
@TableName("biz_user_fund_sip_execution")
public class BizUserFundSipExecution {

    @Schema(description = "定投执行ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "租户ID，关联sys_tenant.id")
    private Long tenantId;

    @Schema(description = "用户ID，关联sys_user.id")
    private Long userId;

    @Schema(description = "定投计划ID，关联biz_user_fund_sip_plan.id")
    private Long sipPlanId;

    @Schema(description = "交易记录ID，关联biz_user_fund_transaction.id")
    private Long transactionId;

    @Schema(description = "执行日期")
    private LocalDate executeDate;

    @Schema(description = "执行金额")
    private BigDecimal executeAmount;

    @Schema(description = "执行状态: PENDING待执行 SUCCESS成功 FAILED失败 SKIPPED跳过")
    private String executeStatus;

    @Schema(description = "失败原因")
    private String failureReason;

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
