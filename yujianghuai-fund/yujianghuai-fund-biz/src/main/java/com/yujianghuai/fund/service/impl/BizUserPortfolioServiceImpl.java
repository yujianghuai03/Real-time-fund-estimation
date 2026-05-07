package com.yujianghuai.fund.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yujianghuai.fund.entity.BizUserPortfolio;
import com.yujianghuai.fund.mapper.BizUserPortfolioMapper;
import com.yujianghuai.fund.service.BizUserPortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户投资组合表
 *
 * @author yujianghuai
 * @date 2026-05-07 10:51:48
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizUserPortfolioServiceImpl extends ServiceImpl<BizUserPortfolioMapper, BizUserPortfolio> implements BizUserPortfolioService {
}
