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
@Schema(description = "biz_user_portfolio")
@TableName("biz_user_portfolio")
public class BizUserPortfolio {

    @Schema(description = "投资组合ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "租户ID，关联sys_tenant.id")
    private Long tenantId;

    @Schema(description = "用户ID，关联sys_user.id")
    private Long userId;

    @Schema(description = "组合名称")
    private String portfolioName;

    @Schema(description = "组合类型: DEFAULT默认 CUSTOM自定义")
    private String portfolioType;

    @Schema(description = "目标金额")
    private BigDecimal targetAmount;

    @Schema(description = "风险偏好: 1保守 2稳健 3平衡 4积极 5激进")
    private Integer riskPreference;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态: 0停用 1启用")
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
