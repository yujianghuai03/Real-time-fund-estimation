package com.yujianghuai.fund.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yujianghuai.common.entity.BaseEntity;
import com.yujianghuai.common.tenant.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TenantTable
@Schema(description = "业务数据同步日志表")
@TableName("biz_data_sync_log")
public class BizDataSyncLog extends BaseEntity {

    @Schema(description = "同步日志ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "租户ID，关联sys_tenant.id")
    private Long tenantId;

    @Schema(description = "同步类型: FUND_INFO基金信息 NAV净值 ESTIMATE估值 DIVIDEND分红")
    private String syncType;

    @Schema(description = "数据来源")
    private String source;

    @Schema(description = "业务日期")
    private LocalDate bizDate;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "总记录数")
    private Integer totalCount;

    @Schema(description = "成功记录数")
    private Integer successCount;

    @Schema(description = "失败记录数")
    private Integer failureCount;

    @Schema(description = "同步状态: PROCESSING处理中 SUCCESS成功 PARTIAL部分成功 FAILED失败")
    private String syncStatus;

    @Schema(description = "失败原因")
    private String failureReason;

}
