package com.yujianghuai.fund.controller;

import com.yujianghuai.common.web.PageResult;
import com.yujianghuai.common.web.R;
import com.yujianghuai.fund.model.dto.FundHoldingQueryRequest;
import com.yujianghuai.fund.model.dto.FundHoldingRequest;
import com.yujianghuai.fund.model.dto.FundHoldingSummaryRequest;
import com.yujianghuai.fund.model.validation.CreateGroup;
import com.yujianghuai.fund.model.validation.QueryGroup;
import com.yujianghuai.fund.model.validation.UpdateGroup;
import com.yujianghuai.fund.model.vo.FundHoldingSummaryVO;
import com.yujianghuai.fund.model.vo.FundHoldingVO;
import com.yujianghuai.fund.service.BizUserFundHoldingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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
@RequestMapping("/fund/holding")
@Tag(name = "用户基金持仓管理", description = "用户持仓基金、持仓成本、市值收益与组合汇总接口")
public class UserFundHoldingController {

    private final BizUserFundHoldingService holdingService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('fund:holding:page')")
    @Operation(summary = "分页查询当前持仓基金", description = "按当前登录用户查询基金持仓，支持组合、分组、基金代码和关键字筛选")
    public R<PageResult<FundHoldingVO>> page(
            @ParameterObject @Validated(QueryGroup.class) FundHoldingQueryRequest request) {
        return R.ok(holdingService.page(request));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('fund:holding:list')")
    @Operation(summary = "查询当前持仓基金列表", description = "按当前登录用户查询持仓基金列表")
    public R<List<FundHoldingVO>> list(
            @ParameterObject @Validated(QueryGroup.class) FundHoldingQueryRequest request) {
        return R.ok(holdingService.list(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('fund:holding:detail')")
    @Operation(summary = "查询持仓详情", description = "根据持仓ID查询当前用户的持仓详情")
    public R<FundHoldingVO> detail(@Parameter(description = "持仓ID", required = true) @PathVariable Long id) {
        return R.ok(holdingService.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('fund:holding:create')")
    @Operation(summary = "新增基金持仓", description = "记录当前用户的持有份额、持仓成本、持仓市值和分组关系")
    public R<FundHoldingVO> create(@Validated(CreateGroup.class) @RequestBody FundHoldingRequest request) {
        return R.ok(holdingService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('fund:holding:update')")
    @Operation(summary = "修改基金持仓", description = "修改当前用户的持仓份额、成本、市值、最新净值和分组关系")
    public R<FundHoldingVO> update(
            @Parameter(description = "持仓ID", required = true) @PathVariable Long id,
            @Validated(UpdateGroup.class) @RequestBody FundHoldingRequest request) {
        return R.ok(holdingService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('fund:holding:remove')")
    @Operation(summary = "删除基金持仓", description = "逻辑删除当前用户的基金持仓，并清理分组关系")
    public R<Boolean> delete(@Parameter(description = "持仓ID", required = true) @PathVariable Long id) {
        return R.ok(holdingService.delete(id));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('fund:holding:summary')")
    @Operation(summary = "查询持仓汇总", description = "按当前账户、投资组合或基金分组汇总持仓成本、市值、收益和收益率")
    public R<List<FundHoldingSummaryVO>> summary(
            @ParameterObject @Validated(QueryGroup.class) FundHoldingSummaryRequest request) {
        return R.ok(holdingService.summary(request));
    }
}
