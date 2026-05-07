package com.yujianghuai.fund.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yujianghuai.fund.entity.BizUserFundHolding;
import com.yujianghuai.fund.mapper.BizUserFundHoldingMapper;
import com.yujianghuai.fund.service.BizUserFundHoldingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户基金持仓表
 *
 * @author yujianghuai
 * @date 2026-05-07 10:51:48
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizUserFundHoldingServiceImpl extends ServiceImpl<BizUserFundHoldingMapper, BizUserFundHolding> implements BizUserFundHoldingService {
}
