package com.yujianghuai.fund.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yujianghuai.common.web.PageResult;
import com.yujianghuai.fund.entity.BizUserFundHolding;
import com.yujianghuai.fund.model.dto.FundHoldingQueryRequest;
import com.yujianghuai.fund.model.dto.FundHoldingRequest;
import com.yujianghuai.fund.model.dto.FundHoldingSummaryRequest;
import com.yujianghuai.fund.model.vo.FundHoldingSummaryVO;
import com.yujianghuai.fund.model.vo.FundHoldingVO;
import java.util.List;

/**
 * 用户基金持仓服务。
 */
public interface BizUserFundHoldingService extends IService<BizUserFundHolding> {

    PageResult<FundHoldingVO> page(FundHoldingQueryRequest request);

    List<FundHoldingVO> list(FundHoldingQueryRequest request);

    FundHoldingVO detail(Long id);

    FundHoldingVO create(FundHoldingRequest request);

    FundHoldingVO update(Long id, FundHoldingRequest request);

    Boolean delete(Long id);

    List<FundHoldingSummaryVO> summary(FundHoldingSummaryRequest request);
}
