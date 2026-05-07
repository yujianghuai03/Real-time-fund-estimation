package com.yujianghuai.fund.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "基金状态修改请求")
public class FundStatusRequest {

    @Min(0)
    @Max(1)
    @Schema(description = "申购状态，0暂停 1开放；为空不修改")
    private Integer purchaseStatus;

    @Min(0)
    @Max(1)
    @Schema(description = "赎回状态，0暂停 1开放；为空不修改")
    private Integer redeemStatus;

    @Min(0)
    @Max(2)
    @Schema(description = "基金状态，0失效 1正常 2清盘；为空不修改")
    private Integer status;
}
