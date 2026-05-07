package com.yujianghuai.fund.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yujianghuai.fund.entity.BizFundRealtimeEstimate;
import com.yujianghuai.fund.mapper.BizFundRealtimeEstimateMapper;
import com.yujianghuai.fund.service.BizFundRealtimeEstimateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 基金实时估值表
 *
 * @author yujianghuai
 * @date 2026-05-07 10:51:48
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizFundRealtimeEstimateServiceImpl extends ServiceImpl<BizFundRealtimeEstimateMapper, BizFundRealtimeEstimate> implements BizFundRealtimeEstimateService {
}
