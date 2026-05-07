package com.yujianghuai.fund.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yujianghuai.fund.entity.BizUserFundGroup;
import com.yujianghuai.fund.mapper.BizUserFundGroupMapper;
import com.yujianghuai.fund.service.BizUserFundGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户基金分组表
 *
 * @author yujianghuai
 * @date 2026-05-07 10:51:48
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizUserFundGroupServiceImpl extends ServiceImpl<BizUserFundGroupMapper, BizUserFundGroup> implements BizUserFundGroupService {
}
