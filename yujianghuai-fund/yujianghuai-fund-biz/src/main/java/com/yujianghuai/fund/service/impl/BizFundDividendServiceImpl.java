package com.yujianghuai.fund.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yujianghuai.fund.entity.BizFundDividend;
import com.yujianghuai.fund.mapper.BizFundDividendMapper;
import com.yujianghuai.fund.service.BizFundDividendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 基金分红记录表
 *
 * @author yujianghuai
 * @date 2026-05-07 10:51:48
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizFundDividendServiceImpl extends ServiceImpl<BizFundDividendMapper, BizFundDividend> implements BizFundDividendService {
}
