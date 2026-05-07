package com.yujianghuai.fund.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "基金标签响应")
public class FundTagVO {

    @Schema(description = "标签ID")
    private Long id;

    @Schema(description = "标签名称")
    private String tagName;

    @Schema(description = "标签类型")
    private String tagType;

    @Schema(description = "标签颜色")
    private String tagColor;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态，0禁用 1启用")
    private Integer status;
}
