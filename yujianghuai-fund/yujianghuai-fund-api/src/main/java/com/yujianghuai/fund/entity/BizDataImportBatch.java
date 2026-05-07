package com.yujianghuai.fund.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yujianghuai.common.entity.BaseEntity;
import com.yujianghuai.common.tenant.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TenantTable
@Schema(description = "业务数据导入批次表")
@TableName("biz_data_import_batch")
public class BizDataImportBatch extends BaseEntity {

    @Schema(description = "导入批次ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "租户ID，关联sys_tenant.id")
    private Long tenantId;

    @Schema(description = "用户ID，关联sys_user.id")
    private Long userId;

    @Schema(description = "导入类型: HOLDING持仓 TRANSACTION交易 NAV净值")
    private String importType;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "文件地址")
    private String fileUrl;

    @Schema(description = "总记录数")
    private Integer totalCount;

    @Schema(description = "成功记录数")
    private Integer successCount;

    @Schema(description = "失败记录数")
    private Integer failureCount;

    @Schema(description = "导入状态: PENDING待处理 PROCESSING处理中 SUCCESS成功 PARTIAL部分成功 FAILED失败")
    private String importStatus;

    @Schema(description = "失败原因")
    private String failureReason;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

}
