package com.yujianghuai.fund.controller;

import com.yujianghuai.common.web.PageResult;
import com.yujianghuai.common.web.R;
import com.yujianghuai.fund.model.dto.TransactionConfirmRequest;
import com.yujianghuai.fund.model.dto.TransactionRecordQueryRequest;
import com.yujianghuai.fund.model.dto.TransactionRecordRequest;
import com.yujianghuai.fund.model.validation.CreateGroup;
import com.yujianghuai.fund.model.validation.QueryGroup;
import com.yujianghuai.fund.model.validation.UpdateGroup;
import com.yujianghuai.fund.model.vo.TransactionRecordVO;
import com.yujianghuai.fund.service.BizUserFundTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transaction-record")
@Tag(name = "交易记录管理", description = "用户基金交易记录查询、维护、确认、撤销和导出接口")
public class TransactionRecordController {

    private final BizUserFundTransactionService transactionService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('fund:transaction:page')")
    @Operation(summary = "分页查询交易记录", description = "按基金代码、基金名称、交易类型、确认状态、交易日期和确认日期筛选交易记录")
    public R<PageResult<TransactionRecordVO>> page(
            @ParameterObject @Validated(QueryGroup.class) TransactionRecordQueryRequest request) {
        return R.ok(transactionService.page(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('fund:transaction:detail')")
    @Operation(summary = "查询交易记录详情", description = "根据交易记录ID查询当前用户交易详情")
    public R<TransactionRecordVO> detail(@Parameter(description = "交易记录ID", required = true) @PathVariable Long id) {
        return R.ok(transactionService.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('fund:transaction:create')")
    @Operation(summary = "新增交易记录", description = "新增当前用户基金交易记录")
    public R<TransactionRecordVO> create(@Validated(CreateGroup.class) @RequestBody TransactionRecordRequest request) {
        return R.ok(transactionService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('fund:transaction:update')")
    @Operation(summary = "编辑交易记录", description = "仅待确认交易允许编辑")
    public R<TransactionRecordVO> update(
            @Parameter(description = "交易记录ID", required = true) @PathVariable Long id,
            @Validated(UpdateGroup.class) @RequestBody TransactionRecordRequest request) {
        return R.ok(transactionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('fund:transaction:remove')")
    @Operation(summary = "删除交易记录", description = "逻辑删除当前用户交易记录，已确认交易不允许删除")
    public R<Boolean> delete(@Parameter(description = "交易记录ID", required = true) @PathVariable Long id) {
        return R.ok(transactionService.delete(id));
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('fund:transaction:confirm')")
    @Operation(summary = "确认交易", description = "仅待确认交易允许确认")
    public R<TransactionRecordVO> confirm(
            @Parameter(description = "交易记录ID", required = true) @PathVariable Long id,
            @Valid @RequestBody(required = false) TransactionConfirmRequest request) {
        return R.ok(transactionService.confirm(id, request));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('fund:transaction:cancel')")
    @Operation(summary = "撤销交易", description = "仅待确认交易允许撤销")
    public R<TransactionRecordVO> cancel(@Parameter(description = "交易记录ID", required = true) @PathVariable Long id) {
        return R.ok(transactionService.cancel(id));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('fund:transaction:export')")
    @Operation(summary = "导出交易记录", description = "按筛选条件导出当前用户交易记录Excel")
    public void export(@ParameterObject @Validated(QueryGroup.class) TransactionRecordQueryRequest request,
                       HttpServletResponse response) {
        transactionService.export(request, response);
    }
}
