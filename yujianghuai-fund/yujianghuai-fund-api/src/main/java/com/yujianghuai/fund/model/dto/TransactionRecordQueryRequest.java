package com.yujianghuai.fund.model.dto;

import com.yujianghuai.fund.model.enums.TransactionStatusEnum;
import com.yujianghuai.fund.model.enums.TransactionTypeEnum;
import com.yujianghuai.fund.model.validation.QueryGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import lombok.Data;

@Data
@Schema(description = "交易记录分页查询请求")
public class TransactionRecordQueryRequest {

    @Min(value = 1, groups = QueryGroup.class)
    @Schema(description = "页码")
    private Long pageNum = 1L;

    @Min(value = 1, groups = QueryGroup.class)
    @Max(value = 200, groups = QueryGroup.class)
    @Schema(description = "每页条数")
    private Long pageSize = 20L;

    @Schema(description = "基金代码")
    private String fundCode;

    @Schema(description = "基金名称")
    private String fundName;

    @Schema(description = "交易类型")
    private TransactionTypeEnum tradeType;

    @Schema(description = "确认状态")
    private TransactionStatusEnum tradeStatus;

    @Schema(description = "交易开始日期")
    private LocalDate tradeStartDate;

    @Schema(description = "交易结束日期")
    private LocalDate tradeEndDate;

    @Schema(description = "确认开始日期")
    private LocalDate confirmStartDate;

    @Schema(description = "确认结束日期")
    private LocalDate confirmEndDate;
}
