package com.yujianghuai.fund.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yujianghuai.fund.entity.BizDataSyncLog;
import com.yujianghuai.fund.mapper.BizDataSyncLogMapper;
import com.yujianghuai.fund.service.BizDataSyncLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 业务数据同步日志表
 *
 * @author yujianghuai
 * @date 2026-05-07 10:51:48
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizDataSyncLogServiceImpl extends ServiceImpl<BizDataSyncLogMapper, BizDataSyncLog> implements BizDataSyncLogService {
}
