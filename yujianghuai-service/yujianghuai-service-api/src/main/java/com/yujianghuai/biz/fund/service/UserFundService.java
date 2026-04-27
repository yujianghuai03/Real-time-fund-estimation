package com.yujianghuai.biz.fund.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yujianghuai.biz.fund.entity.UserFund;
import com.yujianghuai.biz.fund.entity.UserFundGroup;
import com.yujianghuai.biz.fund.entity.UserFundGroupRelation;
import com.yujianghuai.biz.fund.entity.UserFundTransaction;
import com.yujianghuai.biz.fund.mapper.UserFundGroupMapper;
import com.yujianghuai.biz.fund.mapper.UserFundGroupRelationMapper;
import com.yujianghuai.biz.fund.mapper.UserFundMapper;
import com.yujianghuai.biz.fund.mapper.UserFundTransactionMapper;
import com.yujianghuai.biz.fund.model.FundEstimateVO;
import com.yujianghuai.biz.fund.model.FundGroupVO;
import com.yujianghuai.biz.fund.model.FundSearchVO;
import com.yujianghuai.biz.fund.model.FundSnapshotFund;
import com.yujianghuai.biz.fund.model.FundSnapshotGroup;
import com.yujianghuai.biz.fund.model.FundSnapshotRequest;
import com.yujianghuai.biz.fund.model.FundSnapshotTransaction;
import com.yujianghuai.biz.fund.model.FundTransactionVO;
import com.yujianghuai.biz.fund.model.FundWatchRequest;
import com.yujianghuai.common.exception.BizException;
import com.yujianghuai.common.tenant.TenantContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserFundService {

    private static final String UNAUTHORIZED_MESSAGE = "权限不足，请登录后再试！";

    private final UserFundMapper userFundMapper;
    private final UserFundGroupMapper userFundGroupMapper;
    private final UserFundGroupRelationMapper userFundGroupRelationMapper;
    private final UserFundTransactionMapper userFundTransactionMapper;
    private final FundMarketClient fundMarketClient;

    public UserFundService(UserFundMapper userFundMapper,
                           UserFundGroupMapper userFundGroupMapper,
                           UserFundGroupRelationMapper userFundGroupRelationMapper,
                           UserFundTransactionMapper userFundTransactionMapper,
                           FundMarketClient fundMarketClient) {
        this.userFundMapper = userFundMapper;
        this.userFundGroupMapper = userFundGroupMapper;
        this.userFundGroupRelationMapper = userFundGroupRelationMapper;
        this.userFundTransactionMapper = userFundTransactionMapper;
        this.fundMarketClient = fundMarketClient;
    }

    public List<FundSearchVO> search(String keyword) {
        return fundMarketClient.search(keyword);
    }

    public FundEstimateVO estimate(String code) {
        return fundMarketClient.estimate(normalizeCode(code));
    }

    public List<FundEstimateVO> listWithRealtimeEstimate(Principal principal) {
        String username = currentUsername(principal);
        List<UserFund> funds = userFundMapper.selectList(new LambdaQueryWrapper<UserFund>()
                        .eq(UserFund::getUsername, username)
                        .eq(UserFund::getDelFlag, "0")
                        .orderByAsc(UserFund::getSortOrder)
                        .orderByDesc(UserFund::getId));
        Map<String, List<Long>> groupIdMap = listRelationGroupIds(username);
        return funds.stream()
                .map((fund) -> {
                    FundEstimateVO estimate = toEstimate(fund);
                    estimate.setGroupIds(groupIdMap.getOrDefault(fund.getFundCode(), List.of()));
                    return estimate;
                })
                .toList();
    }

    public List<FundGroupVO> listGroups(Principal principal) {
        String username = currentUsername(principal);
        Map<Long, Long> countMap = userFundGroupRelationMapper.selectList(new LambdaQueryWrapper<UserFundGroupRelation>()
                        .eq(UserFundGroupRelation::getUsername, username)
                        .eq(UserFundGroupRelation::getDelFlag, "0"))
                .stream()
                .collect(Collectors.groupingBy(UserFundGroupRelation::getGroupId,
                        Collectors.mapping(UserFundGroupRelation::getFundCode, Collectors.collectingAndThen(Collectors.toSet(), (set) -> (long) set.size()))));
        return listCustomGroups(username).stream()
                .map((group) -> toGroupVO(group, countMap.getOrDefault(group.getId(), 0L)))
                .toList();
    }

    public List<FundTransactionVO> listTransactions(Principal principal) {
        String username = currentUsername(principal);
        return userFundTransactionMapper.selectList(new LambdaQueryWrapper<UserFundTransaction>()
                        .eq(UserFundTransaction::getUsername, username)
                        .eq(UserFundTransaction::getDelFlag, "0")
                        .orderByDesc(UserFundTransaction::getTradeTime)
                        .orderByDesc(UserFundTransaction::getId))
                .stream()
                .map(this::toTransactionVO)
                .toList();
    }

    @Transactional
    public FundGroupVO createGroup(Principal principal, String name) {
        String username = currentUsername(principal);
        String groupName = normalizeGroupName(name);
        ensureGroupNameAvailable(username, groupName, null);
        UserFundGroup group = new UserFundGroup();
        group.setTenantId(currentTenantId());
        group.setUsername(username);
        group.setGroupName(groupName);
        group.setSortOrder(0);
        group.setDelFlag("0");
        userFundGroupMapper.insert(group);
        return toGroupVO(group, 0L);
    }

    @Transactional
    public FundGroupVO updateGroup(Principal principal, Long groupId, String name) {
        String username = currentUsername(principal);
        String groupName = normalizeGroupName(name);
        UserFundGroup group = getCustomGroup(username, groupId);
        ensureGroupNameAvailable(username, groupName, groupId);
        group.setGroupName(groupName);
        userFundGroupMapper.updateById(group);
        return toGroupVO(group, countGroupFunds(username, groupId));
    }

    @Transactional
    public Boolean deleteGroup(Principal principal, Long groupId) {
        String username = currentUsername(principal);
        getCustomGroup(username, groupId);
        userFundGroupMapper.update(new LambdaUpdateWrapper<UserFundGroup>()
                .set(UserFundGroup::getDelFlag, "1")
                .eq(UserFundGroup::getUsername, username)
                .eq(UserFundGroup::getId, groupId));
        userFundGroupRelationMapper.update(new LambdaUpdateWrapper<UserFundGroupRelation>()
                .set(UserFundGroupRelation::getDelFlag, "1")
                .eq(UserFundGroupRelation::getUsername, username)
                .eq(UserFundGroupRelation::getGroupId, groupId));
        return Boolean.TRUE;
    }

    @Transactional
    public Boolean updateFundGroups(Principal principal, String code, List<Long> groupIds) {
        String username = currentUsername(principal);
        String fundCode = normalizeCode(code);
        ensureActiveFund(username, fundCode);
        Set<Long> uniqueGroupIds = new HashSet<>(groupIds == null ? List.of() : groupIds);
        Set<Long> availableGroupIds = listCustomGroups(username).stream()
                .map(UserFundGroup::getId)
                .collect(Collectors.toSet());
        if (!availableGroupIds.containsAll(uniqueGroupIds)) {
            throw new BizException(404, "基金分组不存在");
        }
        userFundGroupRelationMapper.update(new LambdaUpdateWrapper<UserFundGroupRelation>()
                .set(UserFundGroupRelation::getDelFlag, "1")
                .eq(UserFundGroupRelation::getUsername, username)
                .eq(UserFundGroupRelation::getFundCode, fundCode));
        uniqueGroupIds.forEach((groupId) -> {
            UserFundGroupRelation relation = new UserFundGroupRelation();
            relation.setTenantId(currentTenantId());
            relation.setUsername(username);
            relation.setGroupId(groupId);
            relation.setFundCode(fundCode);
            relation.setDelFlag("0");
            userFundGroupRelationMapper.insert(relation);
        });
        return Boolean.TRUE;
    }

    @Transactional
    public FundEstimateVO add(Principal principal, FundWatchRequest request) {
        String username = currentUsername(principal);
        String code = normalizeCode(request.getCode());
        UserFund exists = userFundMapper.selectOne(new LambdaQueryWrapper<UserFund>()
                .eq(UserFund::getUsername, username)
                .eq(UserFund::getFundCode, code));
        if (exists != null) {
            exists.setDelFlag("0");
            exists.setFundName(resolveName(code, request.getName()));
            exists.setHoldingAmount(nullToZero(request.getHoldingAmount()));
            exists.setHoldingCost(nullToZero(request.getHoldingCost()));
            userFundMapper.updateById(exists);
            return toEstimate(exists);
        }

        UserFund fund = new UserFund();
        fund.setTenantId(currentTenantId());
        fund.setUsername(username);
        fund.setFundCode(code);
        fund.setFundName(resolveName(code, request.getName()));
        fund.setHoldingAmount(nullToZero(request.getHoldingAmount()));
        fund.setHoldingCost(nullToZero(request.getHoldingCost()));
        fund.setSortOrder(0);
        fund.setDelFlag("0");
        userFundMapper.insert(fund);
        return toEstimate(fund);
    }

    @Transactional
    public Boolean replaceFromSnapshot(Principal principal, FundSnapshotRequest request) {
        String username = currentUsername(principal);
        clearCloudData(username);
        applySnapshot(username, request, true);
        return Boolean.TRUE;
    }

    @Transactional
    public Boolean mergeFromSnapshot(Principal principal, FundSnapshotRequest request) {
        String username = currentUsername(principal);
        applySnapshot(username, request, false);
        return Boolean.TRUE;
    }

    @Transactional
    public Boolean updateHolding(Principal principal, String code, BigDecimal holdingAmount, BigDecimal holdingCost) {
        String username = currentUsername(principal);
        int updated = userFundMapper.update(new LambdaUpdateWrapper<UserFund>()
                .set(UserFund::getHoldingAmount, nullToZero(holdingAmount))
                .set(UserFund::getHoldingCost, nullToZero(holdingCost))
                .eq(UserFund::getUsername, username)
                .eq(UserFund::getFundCode, normalizeCode(code))
                .eq(UserFund::getDelFlag, "0"));
        if (updated == 0) {
            throw new BizException(404, "自选基金不存在");
        }
        return Boolean.TRUE;
    }

    @Transactional
    public Boolean delete(Principal principal, String code) {
        String username = currentUsername(principal);
        userFundMapper.update(new LambdaUpdateWrapper<UserFund>()
                .set(UserFund::getDelFlag, "1")
                .eq(UserFund::getUsername, username)
                .eq(UserFund::getFundCode, normalizeCode(code)));
        userFundGroupRelationMapper.update(new LambdaUpdateWrapper<UserFundGroupRelation>()
                .set(UserFundGroupRelation::getDelFlag, "1")
                .eq(UserFundGroupRelation::getUsername, username)
                .eq(UserFundGroupRelation::getFundCode, normalizeCode(code)));
        return Boolean.TRUE;
    }

    private List<UserFundGroup> listCustomGroups(String username) {
        return userFundGroupMapper.selectList(new LambdaQueryWrapper<UserFundGroup>()
                .eq(UserFundGroup::getUsername, username)
                .eq(UserFundGroup::getDelFlag, "0")
                .orderByAsc(UserFundGroup::getSortOrder)
                .orderByAsc(UserFundGroup::getId));
    }

    private void clearCloudData(String username) {
        userFundMapper.update(new LambdaUpdateWrapper<UserFund>()
                .set(UserFund::getDelFlag, "1")
                .eq(UserFund::getUsername, username));
        userFundGroupMapper.update(new LambdaUpdateWrapper<UserFundGroup>()
                .set(UserFundGroup::getDelFlag, "1")
                .eq(UserFundGroup::getUsername, username));
        userFundGroupRelationMapper.update(new LambdaUpdateWrapper<UserFundGroupRelation>()
                .set(UserFundGroupRelation::getDelFlag, "1")
                .eq(UserFundGroupRelation::getUsername, username));
        userFundTransactionMapper.update(new LambdaUpdateWrapper<UserFundTransaction>()
                .set(UserFundTransaction::getDelFlag, "1")
                .eq(UserFundTransaction::getUsername, username));
    }

    private void applySnapshot(String username, FundSnapshotRequest request, boolean replace) {
        List<FundSnapshotGroup> requestGroups = request == null || request.getGroups() == null ? List.of() : request.getGroups();
        List<FundSnapshotFund> requestFunds = request == null || request.getFunds() == null ? List.of() : request.getFunds();
        List<FundSnapshotTransaction> requestTransactions = request == null || request.getTransactions() == null ? List.of() : request.getTransactions();
        Map<String, UserFundGroup> existingGroupsByName = listCustomGroups(username).stream()
                .collect(Collectors.toMap(UserFundGroup::getGroupName, Function.identity(), (left, right) -> left));
        Map<Long, Long> groupIdMap = requestGroups.stream()
                .collect(Collectors.toMap(FundSnapshotGroup::getId, (group) -> ensureSnapshotGroup(username, group, existingGroupsByName, replace), (left, right) -> left));
        requestFunds.forEach((fund) -> {
            String code = normalizeCode(fund.getCode());
            ensureSnapshotFund(username, fund, replace);
            if (fund.getGroupIds() != null) {
                fund.getGroupIds().stream()
                        .map(groupIdMap::get)
                        .filter((groupId) -> groupId != null)
                        .forEach((groupId) -> ensureRelation(username, code, groupId));
            }
        });
        requestTransactions.forEach((transaction) -> insertSnapshotTransaction(username, transaction));
    }

    private void insertSnapshotTransaction(String username, FundSnapshotTransaction snapshot) {
        if (!StringUtils.hasText(snapshot.getFundCode()) || !StringUtils.hasText(snapshot.getTradeType())) {
            return;
        }
        UserFundTransaction transaction = new UserFundTransaction();
        transaction.setTenantId(currentTenantId());
        transaction.setUsername(username);
        transaction.setFundCode(normalizeCode(snapshot.getFundCode()));
        transaction.setFundName(StringUtils.hasText(snapshot.getFundName()) ? snapshot.getFundName().trim() : snapshot.getFundCode());
        transaction.setTradeType(snapshot.getTradeType());
        transaction.setAmount(nullToZero(snapshot.getAmount()));
        transaction.setBeforeAmount(nullToZero(snapshot.getBeforeAmount()));
        transaction.setAfterAmount(nullToZero(snapshot.getAfterAmount()));
        transaction.setTargetFundCode(snapshot.getTargetFundCode());
        transaction.setTargetFundName(snapshot.getTargetFundName());
        transaction.setRemark(snapshot.getRemark());
        transaction.setTradeTime(parseTradeTime(snapshot.getTradeTime()));
        transaction.setDelFlag("0");
        userFundTransactionMapper.insert(transaction);
    }

    private Long ensureSnapshotGroup(String username, FundSnapshotGroup snapshotGroup,
                                     Map<String, UserFundGroup> existingGroupsByName, boolean replace) {
        String groupName = normalizeGroupName(snapshotGroup.getName());
        UserFundGroup existing = existingGroupsByName.get(groupName);
        if (!replace && existing != null) {
            return existing.getId();
        }
        UserFundGroup group = new UserFundGroup();
        group.setTenantId(currentTenantId());
        group.setUsername(username);
        group.setGroupName(groupName);
        group.setSortOrder(0);
        group.setDelFlag("0");
        userFundGroupMapper.insert(group);
        existingGroupsByName.put(groupName, group);
        return group.getId();
    }

    private void ensureSnapshotFund(String username, FundSnapshotFund snapshotFund, boolean replace) {
        String code = normalizeCode(snapshotFund.getCode());
        UserFund exists = userFundMapper.selectOne(new LambdaQueryWrapper<UserFund>()
                .eq(UserFund::getUsername, username)
                .eq(UserFund::getFundCode, code)
                .eq(UserFund::getDelFlag, "0"));
        if (!replace && exists != null) {
            return;
        }
        UserFund fund = exists == null ? new UserFund() : exists;
        fund.setTenantId(currentTenantId());
        fund.setUsername(username);
        fund.setFundCode(code);
        fund.setFundName(StringUtils.hasText(snapshotFund.getName()) ? snapshotFund.getName().trim() : code);
        fund.setHoldingAmount(nullToZero(snapshotFund.getHoldingAmount()));
        fund.setHoldingCost(nullToZero(snapshotFund.getHoldingCost()));
        fund.setSortOrder(0);
        fund.setDelFlag("0");
        if (exists == null) {
            userFundMapper.insert(fund);
        } else {
            userFundMapper.updateById(fund);
        }
    }

    private void ensureRelation(String username, String code, Long groupId) {
        Long count = userFundGroupRelationMapper.selectCount(new LambdaQueryWrapper<UserFundGroupRelation>()
                .eq(UserFundGroupRelation::getUsername, username)
                .eq(UserFundGroupRelation::getFundCode, code)
                .eq(UserFundGroupRelation::getGroupId, groupId)
                .eq(UserFundGroupRelation::getDelFlag, "0"));
        if (count != null && count > 0) {
            return;
        }
        UserFundGroupRelation relation = new UserFundGroupRelation();
        relation.setTenantId(currentTenantId());
        relation.setUsername(username);
        relation.setGroupId(groupId);
        relation.setFundCode(code);
        relation.setDelFlag("0");
        userFundGroupRelationMapper.insert(relation);
    }

    private UserFundGroup getCustomGroup(String username, Long groupId) {
        UserFundGroup group = userFundGroupMapper.selectOne(new LambdaQueryWrapper<UserFundGroup>()
                .eq(UserFundGroup::getUsername, username)
                .eq(UserFundGroup::getId, groupId)
                .eq(UserFundGroup::getDelFlag, "0"));
        if (group == null) {
            throw new BizException(404, "基金分组不存在");
        }
        return group;
    }

    private void ensureActiveFund(String username, String code) {
        Long count = userFundMapper.selectCount(new LambdaQueryWrapper<UserFund>()
                .eq(UserFund::getUsername, username)
                .eq(UserFund::getFundCode, code)
                .eq(UserFund::getDelFlag, "0"));
        if (count == null || count == 0) {
            throw new BizException(404, "自选基金不存在");
        }
    }

    private void ensureGroupNameAvailable(String username, String groupName, Long excludeId) {
        LambdaQueryWrapper<UserFundGroup> wrapper = new LambdaQueryWrapper<UserFundGroup>()
                .eq(UserFundGroup::getUsername, username)
                .eq(UserFundGroup::getGroupName, groupName)
                .eq(UserFundGroup::getDelFlag, "0");
        if (excludeId != null) {
            wrapper.ne(UserFundGroup::getId, excludeId);
        }
        Long count = userFundGroupMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BizException(400, "基金分组名称已存在");
        }
    }

    private Map<String, List<Long>> listRelationGroupIds(String username) {
        return userFundGroupRelationMapper.selectList(new LambdaQueryWrapper<UserFundGroupRelation>()
                        .eq(UserFundGroupRelation::getUsername, username)
                        .eq(UserFundGroupRelation::getDelFlag, "0"))
                .stream()
                .collect(Collectors.groupingBy(UserFundGroupRelation::getFundCode,
                        Collectors.mapping(UserFundGroupRelation::getGroupId, Collectors.toCollection(ArrayList::new))));
    }

    private long countGroupFunds(String username, Long groupId) {
        return userFundGroupRelationMapper.selectList(new LambdaQueryWrapper<UserFundGroupRelation>()
                        .eq(UserFundGroupRelation::getUsername, username)
                        .eq(UserFundGroupRelation::getGroupId, groupId)
                        .eq(UserFundGroupRelation::getDelFlag, "0"))
                .stream()
                .map(UserFundGroupRelation::getFundCode)
                .collect(Collectors.toSet())
                .size();
    }

    private FundGroupVO toGroupVO(UserFundGroup group, Long count) {
        FundGroupVO vo = new FundGroupVO();
        vo.setId(group.getId());
        vo.setName(group.getGroupName());
        vo.setCount(count);
        return vo;
    }

    private FundTransactionVO toTransactionVO(UserFundTransaction transaction) {
        FundTransactionVO vo = new FundTransactionVO();
        vo.setId(transaction.getId());
        vo.setFundCode(transaction.getFundCode());
        vo.setFundName(transaction.getFundName());
        vo.setTradeType(transaction.getTradeType());
        vo.setAmount(transaction.getAmount());
        vo.setBeforeAmount(transaction.getBeforeAmount());
        vo.setAfterAmount(transaction.getAfterAmount());
        vo.setTargetFundCode(transaction.getTargetFundCode());
        vo.setTargetFundName(transaction.getTargetFundName());
        vo.setRemark(transaction.getRemark());
        vo.setTradeTime(transaction.getTradeTime() == null ? null : transaction.getTradeTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return vo;
    }

    private LocalDateTime parseTradeTime(String tradeTime) {
        if (!StringUtils.hasText(tradeTime)) {
            return LocalDateTime.now();
        }
        try {
            return OffsetDateTime.parse(tradeTime).toLocalDateTime();
        } catch (RuntimeException ignored) {
            // Try local date time below.
        }
        try {
            return LocalDateTime.parse(tradeTime);
        } catch (RuntimeException exception) {
            return LocalDateTime.now();
        }
    }

    private FundEstimateVO toEstimate(UserFund fund) {
        FundEstimateVO estimate = fundMarketClient.estimate(fund.getFundCode());
        estimate.setId(fund.getId());
        estimate.setCode(fund.getFundCode());
        estimate.setName(StringUtils.hasText(estimate.getName()) && estimate.getError() == null
                ? estimate.getName() : fund.getFundName());
        estimate.setHoldingAmount(nullToZero(fund.getHoldingAmount()));
        estimate.setHoldingCost(nullToZero(fund.getHoldingCost()));
        BigDecimal rate = estimate.getEstimateRate() == null ? BigDecimal.ZERO : estimate.getEstimateRate();
        BigDecimal profit = estimate.getHoldingAmount().multiply(rate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        estimate.setEstimateProfit(profit);
        estimate.setEstimateMarketValue(estimate.getHoldingAmount().add(profit));
        return estimate;
    }

    private String resolveName(String code, String requestName) {
        if (StringUtils.hasText(requestName)) {
            return requestName.trim();
        }
        FundEstimateVO estimate = fundMarketClient.estimate(code);
        return StringUtils.hasText(estimate.getName()) ? estimate.getName() : code;
    }

    private String normalizeCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BizException(400, "基金代码不能为空");
        }
        return code.trim();
    }

    private String normalizeGroupName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new BizException(400, "基金分组名称不能为空");
        }
        String groupName = name.trim();
        if ("全部".equals(groupName) || "自选".equals(groupName)) {
            throw new BizException(400, "系统分组不能修改");
        }
        if (groupName.length() > 30) {
            throw new BizException(400, "基金分组名称不能超过30个字符");
        }
        return groupName;
    }

    private Long currentTenantId() {
        return Long.parseLong(TenantContext.getRequiredTenantId());
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String currentUsername(Principal principal) {
        if (principal == null || !StringUtils.hasText(principal.getName())) {
            throw new BizException(401, UNAUTHORIZED_MESSAGE);
        }
        return principal.getName();
    }
}
