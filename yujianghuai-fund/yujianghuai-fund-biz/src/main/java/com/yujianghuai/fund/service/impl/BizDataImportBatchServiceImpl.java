package com.yujianghuai.fund.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yujianghuai.fund.entity.BizDataImportBatch;
import com.yujianghuai.fund.mapper.BizDataImportBatchMapper;
import com.yujianghuai.fund.service.BizDataImportBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 业务数据导入批次表
 *
 * @author yujianghuai
 * @date 2026-05-07 10:51:48
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizDataImportBatchServiceImpl extends ServiceImpl<BizDataImportBatchMapper, BizDataImportBatch> implements BizDataImportBatchService {
}
