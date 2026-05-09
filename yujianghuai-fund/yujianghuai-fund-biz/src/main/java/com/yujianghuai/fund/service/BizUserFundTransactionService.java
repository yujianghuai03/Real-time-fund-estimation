package com.yujianghuai.fund.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yujianghuai.common.web.PageResult;
import com.yujianghuai.fund.entity.BizUserFundTransaction;
import com.yujianghuai.fund.model.dto.TransactionConfirmRequest;
import com.yujianghuai.fund.model.dto.TransactionRecordQueryRequest;
import com.yujianghuai.fund.model.dto.TransactionRecordRequest;
import com.yujianghuai.fund.model.vo.TransactionRecordVO;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用户基金交易记录表
 *
 * @author yujianghuai
 * @date 2026-05-07 10:51:48
 */
public interface BizUserFundTransactionService extends IService<BizUserFundTransaction> {

    PageResult<TransactionRecordVO> page(TransactionRecordQueryRequest request);

    TransactionRecordVO detail(Long id);

    TransactionRecordVO create(TransactionRecordRequest request);

    TransactionRecordVO update(Long id, TransactionRecordRequest request);

    Boolean delete(Long id);

    TransactionRecordVO confirm(Long id, TransactionConfirmRequest request);

    TransactionRecordVO cancel(Long id);

    void export(TransactionRecordQueryRequest request, HttpServletResponse response);
}
