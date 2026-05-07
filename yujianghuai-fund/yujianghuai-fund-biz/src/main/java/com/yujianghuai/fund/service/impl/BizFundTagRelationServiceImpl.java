package com.yujianghuai.fund.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yujianghuai.fund.entity.BizFundTagRelation;
import com.yujianghuai.fund.mapper.BizFundTagRelationMapper;
import com.yujianghuai.fund.service.BizFundTagRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 基金标签关系表
 *
 * @author yujianghuai
 * @date 2026-05-07 10:51:48
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizFundTagRelationServiceImpl extends ServiceImpl<BizFundTagRelationMapper, BizFundTagRelation> implements BizFundTagRelationService {
}
