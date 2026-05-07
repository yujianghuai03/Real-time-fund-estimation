package com.yujianghuai.fund.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yujianghuai.common.exception.BusinessException;
import com.yujianghuai.common.exception.ErrorCode;
import com.yujianghuai.common.tenant.TenantContext;
import com.yujianghuai.common.web.PageResult;
import com.yujianghuai.fund.entity.BizFundInfo;
import com.yujianghuai.fund.entity.BizFundTag;
import com.yujianghuai.fund.entity.BizFundTagRelation;
import com.yujianghuai.fund.mapper.BizFundInfoMapper;
import com.yujianghuai.fund.mapper.BizFundTagMapper;
import com.yujianghuai.fund.mapper.BizFundTagRelationMapper;
import com.yujianghuai.fund.model.dto.FundInfoQueryRequest;
import com.yujianghuai.fund.model.dto.FundInfoRequest;
import com.yujianghuai.fund.model.dto.FundStatusRequest;
import com.yujianghuai.fund.model.dto.FundTagBindRequest;
import com.yujianghuai.fund.model.vo.FundInfoVO;
import com.yujianghuai.fund.model.vo.FundTagVO;
import com.yujianghuai.fund.service.BizFundInfoService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
 * 基金基础信息表。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizFundInfoServiceImpl extends ServiceImpl<BizFundInfoMapper, BizFundInfo> implements BizFundInfoService {

    private static final String DEFAULT_CURRENCY = "CNY";
    private static final Integer STATUS_ENABLED = 1;
    private static final Integer STATUS_NORMAL = 1;

    private final BizFundInfoMapper fundInfoMapper;
    private final BizFundTagMapper fundTagMapper;
    private final BizFundTagRelationMapper fundTagRelationMapper;

    @Override
    public PageResult<FundInfoVO> page(FundInfoQueryRequest request) {
        Long tenantId = currentTenantId();
        List<Long> filterFundIds = queryFundIdsByTags(tenantId, request.getTagIds());
        if (filterFundIds != null && filterFundIds.isEmpty()) {
            return PageResult.of(Collections.emptyList(), 0L, request.getPageNum(), request.getPageSize());
        }

        LambdaQueryWrapper<BizFundInfo> wrapper = new LambdaQueryWrapper<BizFundInfo>()
                .eq(BizFundInfo::getTenantId, tenantId)
                .eq(BizFundInfo::getDelFlag, "0")
                .eq(StringUtils.hasText(request.getFundCode()), BizFundInfo::getFundCode, request.getFundCode())
                .eq(StringUtils.hasText(request.getFundType()), BizFundInfo::getFundType, request.getFundType())
                .eq(StringUtils.hasText(request.getFundCompany()), BizFundInfo::getFundCompany, request.getFundCompany())
                .eq(request.getRiskLevel() != null, BizFundInfo::getRiskLevel, request.getRiskLevel())
                .eq(request.getPurchaseStatus() != null, BizFundInfo::getPurchaseStatus, request.getPurchaseStatus())
                .eq(request.getRedeemStatus() != null, BizFundInfo::getRedeemStatus, request.getRedeemStatus())
                .eq(request.getStatus() != null, BizFundInfo::getStatus, request.getStatus())
                .in(filterFundIds != null, BizFundInfo::getId, filterFundIds)
                .and(StringUtils.hasText(request.getKeyword()), keywordWrapper -> keywordWrapper
                        .like(BizFundInfo::getFundCode, request.getKeyword())
                        .or()
                        .like(BizFundInfo::getFundName, request.getKeyword())
                        .or()
                        .like(BizFundInfo::getFundShortName, request.getKeyword())
                        .or()
                        .like(BizFundInfo::getFundCompany, request.getKeyword()))
                .orderByDesc(BizFundInfo::getUpdateTime)
                .orderByAsc(BizFundInfo::getFundCode);

        Page<BizFundInfo> page = fundInfoMapper.selectPage(new Page<>(request.getPageNum(), request.getPageSize()), wrapper);
        List<FundInfoVO> records = toVOList(tenantId, page.getRecords());
        return PageResult.of(page, records);
    }

    @Override
    public FundInfoVO detail(Long id) {
        Long tenantId = currentTenantId();
        BizFundInfo fundInfo = getTenantFund(tenantId, id);
        return toVO(tenantId, fundInfo, listTagMap(tenantId, List.of(id)).getOrDefault(id, Collections.emptyList()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FundInfoVO create(FundInfoRequest request) {
        Long tenantId = currentTenantId();
        assertFundCodeUnique(tenantId, null, request.getFundCode());

        BizFundInfo fundInfo = new BizFundInfo();
        fundInfo.setTenantId(tenantId);
        copy(request, fundInfo);
        fillDefaults(fundInfo);
        fundInfoMapper.insert(fundInfo);
        syncTags(tenantId, fundInfo.getId(), request.getTagIds() == null ? Collections.emptyList() : request.getTagIds());

        log.info("创建基金信息 tenantId={}, fundCode={}", tenantId, fundInfo.getFundCode());
        return detail(fundInfo.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FundInfoVO update(Long id, FundInfoRequest request) {
        Long tenantId = currentTenantId();
        BizFundInfo fundInfo = getTenantFund(tenantId, id);
        String newFundCode = StringUtils.hasText(request.getFundCode()) ? request.getFundCode() : fundInfo.getFundCode();
        assertFundCodeUnique(tenantId, id, newFundCode);

        copy(request, fundInfo);
        fillDefaults(fundInfo);
        fundInfoMapper.updateById(fundInfo);
        if (request.getTagIds() != null) {
            syncTags(tenantId, id, request.getTagIds());
        }

        log.info("修改基金信息 tenantId={}, fundCode={}", tenantId, fundInfo.getFundCode());
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FundInfoVO updateStatus(Long id, FundStatusRequest request) {
        Long tenantId = currentTenantId();
        if (request.getPurchaseStatus() == null && request.getRedeemStatus() == null && request.getStatus() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "至少传入一个状态字段");
        }

        BizFundInfo fundInfo = getTenantFund(tenantId, id);
        if (request.getPurchaseStatus() != null) {
            fundInfo.setPurchaseStatus(request.getPurchaseStatus());
        }
        if (request.getRedeemStatus() != null) {
            fundInfo.setRedeemStatus(request.getRedeemStatus());
        }
        if (request.getStatus() != null) {
            fundInfo.setStatus(request.getStatus());
        }
        fundInfoMapper.updateById(fundInfo);

        log.info("修改基金状态 tenantId={}, fundCode={}, purchaseStatus={}, redeemStatus={}, status={}",
                tenantId, fundInfo.getFundCode(), fundInfo.getPurchaseStatus(), fundInfo.getRedeemStatus(), fundInfo.getStatus());
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FundInfoVO bindTags(Long id, FundTagBindRequest request) {
        Long tenantId = currentTenantId();
        getTenantFund(tenantId, id);
        syncTags(tenantId, id, request.getTagIds());
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        Long tenantId = currentTenantId();
        BizFundInfo fundInfo = getTenantFund(tenantId, id);
        fundTagRelationMapper.deleteByFundId(tenantId, id);
        fundInfoMapper.deleteById(id);
        log.info("删除基金信息 tenantId={}, fundCode={}", tenantId, fundInfo.getFundCode());
        return Boolean.TRUE;
    }

    private Long currentTenantId() {
        String tenantId = TenantContext.getTenantId();
        if (!StringUtils.hasText(tenantId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "租户ID不能为空");
        }
        try {
            return Long.valueOf(tenantId);
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "租户ID格式错误");
        }
    }

    private BizFundInfo getTenantFund(Long tenantId, Long id) {
        BizFundInfo fundInfo = fundInfoMapper.selectOne(new LambdaQueryWrapper<BizFundInfo>()
                .eq(BizFundInfo::getTenantId, tenantId)
                .eq(BizFundInfo::getId, id)
                .eq(BizFundInfo::getDelFlag, "0"));
        if (fundInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "基金不存在");
        }
        return fundInfo;
    }

    private void assertFundCodeUnique(Long tenantId, Long id, String fundCode) {
        Long count = fundInfoMapper.selectCount(new LambdaQueryWrapper<BizFundInfo>()
                .eq(BizFundInfo::getTenantId, tenantId)
                .eq(BizFundInfo::getFundCode, fundCode)
                .eq(BizFundInfo::getDelFlag, "0")
                .ne(id != null, BizFundInfo::getId, id));
        if (count > 0) {
            throw new BusinessException(ErrorCode.FUND_DUPLICATE, "基金代码已存在");
        }
    }

    private void copy(FundInfoRequest request, BizFundInfo fundInfo) {
        if (StringUtils.hasText(request.getFundCode())) {
            fundInfo.setFundCode(request.getFundCode());
        }
        if (StringUtils.hasText(request.getFundName())) {
            fundInfo.setFundName(request.getFundName());
        }
        fundInfo.setFundShortName(request.getFundShortName());
        if (StringUtils.hasText(request.getFundType())) {
            fundInfo.setFundType(request.getFundType());
        }
        fundInfo.setFundCompany(request.getFundCompany());
        fundInfo.setManagerName(request.getManagerName());
        fundInfo.setRiskLevel(request.getRiskLevel());
        fundInfo.setEstablishDate(request.getEstablishDate());
        if (StringUtils.hasText(request.getCurrency())) {
            fundInfo.setCurrency(request.getCurrency());
        }
        fundInfo.setTrackingIndex(request.getTrackingIndex());
        if (request.getPurchaseStatus() != null) {
            fundInfo.setPurchaseStatus(request.getPurchaseStatus());
        }
        if (request.getRedeemStatus() != null) {
            fundInfo.setRedeemStatus(request.getRedeemStatus());
        }
        if (request.getStatus() != null) {
            fundInfo.setStatus(request.getStatus());
        }
        fundInfo.setRemark(request.getRemark());
    }

    private void fillDefaults(BizFundInfo fundInfo) {
        if (!StringUtils.hasText(fundInfo.getCurrency())) {
            fundInfo.setCurrency(DEFAULT_CURRENCY);
        }
        if (fundInfo.getPurchaseStatus() == null) {
            fundInfo.setPurchaseStatus(STATUS_ENABLED);
        }
        if (fundInfo.getRedeemStatus() == null) {
            fundInfo.setRedeemStatus(STATUS_ENABLED);
        }
        if (fundInfo.getStatus() == null) {
            fundInfo.setStatus(STATUS_NORMAL);
        }
    }

    private List<Long> queryFundIdsByTags(Long tenantId, List<Long> tagIds) {
        List<Long> distinctTagIds = distinctIds(tagIds);
        if (distinctTagIds.isEmpty()) {
            return null;
        }
        List<BizFundTagRelation> relations = fundTagRelationMapper.selectList(new LambdaQueryWrapper<BizFundTagRelation>()
                .eq(BizFundTagRelation::getTenantId, tenantId)
                .in(BizFundTagRelation::getTagId, distinctTagIds)
                .eq(BizFundTagRelation::getDelFlag, "0"));
        return relations.stream()
                .map(BizFundTagRelation::getFundId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private void syncTags(Long tenantId, Long fundId, List<Long> tagIds) {
        List<Long> distinctTagIds = distinctIds(tagIds);
        validateTags(tenantId, distinctTagIds);

        fundTagRelationMapper.deleteByFundId(tenantId, fundId);
        for (Long tagId : distinctTagIds) {
            BizFundTagRelation relation = new BizFundTagRelation();
            relation.setTenantId(tenantId);
            relation.setFundId(fundId);
            relation.setTagId(tagId);
            fundTagRelationMapper.insert(relation);
        }
    }

    private void validateTags(Long tenantId, List<Long> tagIds) {
        if (tagIds.isEmpty()) {
            return;
        }
        List<BizFundTag> tags = fundTagMapper.selectList(new LambdaQueryWrapper<BizFundTag>()
                .eq(BizFundTag::getTenantId, tenantId)
                .eq(BizFundTag::getStatus, STATUS_ENABLED)
                .eq(BizFundTag::getDelFlag, "0")
                .in(BizFundTag::getId, tagIds));
        Set<Long> existsTagIds = tags.stream().map(BizFundTag::getId).collect(Collectors.toSet());
        if (!existsTagIds.containsAll(tagIds)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "标签不存在或已禁用");
        }
    }

    private List<Long> distinctIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<FundInfoVO> toVOList(Long tenantId, List<BizFundInfo> funds) {
        if (funds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> fundIds = funds.stream().map(BizFundInfo::getId).toList();
        Map<Long, List<FundTagVO>> tagMap = listTagMap(tenantId, fundIds);
        List<FundInfoVO> result = new ArrayList<>();
        for (BizFundInfo fund : funds) {
            result.add(toVO(tenantId, fund, tagMap.getOrDefault(fund.getId(), Collections.emptyList())));
        }
        return result;
    }

    private Map<Long, List<FundTagVO>> listTagMap(Long tenantId, List<Long> fundIds) {
        if (fundIds == null || fundIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<BizFundTagRelation> relations = fundTagRelationMapper.selectList(new LambdaQueryWrapper<BizFundTagRelation>()
                .eq(BizFundTagRelation::getTenantId, tenantId)
                .in(BizFundTagRelation::getFundId, fundIds)
                .eq(BizFundTagRelation::getDelFlag, "0"));
        if (relations.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> tagIds = relations.stream()
                .map(BizFundTagRelation::getTagId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, FundTagVO> tagVOMap = fundTagMapper.selectList(new LambdaQueryWrapper<BizFundTag>()
                        .eq(BizFundTag::getTenantId, tenantId)
                        .in(BizFundTag::getId, tagIds)
                        .eq(BizFundTag::getDelFlag, "0"))
                .stream()
                .map(this::toTagVO)
                .collect(Collectors.toMap(FundTagVO::getId, tag -> tag));

        Map<Long, List<FundTagVO>> result = new HashMap<>();
        Set<Long> fundIdSet = new HashSet<>(fundIds);
        for (BizFundTagRelation relation : relations) {
            if (!fundIdSet.contains(relation.getFundId())) {
                continue;
            }
            FundTagVO tagVO = tagVOMap.get(relation.getTagId());
            if (tagVO != null) {
                result.computeIfAbsent(relation.getFundId(), key -> new ArrayList<>()).add(tagVO);
            }
        }
        result.values().forEach(tags -> tags.sort(Comparator
                .comparing(FundTagVO::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(FundTagVO::getId)));
        return result;
    }

    private FundInfoVO toVO(Long tenantId, BizFundInfo fundInfo, List<FundTagVO> tags) {
        FundInfoVO vo = new FundInfoVO();
        vo.setId(fundInfo.getId());
        vo.setFundCode(fundInfo.getFundCode());
        vo.setFundName(fundInfo.getFundName());
        vo.setFundShortName(fundInfo.getFundShortName());
        vo.setFundType(fundInfo.getFundType());
        vo.setFundCompany(fundInfo.getFundCompany());
        vo.setManagerName(fundInfo.getManagerName());
        vo.setRiskLevel(fundInfo.getRiskLevel());
        vo.setEstablishDate(fundInfo.getEstablishDate());
        vo.setCurrency(fundInfo.getCurrency());
        vo.setTrackingIndex(fundInfo.getTrackingIndex());
        vo.setPurchaseStatus(fundInfo.getPurchaseStatus());
        vo.setRedeemStatus(fundInfo.getRedeemStatus());
        vo.setStatus(fundInfo.getStatus());
        vo.setRemark(fundInfo.getRemark());
        vo.setTags(tags);
        vo.setCreateTime(fundInfo.getCreateTime());
        vo.setUpdateTime(fundInfo.getUpdateTime());
        return vo;
    }

    private FundTagVO toTagVO(BizFundTag tag) {
        FundTagVO vo = new FundTagVO();
        vo.setId(tag.getId());
        vo.setTagName(tag.getTagName());
        vo.setTagType(tag.getTagType());
        vo.setTagColor(tag.getTagColor());
        vo.setSortOrder(tag.getSortOrder());
        vo.setStatus(tag.getStatus());
        return vo;
    }
}
