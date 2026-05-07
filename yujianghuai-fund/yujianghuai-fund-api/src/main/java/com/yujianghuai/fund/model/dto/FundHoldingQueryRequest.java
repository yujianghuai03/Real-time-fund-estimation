package com.yujianghuai.fund.model.dto;

import com.yujianghuai.fund.model.validation.QueryGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "用户基金持仓分页查询请求")
public class FundHoldingQueryRequest {

    @Min(value = 1, groups = QueryGroup.class)
    @Schema(description = "页码")
    private Long pageNum = 1L;

    @Min(value = 1, groups = QueryGroup.class)
    @Max(value = 200, groups = QueryGroup.class)
    @Schema(description = "每页条数")
    private Long pageSize = 20L;

    @Schema(description = "投资组合ID")
    private Long portfolioId;

    @Schema(description = "分组ID")
    private Long groupId;

    @Schema(description = "基金代码")
    private String fundCode;

    @Schema(description = "关键字，匹配基金代码或名称")
    private String keyword;

    @Min(value = 0, groups = QueryGroup.class)
    @Max(value = 1, groups = QueryGroup.class)
    @Schema(description = "持仓状态，0清仓 1持有")
    private Integer status = 1;
}
