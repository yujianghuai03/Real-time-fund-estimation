package com.yujianghuai.biz.fund.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "基金快照分组项")
public class FundSnapshotGroup {

    @NotNull
    @Schema(description = "客户端分组ID")
    private Long id;

    @NotBlank
    @Schema(description = "分组名称")
    private String name;
}
