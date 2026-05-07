package com.yujianghuai.fund.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yujianghuai.fund.entity.BizUserFundDailySnapshot;
import com.yujianghuai.fund.mapper.BizUserFundDailySnapshotMapper;
import com.yujianghuai.fund.service.BizUserFundDailySnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户基金每日收益快照表
 *
 * @author yujianghuai
 * @date 2026-05-07 10:51:48
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizUserFundDailySnapshotServiceImpl extends ServiceImpl<BizUserFundDailySnapshotMapper, BizUserFundDailySnapshot> implements BizUserFundDailySnapshotService {
}
