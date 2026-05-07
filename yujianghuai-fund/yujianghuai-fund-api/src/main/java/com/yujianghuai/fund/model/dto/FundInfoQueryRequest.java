package com.yujianghuai.fund.model.dto;

import com.yujianghuai.fund.model.validation.QueryGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "基金信息分页查询请求")
public class FundInfoQueryRequest {

    @Min(value = 1, groups = QueryGroup.class)
    @Schema(description = "页码")
    private Long pageNum = 1L;

    @Min(value = 1, groups = QueryGroup.class)
    @Max(value = 200, groups = QueryGroup.class)
    @Schema(description = "每页条数")
    private Long pageSize = 20L;

    @Schema(description = "基金代码")
    private String fundCode;

    @Schema(description = "关键字，匹配代码、名称、简称、基金公司")
    private String keyword;

    @Schema(description = "基金类型")
    private String fundType;

    @Schema(description = "基金公司")
    private String fundCompany;

    @Schema(description = "风险等级")
    private Integer riskLevel;

    @Schema(description = "申购状态，0暂停 1开放")
    private Integer purchaseStatus;

    @Schema(description = "赎回状态，0暂停 1开放")
    private Integer redeemStatus;

    @Schema(description = "基金状态，0失效 1正常 2清盘")
    private Integer status;

    @Schema(description = "标签ID列表")
    private List<Long> tagIds;
}
