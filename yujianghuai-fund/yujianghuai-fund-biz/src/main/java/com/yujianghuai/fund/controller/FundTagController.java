package com.yujianghuai.fund.controller;

import com.yujianghuai.common.web.R;
import com.yujianghuai.fund.model.dto.FundTagQueryRequest;
import com.yujianghuai.fund.model.dto.FundTagRequest;
import com.yujianghuai.fund.model.validation.CreateGroup;
import com.yujianghuai.fund.model.validation.UpdateGroup;
import com.yujianghuai.fund.model.vo.FundTagVO;
import com.yujianghuai.fund.service.BizFundTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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
@RequestMapping("/fund/tag")
@Tag(name = "基金标签管理", description = "基金标签、分类维护接口")
public class FundTagController {

    private final BizFundTagService fundTagService;

    @GetMapping("/list")
    @Operation(summary = "查询基金标签", description = "按标签名称、类型和状态查询标签分类")
    public R<List<FundTagVO>> list(@ParameterObject FundTagQueryRequest request) {
        return R.ok(fundTagService.list(request));
    }

    @PostMapping
    @Operation(summary = "新增基金标签", description = "新增风格、行业、主题或自定义标签")
    public R<FundTagVO> create(@Validated(CreateGroup.class) @RequestBody FundTagRequest request) {
        return R.ok(fundTagService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改基金标签", description = "根据标签ID修改标签名称、类型、颜色、排序或状态")
    public R<FundTagVO> update(
            @Parameter(description = "标签ID", required = true) @PathVariable Long id,
            @Validated(UpdateGroup.class) @RequestBody FundTagRequest request) {
        return R.ok(fundTagService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除基金标签", description = "根据标签ID逻辑删除标签，并清理基金标签关系")
    public R<Boolean> delete(@Parameter(description = "标签ID", required = true) @PathVariable Long id) {
        return R.ok(fundTagService.delete(id));
    }
}
