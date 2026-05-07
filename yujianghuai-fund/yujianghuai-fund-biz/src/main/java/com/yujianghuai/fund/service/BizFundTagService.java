package com.yujianghuai.fund.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yujianghuai.fund.entity.BizFundTag;
import com.yujianghuai.fund.model.dto.FundTagQueryRequest;
import com.yujianghuai.fund.model.dto.FundTagRequest;
import com.yujianghuai.fund.model.vo.FundTagVO;
import java.util.List;

/**
 * 基金标签表。
 */
public interface BizFundTagService extends IService<BizFundTag> {

    List<FundTagVO> list(FundTagQueryRequest request);

    FundTagVO create(FundTagRequest request);

    FundTagVO update(Long id, FundTagRequest request);

    Boolean delete(Long id);
}
