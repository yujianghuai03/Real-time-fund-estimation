package com.yujianghuai.fund.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yujianghuai.fund.entity.BizUserFundTransaction;
import com.yujianghuai.fund.mapper.BizUserFundTransactionMapper;
import com.yujianghuai.fund.service.BizUserFundTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户基金交易记录表
 *
 * @author yujianghuai
 * @date 2026-05-07 10:51:48
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizUserFundTransactionServiceImpl extends ServiceImpl<BizUserFundTransactionMapper, BizUserFundTransaction> implements BizUserFundTransactionService {
}
