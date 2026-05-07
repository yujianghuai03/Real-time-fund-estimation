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
@Schema(description = "biz_fund_dividend")
@TableName("biz_fund_dividend")
public class BizFundDividend {

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
