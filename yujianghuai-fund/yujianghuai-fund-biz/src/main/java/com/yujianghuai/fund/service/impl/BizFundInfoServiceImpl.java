package com.yujianghuai.fund.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yujianghuai.fund.entity.BizFundInfo;
import com.yujianghuai.fund.mapper.BizFundInfoMapper;
import com.yujianghuai.fund.service.BizFundInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 基金基础信息表
 *
 * @author yujianghuai
 * @date 2026-05-07 10:51:48
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizFundInfoServiceImpl extends ServiceImpl<BizFundInfoMapper, BizFundInfo> implements BizFundInfoService {
}
