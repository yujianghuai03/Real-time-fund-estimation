package com.yujianghuai.fund.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yujianghuai.common.exception.BusinessException;
import com.yujianghuai.common.exception.ErrorCode;
import com.yujianghuai.common.tenant.TenantContext;
import com.yujianghuai.fund.entity.BizFundTag;
import com.yujianghuai.fund.mapper.BizFundTagMapper;
import com.yujianghuai.fund.mapper.BizFundTagRelationMapper;
import com.yujianghuai.fund.model.dto.FundTagQueryRequest;
import com.yujianghuai.fund.model.dto.FundTagRequest;
import com.yujianghuai.fund.model.vo.FundTagVO;
import com.yujianghuai.fund.service.BizFundTagService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 基金标签表。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizFundTagServiceImpl extends ServiceImpl<BizFundTagMapper, BizFundTag> implements BizFundTagService {

    private static final String DEFAULT_TAG_TYPE = "CUSTOM";
    private static final Integer STATUS_ENABLED = 1;
    private static final Integer DEFAULT_SORT_ORDER = 0;

    private final BizFundTagMapper fundTagMapper;
    private final BizFundTagRelationMapper fundTagRelationMapper;

    @Override
    public List<FundTagVO> list(FundTagQueryRequest request) {
        Long tenantId = currentTenantId();
        return fundTagMapper.selectList(new LambdaQueryWrapper<BizFundTag>()
                        .eq(BizFundTag::getTenantId, tenantId)
                        .eq(BizFundTag::getDelFlag, "0")
                        .like(StringUtils.hasText(request.getTagName()), BizFundTag::getTagName, request.getTagName())
                        .eq(StringUtils.hasText(request.getTagType()), BizFundTag::getTagType, request.getTagType())
                        .eq(request.getStatus() != null, BizFundTag::getStatus, request.getStatus())
                        .orderByAsc(BizFundTag::getSortOrder)
                        .orderByAsc(BizFundTag::getId))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FundTagVO create(FundTagRequest request) {
        Long tenantId = currentTenantId();
        assertTagNameUnique(tenantId, null, request.getTagName());

        BizFundTag tag = new BizFundTag();
        tag.setTenantId(tenantId);
        copy(request, tag);
        fillDefaults(tag);
        fundTagMapper.insert(tag);
        log.info("创建基金标签 tenantId={}, tagName={}", tenantId, tag.getTagName());
        return toVO(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FundTagVO update(Long id, FundTagRequest request) {
        Long tenantId = currentTenantId();
        BizFundTag tag = getTenantTag(tenantId, id);
        String newTagName = StringUtils.hasText(request.getTagName()) ? request.getTagName() : tag.getTagName();
        assertTagNameUnique(tenantId, id, newTagName);

        copy(request, tag);
        fillDefaults(tag);
        fundTagMapper.updateById(tag);
        log.info("修改基金标签 tenantId={}, tagName={}", tenantId, tag.getTagName());
        return toVO(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        Long tenantId = currentTenantId();
        BizFundTag tag = getTenantTag(tenantId, id);
        fundTagRelationMapper.deleteByTagId(tenantId, id);
        fundTagMapper.deleteById(id);
        log.info("删除基金标签 tenantId={}, tagName={}", tenantId, tag.getTagName());
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

    private BizFundTag getTenantTag(Long tenantId, Long id) {
        BizFundTag tag = fundTagMapper.selectOne(new LambdaQueryWrapper<BizFundTag>()
                .eq(BizFundTag::getTenantId, tenantId)
                .eq(BizFundTag::getId, id)
                .eq(BizFundTag::getDelFlag, "0"));
        if (tag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "标签不存在");
        }
        return tag;
    }

    private void assertTagNameUnique(Long tenantId, Long id, String tagName) {
        Long count = fundTagMapper.selectCount(new LambdaQueryWrapper<BizFundTag>()
                .eq(BizFundTag::getTenantId, tenantId)
                .eq(BizFundTag::getTagName, tagName)
                .eq(BizFundTag::getDelFlag, "0")
                .ne(id != null, BizFundTag::getId, id));
        if (count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "标签名称已存在");
        }
    }

    private void copy(FundTagRequest request, BizFundTag tag) {
        if (StringUtils.hasText(request.getTagName())) {
            tag.setTagName(request.getTagName());
        }
        if (StringUtils.hasText(request.getTagType())) {
            tag.setTagType(request.getTagType());
        }
        tag.setTagColor(request.getTagColor());
        if (request.getSortOrder() != null) {
            tag.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            tag.setStatus(request.getStatus());
        }
    }

    private void fillDefaults(BizFundTag tag) {
        if (!StringUtils.hasText(tag.getTagType())) {
            tag.setTagType(DEFAULT_TAG_TYPE);
        }
        if (tag.getSortOrder() == null) {
            tag.setSortOrder(DEFAULT_SORT_ORDER);
        }
        if (tag.getStatus() == null) {
            tag.setStatus(STATUS_ENABLED);
        }
    }

    private FundTagVO toVO(BizFundTag tag) {
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
