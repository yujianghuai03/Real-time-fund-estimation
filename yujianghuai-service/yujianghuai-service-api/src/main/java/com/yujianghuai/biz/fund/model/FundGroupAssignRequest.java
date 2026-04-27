package com.yujianghuai.biz.fund.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "基金分组分配请求")
public class FundGroupAssignRequest {

    @NotNull
    @Schema(description = "自定义分组ID列表")
    private List<Long> groupIds = new ArrayList<>();
}
