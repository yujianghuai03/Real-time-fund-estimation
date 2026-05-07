package com.yujianghuai.fund.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yujianghuai.fund.entity.BizUserAlertLog;
import com.yujianghuai.fund.mapper.BizUserAlertLogMapper;
import com.yujianghuai.fund.service.BizUserAlertLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户提醒通知日志表
 *
 * @author yujianghuai
 * @date 2026-05-07 10:51:48
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizUserAlertLogServiceImpl extends ServiceImpl<BizUserAlertLogMapper, BizUserAlertLog> implements BizUserAlertLogService {
}
