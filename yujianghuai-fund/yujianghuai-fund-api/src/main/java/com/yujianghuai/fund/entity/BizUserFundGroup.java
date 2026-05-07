package com.yujianghuai.fund.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yujianghuai.common.entity.BaseEntity;
import com.yujianghuai.common.tenant.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TenantTable
@Schema(description = "用户基金分组表")
@TableName("biz_user_fund_group")
public class BizUserFundGroup extends BaseEntity {

    @Schema(description = "分组ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "租户ID，关联sys_tenant.id")
    private Long tenantId;

    @Schema(description = "用户ID，关联sys_user.id")
    private Long userId;

    @Schema(description = "投资组合ID，关联biz_user_portfolio.id")
    private Long portfolioId;

    @Schema(description = "分组名称")
    private String groupName;

    @Schema(description = "分组类型: SYSTEM系统分组 CUSTOM自定义分组")
    private String groupType;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态: 0禁用 1启用")
    private Integer status;

}
