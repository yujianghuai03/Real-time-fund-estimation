package com.yujianghuai.fund.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yujianghuai.fund.entity.BizUserFundSipExecution;
import com.yujianghuai.fund.mapper.BizUserFundSipExecutionMapper;
import com.yujianghuai.fund.service.BizUserFundSipExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户基金定投执行记录表
 *
 * @author yujianghuai
 * @date 2026-05-07 10:51:48
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizUserFundSipExecutionServiceImpl extends ServiceImpl<BizUserFundSipExecutionMapper, BizUserFundSipExecution> implements BizUserFundSipExecutionService {
}
