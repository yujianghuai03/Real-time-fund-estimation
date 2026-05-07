package com.yujianghuai.fund.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yujianghuai.fund.entity.BizUserFundGroupRelation;
import com.yujianghuai.fund.mapper.BizUserFundGroupRelationMapper;
import com.yujianghuai.fund.service.BizUserFundGroupRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户基金分组关系表
 *
 * @author yujianghuai
 * @date 2026-05-07 10:51:48
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizUserFundGroupRelationServiceImpl extends ServiceImpl<BizUserFundGroupRelationMapper, BizUserFundGroupRelation> implements BizUserFundGroupRelationService {
}
