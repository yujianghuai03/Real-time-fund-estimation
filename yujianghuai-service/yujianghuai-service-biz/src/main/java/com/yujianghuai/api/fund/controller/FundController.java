package com.yujianghuai.api.fund.controller;

import com.yujianghuai.biz.fund.model.FundEstimateVO;
import com.yujianghuai.biz.fund.model.FundGroupAssignRequest;
import com.yujianghuai.biz.fund.model.FundGroupRequest;
import com.yujianghuai.biz.fund.model.FundGroupVO;
import com.yujianghuai.biz.fund.model.FundSearchVO;
import com.yujianghuai.biz.fund.model.FundSnapshotRequest;
import com.yujianghuai.biz.fund.model.FundTransactionVO;
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
    @GetMapping("/estimate/{code}")
    @Operation(summary = "实时估值", description = "根据基金代码查询公开实时估值")
    public R<FundEstimateVO> estimate(@Parameter(description = "基金代码", required = true) @PathVariable String code) {
        return R.ok(userFundService.estimate(code));
    }

    /**
     * 查询自选基金列表。
     */
    @GetMapping("/watchlist")
    @Operation(summary = "查询自选基金列表", description = "查询当前登录用户的自选基金及实时估值")
    public R<List<FundEstimateVO>> watchlist(Principal principal) {
        return R.ok(userFundService.listWithRealtimeEstimate(principal));
    }

    @GetMapping("/groups")
    @Operation(summary = "查询基金分组", description = "查询当前登录用户的自定义基金分组")
    public R<List<FundGroupVO>> groups(Principal principal) {
        return R.ok(userFundService.listGroups(principal));
    }

    @GetMapping("/transactions")
    @Operation(summary = "查询基金交易记录", description = "查询当前登录用户的基金交易记录")
    public R<List<FundTransactionVO>> transactions(Principal principal) {
        return R.ok(userFundService.listTransactions(principal));
    }

    @PostMapping("/groups")
    @Operation(summary = "新增基金分组", description = "新增当前登录用户的自定义基金分组")
    public R<FundGroupVO> createGroup(Principal principal, @Valid @RequestBody FundGroupRequest request) {
        return R.ok(userFundService.createGroup(principal, request.getName()));
    }

    @PutMapping("/groups/{groupId}")
    @Operation(summary = "修改基金分组", description = "修改当前登录用户的自定义基金分组名称")
    public R<FundGroupVO> updateGroup(Principal principal,
                                      @Parameter(description = "分组ID", required = true) @PathVariable Long groupId,
                                      @Valid @RequestBody FundGroupRequest request) {
        return R.ok(userFundService.updateGroup(principal, groupId, request.getName()));
    }

    @DeleteMapping("/groups/{groupId}")
    @Operation(summary = "删除基金分组", description = "删除当前登录用户的自定义基金分组")
    public R<Boolean> deleteGroup(Principal principal,
                                  @Parameter(description = "分组ID", required = true) @PathVariable Long groupId) {
        return R.ok(userFundService.deleteGroup(principal, groupId));
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
        return R.ok(userFundService.updateHolding(principal, code, request.getHoldingAmount(), request.getHoldingCost()));
    }

    @PutMapping("/watchlist/{code}/groups")
    @Operation(summary = "修改基金所属分组", description = "为当前登录用户的自选基金设置多个自定义分组")
    public R<Boolean> updateFundGroups(Principal principal,
                                       @Parameter(description = "基金代码", required = true) @PathVariable String code,
                                       @Valid @RequestBody FundGroupAssignRequest request) {
        return R.ok(userFundService.updateFundGroups(principal, code, request.getGroupIds()));
    }

    @PostMapping("/snapshot/replace")
    @Operation(summary = "替换云端基金快照", description = "使用客户端快照完全替换当前用户的云端自选、分组与分组关系")
    public R<Boolean> replaceSnapshot(Principal principal, @Valid @RequestBody FundSnapshotRequest request) {
        return R.ok(userFundService.replaceFromSnapshot(principal, request));
    }

    @PostMapping("/snapshot/merge")
    @Operation(summary = "追加云端基金快照", description = "将客户端快照追加合并到当前用户的云端自选、分组与分组关系")
    public R<Boolean> mergeSnapshot(Principal principal, @Valid @RequestBody FundSnapshotRequest request) {
        return R.ok(userFundService.mergeFromSnapshot(principal, request));
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
