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
@Schema(description = "biz_fund_nav")
@TableName("biz_fund_nav")
public class BizFundNav {

    @Schema(description = "净值ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "租户ID，关联sys_tenant.id")
    private Long tenantId;

    @Schema(description = "基金ID，关联biz_fund_info.id")
    private Long fundId;

    @Schema(description = "基金代码，冗余用于查询")
    private String fundCode;

    @Schema(description = "净值日期")
    private LocalDate navDate;

    @Schema(description = "单位净值")
    private BigDecimal unitNav;

    @Schema(description = "累计净值")
    private BigDecimal accumulatedNav;

    @Schema(description = "日涨跌幅百分比")
    private BigDecimal dailyGrowthRate;

    @Schema(description = "数据来源")
    private String source;

    @Schema(description = "来源更新时间")
    private LocalDateTime sourceUpdateTime;

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
