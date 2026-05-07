package com.yujianghuai.fund.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yujianghuai.common.web.PageResult;
import com.yujianghuai.fund.entity.BizFundInfo;
import com.yujianghuai.fund.model.dto.FundInfoQueryRequest;
import com.yujianghuai.fund.model.dto.FundInfoRequest;
import com.yujianghuai.fund.model.dto.FundStatusRequest;
import com.yujianghuai.fund.model.dto.FundTagBindRequest;
import com.yujianghuai.fund.model.vo.FundInfoVO;

/**
 * 基金基础信息表。
 */
public interface BizFundInfoService extends IService<BizFundInfo> {

    PageResult<FundInfoVO> page(FundInfoQueryRequest request);

    FundInfoVO detail(Long id);

    FundInfoVO create(FundInfoRequest request);

    FundInfoVO update(Long id, FundInfoRequest request);

    FundInfoVO updateStatus(Long id, FundStatusRequest request);

    FundInfoVO bindTags(Long id, FundTagBindRequest request);

    Boolean delete(Long id);
}
