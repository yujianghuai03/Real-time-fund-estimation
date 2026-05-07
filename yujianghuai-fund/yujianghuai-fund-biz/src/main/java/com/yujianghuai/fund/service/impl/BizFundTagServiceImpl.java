package com.yujianghuai.fund.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yujianghuai.fund.entity.BizFundTag;
import com.yujianghuai.fund.mapper.BizFundTagMapper;
import com.yujianghuai.fund.service.BizFundTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 基金标签表
 *
 * @author yujianghuai
 * @date 2026-05-07 10:51:48
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizFundTagServiceImpl extends ServiceImpl<BizFundTagMapper, BizFundTag> implements BizFundTagService {
}
