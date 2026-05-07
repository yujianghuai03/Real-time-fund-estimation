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
@Schema(description = "用户基金分组关系表")
@TableName("biz_user_fund_group_relation")
public class BizUserFundGroupRelation extends BaseEntity {

    @Schema(description = "关系ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "租户ID，关联sys_tenant.id")
    private Long tenantId;

    @Schema(description = "用户ID，关联sys_user.id")
    private Long userId;

    @Schema(description = "分组ID，关联biz_user_fund_group.id")
    private Long groupId;

    @Schema(description = "持仓ID，关联biz_user_fund_holding.id")
    private Long holdingId;

    @Schema(description = "基金ID，关联biz_fund_info.id")
    private Long fundId;

    @Schema(description = "组内排序")
    private Integer sortOrder;

}
