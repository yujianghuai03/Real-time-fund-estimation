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
@Schema(description = "biz_user_alert_rule")
@TableName("biz_user_alert_rule")
public class BizUserAlertRule {

    @Schema(description = "提醒规则ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "租户ID，关联sys_tenant.id")
    private Long tenantId;

    @Schema(description = "用户ID，关联sys_user.id")
    private Long userId;

    @Schema(description = "基金ID，关联biz_fund_info.id；为空表示用户级提醒")
    private Long fundId;

    @Schema(description = "提醒名称")
    private String ruleName;

    @Schema(description = "提醒类型: NAV净值 ESTIMATE估值 PROFIT收益 SIP定投 DIVIDEND分红")
    private String ruleType;

    @Schema(description = "比较符: GT大于 GTE大于等于 LT小于 LTE小于等于 EQ等于")
    private String compareOperator;

    @Schema(description = "阈值")
    private BigDecimal thresholdValue;

    @Schema(description = "通知渠道，多个用逗号分隔")
    private String notifyChannels;

    @Schema(description = "最近触发时间")
    private LocalDateTime lastTriggerTime;

    @Schema(description = "状态: 0禁用 1启用")
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
