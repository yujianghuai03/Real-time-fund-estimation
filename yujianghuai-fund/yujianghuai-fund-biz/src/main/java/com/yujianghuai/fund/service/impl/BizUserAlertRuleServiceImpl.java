package com.yujianghuai.fund.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yujianghuai.fund.entity.BizUserAlertRule;
import com.yujianghuai.fund.mapper.BizUserAlertRuleMapper;
import com.yujianghuai.fund.service.BizUserAlertRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户提醒规则表
 *
 * @author yujianghuai
 * @date 2026-05-07 10:51:48
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizUserAlertRuleServiceImpl extends ServiceImpl<BizUserAlertRuleMapper, BizUserAlertRule> implements BizUserAlertRuleService {
}
