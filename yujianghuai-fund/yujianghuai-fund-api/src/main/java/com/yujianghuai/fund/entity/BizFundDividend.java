package com.yujianghuai.fund.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yujianghuai.common.entity.BaseEntity;
import com.yujianghuai.common.tenant.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TenantTable
@Schema(description = "基金分红记录表")
@TableName("biz_fund_dividend")
public class BizFundDividend extends BaseEntity {

    @Schema(description = "分红ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "租户ID，关联sys_tenant.id")
    private Long tenantId;

    @Schema(description = "基金ID，关联biz_fund_info.id")
    private Long fundId;

    @Schema(description = "基金代码，冗余用于查询")
    private String fundCode;

    @Schema(description = "权益登记日")
    private LocalDate recordDate;

    @Schema(description = "除息日")
    private LocalDate exDividendDate;

    @Schema(description = "派息日")
    private LocalDate paymentDate;

    @Schema(description = "每份分红金额")
    private BigDecimal dividendPerShare;

    @Schema(description = "分红方式: CASH现金分红 REINVEST红利再投资")
    private String dividendType;

    @Schema(description = "备注")
    private String remark;

}
