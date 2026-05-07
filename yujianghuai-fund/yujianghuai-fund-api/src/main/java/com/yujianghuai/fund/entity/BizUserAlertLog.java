package com.yujianghuai.fund.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yujianghuai.common.entity.BaseEntity;
import com.yujianghuai.common.tenant.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TenantTable
@Schema(description = "用户提醒通知日志表")
@TableName("biz_user_alert_log")
public class BizUserAlertLog extends BaseEntity {

    @Schema(description = "提醒日志ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "租户ID，关联sys_tenant.id")
    private Long tenantId;

    @Schema(description = "用户ID，关联sys_user.id")
    private Long userId;

    @Schema(description = "提醒规则ID，关联biz_user_alert_rule.id")
    private Long ruleId;

    @Schema(description = "基金ID，关联biz_fund_info.id")
    private Long fundId;

    @Schema(description = "提醒标题")
    private String alertTitle;

    @Schema(description = "提醒内容")
    private String alertContent;

    @Schema(description = "触发值")
    private BigDecimal triggerValue;

    @Schema(description = "通知渠道")
    private String notifyChannel;

    @Schema(description = "通知状态: PENDING待发送 SENT已发送 FAILED失败 READ已读")
    private String notifyStatus;

    @Schema(description = "触发时间")
    private LocalDateTime triggerTime;

    @Schema(description = "读取时间")
    private LocalDateTime readTime;

    @Schema(description = "失败原因")
    private String failureReason;

}
