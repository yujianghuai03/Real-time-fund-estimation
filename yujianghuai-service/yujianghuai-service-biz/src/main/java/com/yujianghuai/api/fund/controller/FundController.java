package com.yujianghuai.api.fund.controller;

import com.yujianghuai.biz.fund.model.FundEstimateVO;
import com.yujianghuai.biz.fund.model.FundSearchVO;
import com.yujianghuai.biz.fund.model.FundWatchRequest;
import com.yujianghuai.biz.fund.model.HoldingAmountRequest;
import com.yujianghuai.biz.fund.service.UserFundService;
import com.yujianghuai.common.web.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/funds")
@Tag(name = "基金管理", description = "基金搜索与自选基金管理接口")
public class FundController {

    private final UserFundService userFundService;

    public FundController(UserFundService userFundService) {
        this.userFundService = userFundService;
    }

    /**
     * 搜索基金。
     */
    @GetMapping("/search")
    @Operation(summary = "搜索基金", description = "根据关键字搜索基金列表")
    public R<List<FundSearchVO>> search(@Parameter(description = "搜索关键字", required = true) @RequestParam String keyword) {
        return R.ok(userFundService.search(keyword));
    }

    /**
     * 查询自选基金列表。
     */
    @GetMapping("/watchlist")
    @Operation(summary = "查询自选基金列表", description = "查询当前登录用户的自选基金及实时估值")
    public R<List<FundEstimateVO>> watchlist(Principal principal) {
        return R.ok(userFundService.listWithRealtimeEstimate(principal));
    }

    /**
     * 新增自选基金。
     */
    @PostMapping("/watchlist")
    @Operation(summary = "新增自选基金", description = "为当前登录用户新增一条自选基金")
    public R<FundEstimateVO> add(
            Principal principal,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "新增自选基金请求",
                    required = true,
                    content = @Content(schema = @Schema(implementation = FundWatchRequest.class)))
            @Valid @RequestBody FundWatchRequest request) {
        return R.ok(userFundService.add(principal, request));
    }

    /**
     * 更新持有金额。
     */
    @PutMapping("/watchlist/{code}/holding")
    @Operation(summary = "更新持有金额", description = "根据基金代码更新当前用户的持有金额")
    public R<Boolean> updateHolding(Principal principal,
                                    @Parameter(description = "基金代码", required = true) @PathVariable String code,
                                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                            description = "持有金额更新请求",
                                            required = true,
                                            content = @Content(schema = @Schema(implementation = HoldingAmountRequest.class)))
                                    @Valid @RequestBody HoldingAmountRequest request) {
        return R.ok(userFundService.updateHolding(principal, code, request.getHoldingAmount()));
    }

    /**
     * 删除自选基金。
     */
    @DeleteMapping("/watchlist/{code}")
    @Operation(summary = "删除自选基金", description = "根据基金代码删除当前用户的自选基金")
    public R<Boolean> delete(
            Principal principal,
            @Parameter(description = "基金代码", required = true) @PathVariable String code) {
        return R.ok(userFundService.delete(principal, code));
    }
}
