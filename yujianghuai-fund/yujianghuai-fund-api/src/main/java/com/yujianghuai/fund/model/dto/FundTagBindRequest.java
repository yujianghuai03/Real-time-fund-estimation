package com.yujianghuai.fund.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "基金标签绑定请求")
public class FundTagBindRequest {

    @NotNull
    @Schema(description = "标签ID列表；空列表表示清空标签")
    private List<Long> tagIds = new ArrayList<>();
}
