package com.yujianghuai.biz.fund.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "基金分组视图对象")
public class FundGroupVO {

    @Schema(description = "分组ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "分组名称")
    private String name;

    @Schema(description = "基金数量")
    private Long count;

    @Schema(description = "分组类型，SYSTEM系统分组，CUSTOM自定义分组")
    private String groupType;

    @Schema(description = "是否允许用户编辑")
    private Boolean editable;
}
