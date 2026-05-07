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
@Schema(description = "基金标签表")
@TableName("biz_fund_tag")
public class BizFundTag extends BaseEntity {

    @Schema(description = "标签ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "租户ID，关联sys_tenant.id")
    private Long tenantId;

    @Schema(description = "标签名称")
    private String tagName;

    @Schema(description = "标签类型: STYLE风格 INDUSTRY行业 THEME主题 CUSTOM自定义")
    private String tagType;

    @Schema(description = "标签颜色")
    private String tagColor;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态: 0禁用 1启用")
    private Integer status;

}
