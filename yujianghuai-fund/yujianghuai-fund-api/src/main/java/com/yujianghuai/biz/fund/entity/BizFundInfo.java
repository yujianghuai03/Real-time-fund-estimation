package com.yujianghuai.biz.fund.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yujianghuai.common.tenant.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TenantTable
@Schema(description = "biz_fund_info")
@TableName("biz_fund_info")
public class BizFundInfo {

    @Schema(description = "基金主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "租户ID，关联sys_tenant.id")
    private Long tenantId;

    @Schema(description = "基金代码")
    private String fundCode;

    @Schema(description = "基金名称")
    private String fundName;

    @Schema(description = "基金简称")
    private String fundShortName;

    @Schema(description = "基金类型: STOCK股票型 BOND债券型 MIXED混合型 INDEX指数型 MONEY货币型 QDII等")
    private String fundType;

    @Schema(description = "基金公司")
    private String fundCompany;

    @Schema(description = "基金经理")
    private String managerName;

    @Schema(description = "风险等级: 1低 2中低 3中 4中高 5高")
    private Integer riskLevel;

    @Schema(description = "成立日期")
    private LocalDate establishDate;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "跟踪指数")
    private String trackingIndex;

    @Schema(description = "申购状态: 0暂停 1开放")
    private Integer purchaseStatus;

    @Schema(description = "赎回状态: 0暂停 1开放")
    private Integer redeemStatus;

    @Schema(description = "基金状态: 0失效 1正常 2清盘")
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
