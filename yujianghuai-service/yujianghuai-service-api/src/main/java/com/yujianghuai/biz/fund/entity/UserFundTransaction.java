package com.yujianghuai.biz.fund.entity;

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
@TenantTable
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户基金交易记录实体")
@TableName("biz_user_fund_transaction")
public class UserFundTransaction extends BaseEntity {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "基金代码")
    private String fundCode;

    @Schema(description = "基金名称")
    private String fundName;

    @Schema(description = "交易类型")
    private String tradeType;

    @Schema(description = "交易金额")
    private BigDecimal amount;

    @Schema(description = "操作前持仓金额")
    private BigDecimal beforeAmount;

    @Schema(description = "操作后持仓金额")
    private BigDecimal afterAmount;

    @Schema(description = "目标基金代码")
    private String targetFundCode;

    @Schema(description = "目标基金名称")
    private String targetFundName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "交易时间")
    private LocalDateTime tradeTime;
}
