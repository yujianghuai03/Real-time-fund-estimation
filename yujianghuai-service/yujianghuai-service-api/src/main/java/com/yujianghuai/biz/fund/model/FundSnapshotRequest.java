package com.yujianghuai.biz.fund.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "基金快照同步请求")
public class FundSnapshotRequest {

    @Valid
    @Schema(description = "自选基金列表")
    private List<FundSnapshotFund> funds = new ArrayList<>();

    @Valid
    @Schema(description = "自定义分组列表")
    private List<FundSnapshotGroup> groups = new ArrayList<>();

    @Valid
    @Schema(description = "交易记录列表")
    private List<FundSnapshotTransaction> transactions = new ArrayList<>();
}
