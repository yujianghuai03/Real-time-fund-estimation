package com.yujianghuai.biz.fund.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yujianghuai.common.entity.BaseEntity;
import com.yujianghuai.common.tenant.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TenantTable
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户自选基金实体")
@TableName("biz_user_fund")
public class UserFund extends BaseEntity {

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
    @Schema(description = "持有金额")
    private BigDecimal holdingAmount;
    @Schema(description = "持仓成本")
    private BigDecimal holdingCost;
    @Schema(description = "排序值")
    private Integer sortOrder;
}
