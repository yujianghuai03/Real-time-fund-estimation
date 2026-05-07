package com.yujianghuai.fund.controller;

import com.yujianghuai.common.web.PageResult;
import com.yujianghuai.common.web.R;
import com.yujianghuai.fund.model.dto.FundInfoQueryRequest;
import com.yujianghuai.fund.model.dto.FundInfoRequest;
import com.yujianghuai.fund.model.dto.FundStatusRequest;
import com.yujianghuai.fund.model.dto.FundTagBindRequest;
import com.yujianghuai.fund.model.validation.CreateGroup;
import com.yujianghuai.fund.model.validation.QueryGroup;
import com.yujianghuai.fund.model.validation.UpdateGroup;
import com.yujianghuai.fund.model.vo.FundInfoVO;
import com.yujianghuai.fund.service.BizFundInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
@RequestMapping("/fund/info")
@Tag(name = "基金信息管理", description = "基金基础信息、状态和标签管理接口")
public class FundInfoController {

    private final BizFundInfoService fundInfoService;

    @GetMapping("/page")
    @Operation(summary = "分页查询基金信息", description = "按基金代码、名称、类型、公司、风险、状态和标签筛选基金")
    public R<PageResult<FundInfoVO>> page(@ParameterObject @Validated(QueryGroup.class) FundInfoQueryRequest request) {
        return R.ok(fundInfoService.page(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询基金详情", description = "根据基金ID查询基金基础信息和标签")
    public R<FundInfoVO> detail(@Parameter(description = "基金ID", required = true) @PathVariable Long id) {
        return R.ok(fundInfoService.detail(id));
    }

    @PostMapping
    @Operation(summary = "新增基金信息", description = "维护基金代码、名称、类型、基金公司、风险等级和标签")
    public R<FundInfoVO> create(@Validated(CreateGroup.class) @RequestBody FundInfoRequest request) {
        return R.ok(fundInfoService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改基金信息", description = "根据基金ID修改基础信息，可同步修改标签")
    public R<FundInfoVO> update(
            @Parameter(description = "基金ID", required = true) @PathVariable Long id,
            @Validated(UpdateGroup.class) @RequestBody FundInfoRequest request) {
        return R.ok(fundInfoService.update(id, request));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "修改基金状态", description = "管理开放申购、暂停申购、暂停赎回、清盘等状态")
    public R<FundInfoVO> updateStatus(
            @Parameter(description = "基金ID", required = true) @PathVariable Long id,
            @Valid @RequestBody FundStatusRequest request) {
        return R.ok(fundInfoService.updateStatus(id, request));
    }

    @PutMapping("/{id}/tags")
    @Operation(summary = "绑定基金标签", description = "设置基金标签和分类，空列表表示清空标签")
    public R<FundInfoVO> bindTags(
            @Parameter(description = "基金ID", required = true) @PathVariable Long id,
            @Valid @RequestBody FundTagBindRequest request) {
        return R.ok(fundInfoService.bindTags(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除基金信息", description = "根据基金ID执行逻辑删除，并清理标签关系")
    public R<Boolean> delete(@Parameter(description = "基金ID", required = true) @PathVariable Long id) {
        return R.ok(fundInfoService.delete(id));
    }
}
