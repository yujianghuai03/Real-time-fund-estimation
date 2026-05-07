package com.yujianghuai.fund.model.dto;

import com.yujianghuai.fund.model.validation.QueryGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "用户基金持仓汇总请求")
public class FundHoldingSummaryRequest {

    @Schema(description = "汇总维度，USER当前账户 PORTFOLIO组合 GROUP分组")
    private String dimension = "USER";

    @Schema(description = "投资组合ID")
    private Long portfolioId;

    @Schema(description = "分组ID")
    private Long groupId;

    @Min(value = 0, groups = QueryGroup.class)
    @Max(value = 1, groups = QueryGroup.class)
    @Schema(description = "持仓状态，0清仓 1持有")
    private Integer status = 1;
}
