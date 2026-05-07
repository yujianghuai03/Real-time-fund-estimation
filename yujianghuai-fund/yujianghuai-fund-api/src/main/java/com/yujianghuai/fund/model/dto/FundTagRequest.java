package com.yujianghuai.fund.model.dto;

import com.yujianghuai.fund.model.validation.CreateGroup;
import com.yujianghuai.fund.model.validation.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "基金标签新增或修改请求")
public class FundTagRequest {

    @NotBlank(groups = CreateGroup.class)
    @Size(max = 64, groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "标签名称")
    private String tagName;

    @NotBlank(groups = CreateGroup.class)
    @Size(max = 32, groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "标签类型，STYLE风格 INDUSTRY行业 THEME主题 CUSTOM自定义")
    private String tagType;

    @Pattern(regexp = "^$|^#[0-9a-fA-F]{6}$|^[a-zA-Z0-9_-]{1,32}$", groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "标签颜色")
    private String tagColor;

    @Min(value = 0, groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "排序")
    private Integer sortOrder;

    @Min(value = 0, groups = {CreateGroup.class, UpdateGroup.class})
    @Max(value = 1, groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "状态，0禁用 1启用")
    private Integer status;
}
