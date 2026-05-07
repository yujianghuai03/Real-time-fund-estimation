package com.yujianghuai.fund.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yujianghuai.common.exception.BusinessException;
import com.yujianghuai.common.exception.ErrorCode;
import com.yujianghuai.common.web.PageResult;
import com.yujianghuai.fund.entity.BizFundInfo;
import com.yujianghuai.fund.entity.BizUserFundGroup;
import com.yujianghuai.fund.entity.BizUserFundGroupRelation;
import com.yujianghuai.fund.entity.BizUserFundHolding;
import com.yujianghuai.fund.entity.BizUserPortfolio;
import com.yujianghuai.fund.mapper.BizFundInfoMapper;
import com.yujianghuai.fund.mapper.BizUserFundGroupMapper;
import com.yujianghuai.fund.mapper.BizUserFundGroupRelationMapper;
import com.yujianghuai.fund.mapper.BizUserFundHoldingMapper;
import com.yujianghuai.fund.mapper.BizUserPortfolioMapper;
import com.yujianghuai.fund.model.dto.FundHoldingQueryRequest;
import com.yujianghuai.fund.model.dto.FundHoldingRequest;
import com.yujianghuai.fund.model.dto.FundHoldingSummaryRequest;
import com.yujianghuai.fund.model.vo.FundHoldingSummaryVO;
import com.yujianghuai.fund.model.vo.FundHoldingVO;
import com.yujianghuai.fund.service.BizUserFundHoldingService;
import com.yujianghuai.fund.support.CurrentFundUserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户基金持仓服务实现。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizUserFundHoldingServiceImpl extends ServiceImpl<BizUserFundHoldingMapper, BizUserFundHolding>
        implements BizUserFundHoldingService {

    private static final Integer STATUS_HOLDING = 1;
    private static final Integer DEFAULT_SORT_ORDER = 0;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final BizUserFundHoldingMapper holdingMapper;
    private final BizFundInfoMapper fundInfoMapper;
    private final BizUserPortfolioMapper portfolioMapper;
    private final BizUserFundGroupMapper groupMapper;
    private final BizUserFundGroupRelationMapper groupRelationMapper;
    private final CurrentFundUserService currentUserService;

    @Override
    public PageResult<FundHoldingVO> page(FundHoldingQueryRequest request) {
        Long tenantId = currentUserService.currentTenantId();
        Long userId = currentUserService.currentUserId(tenantId);
        List<Long> holdingIds = queryHoldingIdsByGroup(tenantId, userId, request.getGroupId());
        if (holdingIds != null && holdingIds.isEmpty()) {
            return PageResult.of(Collections.emptyList(), 0L, request.getPageNum(), request.getPageSize());
        }

        Page<BizUserFundHolding> page = holdingMapper.selectPage(
                new Page<>(request.getPageNum(), request.getPageSize()),
                buildQueryWrapper(tenantId, userId, request, holdingIds));
        return PageResult.of(page, toVOList(tenantId, userId, page.getRecords()));
    }

    @Override
    public List<FundHoldingVO> list(FundHoldingQueryRequest request) {
        Long tenantId = currentUserService.currentTenantId();
        Long userId = currentUserService.currentUserId(tenantId);
        List<Long> holdingIds = queryHoldingIdsByGroup(tenantId, userId, request.getGroupId());
        if (holdingIds != null && holdingIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<BizUserFundHolding> holdings = holdingMapper.selectList(buildQueryWrapper(tenantId, userId, request, holdingIds));
        return toVOList(tenantId, userId, holdings);
    }

    @Override
    public FundHoldingVO detail(Long id) {
        Long tenantId = currentUserService.currentTenantId();
        Long userId = currentUserService.currentUserId(tenantId);
        return toVOList(tenantId, userId, List.of(getOwnedHolding(tenantId, userId, id))).get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FundHoldingVO create(FundHoldingRequest request) {
        Long tenantId = currentUserService.currentTenantId();
        Long userId = currentUserService.currentUserId(tenantId);
        BizFundInfo fundInfo = getTenantFund(tenantId, request.getFundId());
        validatePortfolio(tenantId, userId, request.getPortfolioId());
        List<Long> groupIds = distinctIds(request.getGroupIds());
        validateGroups(tenantId, userId, request.getPortfolioId(), groupIds);
        assertHoldingUnique(tenantId, userId, null, request.getPortfolioId(), fundInfo.getId());

        BizUserFundHolding holding = new BizUserFundHolding();
        holding.setTenantId(tenantId);
        holding.setUserId(userId);
        holding.setPortfolioId(request.getPortfolioId());
        holding.setFundId(fundInfo.getId());
        holding.setFundCode(fundInfo.getFundCode());
        holding.setFundName(fundInfo.getFundName());
        copy(request, holding, true);
        recalculate(request, holding, true);
        holdingMapper.insert(holding);
        syncGroups(tenantId, userId, holding, groupIds);

        log.info("新增用户基金持仓 tenantId={}, userId={}, fundCode={}, amount={}",
                tenantId, userId, holding.getFundCode(), holding.getCostAmount());
        return detail(holding.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FundHoldingVO update(Long id, FundHoldingRequest request) {
        Long tenantId = currentUserService.currentTenantId();
        Long userId = currentUserService.currentUserId(tenantId);
        BizUserFundHolding holding = getOwnedHolding(tenantId, userId, id);

        Long portfolioId = request.getPortfolioId() != null ? request.getPortfolioId() : holding.getPortfolioId();
        Long fundId = request.getFundId() != null ? request.getFundId() : holding.getFundId();
        BizFundInfo fundInfo = getTenantFund(tenantId, fundId);
        validatePortfolio(tenantId, userId, portfolioId);
        if (request.getGroupIds() != null) {
            validateGroups(tenantId, userId, portfolioId, distinctIds(request.getGroupIds()));
        }
        assertHoldingUnique(tenantId, userId, id, portfolioId, fundId);

        holding.setPortfolioId(portfolioId);
        holding.setFundId(fundInfo.getId());
        holding.setFundCode(fundInfo.getFundCode());
        holding.setFundName(fundInfo.getFundName());
        copy(request, holding, false);
        recalculate(request, holding, false);
        holdingMapper.updateById(holding);
        if (request.getGroupIds() != null) {
            syncGroups(tenantId, userId, holding, distinctIds(request.getGroupIds()));
        }

        log.info("修改用户基金持仓 tenantId={}, userId={}, fundCode={}, amount={}",
                tenantId, userId, holding.getFundCode(), holding.getCostAmount());
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        Long tenantId = currentUserService.currentTenantId();
        Long userId = currentUserService.currentUserId(tenantId);
        BizUserFundHolding holding = getOwnedHolding(tenantId, userId, id);
        deleteHoldingGroups(tenantId, userId, id);
        holdingMapper.deleteById(id);
        log.info("删除用户基金持仓 tenantId={}, userId={}, fundCode={}", tenantId, userId, holding.getFundCode());
        return Boolean.TRUE;
    }

    @Override
    public List<FundHoldingSummaryVO> summary(FundHoldingSummaryRequest request) {
        Long tenantId = currentUserService.currentTenantId();
        Long userId = currentUserService.currentUserId(tenantId);
        validatePortfolio(tenantId, userId, request.getPortfolioId());
        if (request.getGroupId() != null) {
            validateGroup(tenantId, userId, request.getGroupId());
        }

        String dimension = normalizeDimension(request.getDimension());
        List<FundHoldingSummaryVO> summaries = switch (dimension) {
            case "PORTFOLIO" -> holdingMapper.selectPortfolioSummary(tenantId, userId, request);
            case "GROUP" -> holdingMapper.selectGroupSummary(tenantId, userId, request);
            default -> holdingMapper.selectUserSummary(tenantId, userId, request);
        };
        summaries.forEach(this::scaleSummary);
        return summaries;
    }

    private LambdaQueryWrapper<BizUserFundHolding> buildQueryWrapper(Long tenantId,
                                                                     Long userId,
                                                                     FundHoldingQueryRequest request,
                                                                     List<Long> holdingIds) {
        return new LambdaQueryWrapper<BizUserFundHolding>()
                .eq(BizUserFundHolding::getTenantId, tenantId)
                .eq(BizUserFundHolding::getUserId, userId)
                .eq(BizUserFundHolding::getDelFlag, "0")
                .eq(request.getPortfolioId() != null, BizUserFundHolding::getPortfolioId, request.getPortfolioId())
                .eq(StringUtils.hasText(request.getFundCode()), BizUserFundHolding::getFundCode, request.getFundCode())
                .eq(request.getStatus() != null, BizUserFundHolding::getStatus, request.getStatus())
                .in(holdingIds != null, BizUserFundHolding::getId, holdingIds)
                .and(StringUtils.hasText(request.getKeyword()), wrapper -> wrapper
                        .like(BizUserFundHolding::getFundCode, request.getKeyword())
                        .or()
                        .like(BizUserFundHolding::getFundName, request.getKeyword()))
                .orderByAsc(BizUserFundHolding::getSortOrder)
                .orderByAsc(BizUserFundHolding::getFundCode)
                .orderByDesc(BizUserFundHolding::getUpdateTime);
    }

    private BizUserFundHolding getOwnedHolding(Long tenantId, Long userId, Long id) {
        BizUserFundHolding holding = holdingMapper.selectOne(new LambdaQueryWrapper<BizUserFundHolding>()
                .eq(BizUserFundHolding::getTenantId, tenantId)
                .eq(BizUserFundHolding::getUserId, userId)
                .eq(BizUserFundHolding::getId, id)
                .eq(BizUserFundHolding::getDelFlag, "0"));
        if (holding == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "持仓不存在");
        }
        return holding;
    }

    private BizFundInfo getTenantFund(Long tenantId, Long fundId) {
        BizFundInfo fundInfo = fundInfoMapper.selectOne(new LambdaQueryWrapper<BizFundInfo>()
                .eq(BizFundInfo::getTenantId, tenantId)
                .eq(BizFundInfo::getId, fundId)
                .eq(BizFundInfo::getDelFlag, "0"));
        if (fundInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "基金不存在");
        }
        return fundInfo;
    }

    private void validatePortfolio(Long tenantId, Long userId, Long portfolioId) {
        if (portfolioId == null) {
            return;
        }
        Long count = portfolioMapper.selectCount(new LambdaQueryWrapper<BizUserPortfolio>()
                .eq(BizUserPortfolio::getTenantId, tenantId)
                .eq(BizUserPortfolio::getUserId, userId)
                .eq(BizUserPortfolio::getId, portfolioId)
                .eq(BizUserPortfolio::getStatus, STATUS_HOLDING)
                .eq(BizUserPortfolio::getDelFlag, "0"));
        if (count == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "投资组合不存在或已停用");
        }
    }

    private BizUserFundGroup validateGroup(Long tenantId, Long userId, Long groupId) {
        BizUserFundGroup group = groupMapper.selectOne(new LambdaQueryWrapper<BizUserFundGroup>()
                .eq(BizUserFundGroup::getTenantId, tenantId)
                .eq(BizUserFundGroup::getUserId, userId)
                .eq(BizUserFundGroup::getId, groupId)
                .eq(BizUserFundGroup::getStatus, STATUS_HOLDING)
                .eq(BizUserFundGroup::getDelFlag, "0"));
        if (group == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "基金分组不存在或已停用");
        }
        return group;
    }

    private List<BizUserFundGroup> validateGroups(Long tenantId, Long userId, Long portfolioId, List<Long> groupIds) {
        if (groupIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<BizUserFundGroup> groups = groupMapper.selectList(new LambdaQueryWrapper<BizUserFundGroup>()
                .eq(BizUserFundGroup::getTenantId, tenantId)
                .eq(BizUserFundGroup::getUserId, userId)
                .eq(BizUserFundGroup::getStatus, STATUS_HOLDING)
                .eq(BizUserFundGroup::getDelFlag, "0")
                .in(BizUserFundGroup::getId, groupIds));
        Set<Long> existsIds = groups.stream().map(BizUserFundGroup::getId).collect(Collectors.toSet());
        if (!existsIds.containsAll(groupIds)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "基金分组不存在或已停用");
        }
        for (BizUserFundGroup group : groups) {
            if (group.getPortfolioId() != null && !Objects.equals(group.getPortfolioId(), portfolioId)) {
                throw new BusinessException(ErrorCode.CONFLICT, "基金分组不属于当前投资组合");
            }
        }
        return groups;
    }

    private void assertHoldingUnique(Long tenantId, Long userId, Long id, Long portfolioId, Long fundId) {
        LambdaQueryWrapper<BizUserFundHolding> wrapper = new LambdaQueryWrapper<BizUserFundHolding>()
                .eq(BizUserFundHolding::getTenantId, tenantId)
                .eq(BizUserFundHolding::getUserId, userId)
                .eq(BizUserFundHolding::getFundId, fundId)
                .eq(BizUserFundHolding::getDelFlag, "0")
                .ne(id != null, BizUserFundHolding::getId, id);
        if (portfolioId == null) {
            wrapper.isNull(BizUserFundHolding::getPortfolioId);
        } else {
            wrapper.eq(BizUserFundHolding::getPortfolioId, portfolioId);
        }
        if (holdingMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.FUND_DUPLICATE, "该组合下基金持仓已存在");
        }
    }

    private List<Long> queryHoldingIdsByGroup(Long tenantId, Long userId, Long groupId) {
        if (groupId == null) {
            return null;
        }
        validateGroup(tenantId, userId, groupId);
        return groupRelationMapper.selectList(new LambdaQueryWrapper<BizUserFundGroupRelation>()
                        .eq(BizUserFundGroupRelation::getTenantId, tenantId)
                        .eq(BizUserFundGroupRelation::getUserId, userId)
                        .eq(BizUserFundGroupRelation::getGroupId, groupId)
                        .eq(BizUserFundGroupRelation::getDelFlag, "0"))
                .stream()
                .map(BizUserFundGroupRelation::getHoldingId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private void copy(FundHoldingRequest request, BizUserFundHolding holding, boolean create) {
        if (request.getHoldingShares() != null) {
            holding.setHoldingShares(request.getHoldingShares());
        }
        if (request.getAvailableShares() != null) {
            holding.setAvailableShares(request.getAvailableShares());
        }
        if (request.getFrozenShares() != null) {
            holding.setFrozenShares(request.getFrozenShares());
        }
        if (request.getCostAmount() != null) {
            holding.setCostAmount(request.getCostAmount());
        }
        if (request.getCostNav() != null) {
            holding.setCostNav(request.getCostNav());
        }
        if (request.getMarketValue() != null) {
            holding.setMarketValue(request.getMarketValue());
        }
        if (request.getLatestNav() != null) {
            holding.setLatestNav(request.getLatestNav());
        }
        if (request.getLatestNavDate() != null) {
            holding.setLatestNavDate(request.getLatestNavDate());
        }
        if (request.getTodayEstimatedValue() != null) {
            holding.setTodayEstimatedValue(request.getTodayEstimatedValue());
        }
        if (request.getTodayProfit() != null) {
            holding.setTodayProfit(request.getTodayProfit());
        }
        if (request.getFirstBuyDate() != null) {
            holding.setFirstBuyDate(request.getFirstBuyDate());
        }
        if (request.getLastTradeTime() != null) {
            holding.setLastTradeTime(request.getLastTradeTime());
        }
        if (request.getSortOrder() != null || create) {
            holding.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null || create) {
            holding.setStatus(request.getStatus());
        }
    }

    private void recalculate(FundHoldingRequest request, BizUserFundHolding holding, boolean create) {
        if (create && request.getMarketValue() == null && request.getLatestNav() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前市值或最新单位净值不能为空");
        }

        holding.setHoldingShares(scaleAmount(defaultAmount(holding.getHoldingShares())));
        holding.setFrozenShares(scaleAmount(defaultAmount(holding.getFrozenShares())));
        if (holding.getAvailableShares() == null) {
            holding.setAvailableShares(holding.getHoldingShares().subtract(holding.getFrozenShares()));
        }
        holding.setAvailableShares(scaleAmount(holding.getAvailableShares()));
        holding.setCostAmount(scaleAmount(defaultAmount(holding.getCostAmount())));

        if (request.getMarketValue() == null && holding.getLatestNav() != null) {
            holding.setMarketValue(holding.getHoldingShares().multiply(holding.getLatestNav()));
        }
        holding.setMarketValue(scaleAmount(defaultAmount(holding.getMarketValue())));
        if (holding.getCostNav() == null && holding.getHoldingShares().compareTo(BigDecimal.ZERO) > 0) {
            holding.setCostNav(holding.getCostAmount().divide(holding.getHoldingShares(), 6, RoundingMode.HALF_UP));
        }
        if (holding.getCostNav() != null) {
            holding.setCostNav(scaleNav(holding.getCostNav()));
        }
        if (holding.getLatestNav() != null) {
            holding.setLatestNav(scaleNav(holding.getLatestNav()));
        }
        if (holding.getTodayEstimatedValue() != null) {
            holding.setTodayEstimatedValue(scaleAmount(holding.getTodayEstimatedValue()));
        }
        if (holding.getTodayProfit() != null) {
            holding.setTodayProfit(scaleAmount(holding.getTodayProfit()));
        }

        if (holding.getAvailableShares().add(holding.getFrozenShares()).compareTo(holding.getHoldingShares()) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "可用份额和冻结份额之和不能大于持有份额");
        }

        BigDecimal holdingProfit = holding.getMarketValue().subtract(holding.getCostAmount());
        holding.setHoldingProfit(scaleAmount(holdingProfit));
        holding.setHoldingProfitRate(calculateProfitRate(holding.getHoldingProfit(), holding.getCostAmount()));
        if (holding.getSortOrder() == null) {
            holding.setSortOrder(DEFAULT_SORT_ORDER);
        }
        if (holding.getStatus() == null) {
            holding.setStatus(STATUS_HOLDING);
        }
    }

    private void syncGroups(Long tenantId, Long userId, BizUserFundHolding holding, List<Long> groupIds) {
        deleteHoldingGroups(tenantId, userId, holding.getId());
        for (Long groupId : groupIds) {
            BizUserFundGroupRelation relation = new BizUserFundGroupRelation();
            relation.setTenantId(tenantId);
            relation.setUserId(userId);
            relation.setGroupId(groupId);
            relation.setHoldingId(holding.getId());
            relation.setFundId(holding.getFundId());
            relation.setSortOrder(DEFAULT_SORT_ORDER);
            groupRelationMapper.insert(relation);
        }
    }

    private void deleteHoldingGroups(Long tenantId, Long userId, Long holdingId) {
        groupRelationMapper.deleteByHoldingId(tenantId, userId, holdingId);
    }

    private List<FundHoldingVO> toVOList(Long tenantId, Long userId, List<BizUserFundHolding> holdings) {
        if (holdings.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, String> portfolioNameMap = loadPortfolioNameMap(tenantId, userId, holdings);
        Map<Long, List<BizUserFundGroupRelation>> relationMap = loadGroupRelationMap(tenantId, userId, holdings);
        Map<Long, String> groupNameMap = loadGroupNameMap(tenantId, userId, relationMap);
        List<FundHoldingVO> result = new ArrayList<>();
        for (BizUserFundHolding holding : holdings) {
            result.add(toVO(holding, portfolioNameMap, relationMap, groupNameMap));
        }
        return result;
    }

    private Map<Long, String> loadPortfolioNameMap(Long tenantId, Long userId, List<BizUserFundHolding> holdings) {
        List<Long> portfolioIds = holdings.stream()
                .map(BizUserFundHolding::getPortfolioId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (portfolioIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return portfolioMapper.selectList(new LambdaQueryWrapper<BizUserPortfolio>()
                        .eq(BizUserPortfolio::getTenantId, tenantId)
                        .eq(BizUserPortfolio::getUserId, userId)
                        .eq(BizUserPortfolio::getDelFlag, "0")
                        .in(BizUserPortfolio::getId, portfolioIds))
                .stream()
                .collect(Collectors.toMap(BizUserPortfolio::getId, BizUserPortfolio::getPortfolioName));
    }

    private Map<Long, List<BizUserFundGroupRelation>> loadGroupRelationMap(Long tenantId,
                                                                            Long userId,
                                                                            List<BizUserFundHolding> holdings) {
        List<Long> holdingIds = holdings.stream().map(BizUserFundHolding::getId).toList();
        return groupRelationMapper.selectList(new LambdaQueryWrapper<BizUserFundGroupRelation>()
                        .eq(BizUserFundGroupRelation::getTenantId, tenantId)
                        .eq(BizUserFundGroupRelation::getUserId, userId)
                        .eq(BizUserFundGroupRelation::getDelFlag, "0")
                        .in(BizUserFundGroupRelation::getHoldingId, holdingIds)
                        .orderByAsc(BizUserFundGroupRelation::getSortOrder)
                        .orderByAsc(BizUserFundGroupRelation::getGroupId))
                .stream()
                .collect(Collectors.groupingBy(BizUserFundGroupRelation::getHoldingId));
    }

    private Map<Long, String> loadGroupNameMap(Long tenantId,
                                               Long userId,
                                               Map<Long, List<BizUserFundGroupRelation>> relationMap) {
        List<Long> groupIds = relationMap.values().stream()
                .flatMap(List::stream)
                .map(BizUserFundGroupRelation::getGroupId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (groupIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return groupMapper.selectList(new LambdaQueryWrapper<BizUserFundGroup>()
                        .eq(BizUserFundGroup::getTenantId, tenantId)
                        .eq(BizUserFundGroup::getUserId, userId)
                        .eq(BizUserFundGroup::getDelFlag, "0")
                        .in(BizUserFundGroup::getId, groupIds))
                .stream()
                .collect(Collectors.toMap(BizUserFundGroup::getId, BizUserFundGroup::getGroupName));
    }

    private FundHoldingVO toVO(BizUserFundHolding holding,
                               Map<Long, String> portfolioNameMap,
                               Map<Long, List<BizUserFundGroupRelation>> relationMap,
                               Map<Long, String> groupNameMap) {
        FundHoldingVO vo = new FundHoldingVO();
        vo.setId(holding.getId());
        vo.setPortfolioId(holding.getPortfolioId());
        vo.setPortfolioName(portfolioNameMap.get(holding.getPortfolioId()));
        vo.setFundId(holding.getFundId());
        vo.setFundCode(holding.getFundCode());
        vo.setFundName(holding.getFundName());
        vo.setHoldingShares(scaleAmount(holding.getHoldingShares()));
        vo.setAvailableShares(scaleAmount(holding.getAvailableShares()));
        vo.setFrozenShares(scaleAmount(holding.getFrozenShares()));
        vo.setCostAmount(scaleAmount(holding.getCostAmount()));
        vo.setCostNav(scaleNav(holding.getCostNav()));
        vo.setMarketValue(scaleAmount(holding.getMarketValue()));
        vo.setLatestNav(scaleNav(holding.getLatestNav()));
        vo.setLatestNavDate(holding.getLatestNavDate());
        vo.setTodayEstimatedValue(scaleAmount(holding.getTodayEstimatedValue()));
        vo.setTodayProfit(scaleAmount(holding.getTodayProfit()));
        vo.setHoldingProfit(scaleAmount(holding.getHoldingProfit()));
        vo.setProfitLossAmount(vo.getHoldingProfit());
        vo.setHoldingProfitRate(scaleRate(holding.getHoldingProfitRate()));
        vo.setFirstBuyDate(holding.getFirstBuyDate());
        vo.setLastTradeTime(holding.getLastTradeTime());
        vo.setSortOrder(holding.getSortOrder());
        vo.setStatus(holding.getStatus());
        vo.setCreateTime(holding.getCreateTime());
        vo.setUpdateTime(holding.getUpdateTime());

        List<BizUserFundGroupRelation> relations = relationMap.getOrDefault(holding.getId(), Collections.emptyList());
        vo.setGroupIds(relations.stream().map(BizUserFundGroupRelation::getGroupId).filter(Objects::nonNull).toList());
        vo.setGroupNames(relations.stream()
                .map(relation -> groupNameMap.get(relation.getGroupId()))
                .filter(StringUtils::hasText)
                .toList());
        return vo;
    }

    private String normalizeDimension(String dimension) {
        String value = StringUtils.hasText(dimension) ? dimension.trim().toUpperCase() : "USER";
        if ("ACCOUNT".equals(value)) {
            return "USER";
        }
        if (!Set.of("USER", "PORTFOLIO", "GROUP").contains(value)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "汇总维度仅支持USER、PORTFOLIO、GROUP");
        }
        return value;
    }

    private List<Long> distinctIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(ids.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal calculateProfitRate(BigDecimal profit, BigDecimal costAmount) {
        if (costAmount == null || costAmount.compareTo(BigDecimal.ZERO) == 0) {
            return scaleRate(BigDecimal.ZERO);
        }
        return profit.multiply(ONE_HUNDRED).divide(costAmount, 4, RoundingMode.HALF_UP);
    }

    private void scaleSummary(FundHoldingSummaryVO summary) {
        summary.setTotalHoldingShares(scaleAmount(summary.getTotalHoldingShares()));
        summary.setTotalCostAmount(scaleAmount(summary.getTotalCostAmount()));
        summary.setTotalMarketValue(scaleAmount(summary.getTotalMarketValue()));
        summary.setTotalTodayEstimatedValue(scaleAmount(summary.getTotalTodayEstimatedValue()));
        summary.setTotalTodayProfit(scaleAmount(summary.getTotalTodayProfit()));
        summary.setTotalHoldingProfit(scaleAmount(summary.getTotalHoldingProfit()));
        summary.setTotalProfitLossAmount(scaleAmount(summary.getTotalProfitLossAmount()));
        summary.setTotalHoldingProfitRate(scaleRate(summary.getTotalHoldingProfitRate()));
    }

    private BigDecimal scaleAmount(BigDecimal value) {
        return value == null ? null : value.setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleNav(BigDecimal value) {
        return value == null ? null : value.setScale(6, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleRate(BigDecimal value) {
        return value == null ? null : value.setScale(4, RoundingMode.HALF_UP);
    }
}
