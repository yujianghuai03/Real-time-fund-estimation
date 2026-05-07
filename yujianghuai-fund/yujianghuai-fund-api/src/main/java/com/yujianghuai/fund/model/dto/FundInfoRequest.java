package com.yujianghuai.fund.model.dto;

import com.yujianghuai.fund.model.validation.CreateGroup;
import com.yujianghuai.fund.model.validation.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "基金信息新增或修改请求")
public class FundInfoRequest {

    @NotBlank(groups = CreateGroup.class)
    @Size(max = 32, groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "基金代码")
    private String fundCode;

    @NotBlank(groups = CreateGroup.class)
    @Size(max = 128, groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "基金名称")
    private String fundName;

    @Size(max = 64, groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "基金简称")
    private String fundShortName;

    @NotBlank(groups = CreateGroup.class)
    @Size(max = 32, groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "基金类型")
    private String fundType;

    @Size(max = 128, groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "基金公司")
    private String fundCompany;

    @Size(max = 128, groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "基金经理")
    private String managerName;

    @Min(value = 1, groups = {CreateGroup.class, UpdateGroup.class})
    @Max(value = 5, groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "风险等级，1低 2中低 3中 4中高 5高")
    private Integer riskLevel;

    @Schema(description = "成立日期")
    private LocalDate establishDate;

    @Size(max = 16, groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "币种")
    private String currency;

    @Size(max = 128, groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "跟踪指数")
    private String trackingIndex;

    @Min(value = 0, groups = {CreateGroup.class, UpdateGroup.class})
    @Max(value = 1, groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "申购状态，0暂停 1开放")
    private Integer purchaseStatus;

    @Min(value = 0, groups = {CreateGroup.class, UpdateGroup.class})
    @Max(value = 1, groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "赎回状态，0暂停 1开放")
    private Integer redeemStatus;

    @Min(value = 0, groups = {CreateGroup.class, UpdateGroup.class})
    @Max(value = 2, groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "基金状态，0失效 1正常 2清盘")
    private Integer status;

    @Size(max = 512, groups = {CreateGroup.class, UpdateGroup.class})
    @Schema(description = "备注")
    private String remark;

    @Schema(description = "标签ID列表；新增为空表示不绑定，修改为空表示不变")
    private List<Long> tagIds;
}
