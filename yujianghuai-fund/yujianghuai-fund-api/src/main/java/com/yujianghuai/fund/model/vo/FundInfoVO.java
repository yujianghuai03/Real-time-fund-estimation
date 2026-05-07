package com.yujianghuai.fund.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "基金信息响应")
public class FundInfoVO {

    @Schema(description = "基金ID")
    private Long id;

    @Schema(description = "基金代码")
    private String fundCode;

    @Schema(description = "基金名称")
    private String fundName;

    @Schema(description = "基金简称")
    private String fundShortName;

    @Schema(description = "基金类型")
    private String fundType;

    @Schema(description = "基金公司")
    private String fundCompany;

    @Schema(description = "基金经理")
    private String managerName;

    @Schema(description = "风险等级")
    private Integer riskLevel;

    @Schema(description = "成立日期")
    private LocalDate establishDate;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "跟踪指数")
    private String trackingIndex;

    @Schema(description = "申购状态，0暂停 1开放")
    private Integer purchaseStatus;

    @Schema(description = "赎回状态，0暂停 1开放")
    private Integer redeemStatus;

    @Schema(description = "基金状态，0失效 1正常 2清盘")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "标签列表")
    private List<FundTagVO> tags = new ArrayList<>();

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
