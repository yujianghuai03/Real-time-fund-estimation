package com.yujianghuai.fund.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yujianghuai.fund.entity.BizUserFundSipPlan;
import com.yujianghuai.fund.mapper.BizUserFundSipPlanMapper;
import com.yujianghuai.fund.service.BizUserFundSipPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户基金定投计划表
 *
 * @author yujianghuai
 * @date 2026-05-07 10:51:48
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizUserFundSipPlanServiceImpl extends ServiceImpl<BizUserFundSipPlanMapper, BizUserFundSipPlan> implements BizUserFundSipPlanService {
}
