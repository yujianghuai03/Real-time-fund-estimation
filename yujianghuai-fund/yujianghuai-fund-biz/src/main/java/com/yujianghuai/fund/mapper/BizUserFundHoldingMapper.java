package com.yujianghuai.fund.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yujianghuai.fund.entity.BizUserFundHolding;
import com.yujianghuai.fund.model.dto.FundHoldingSummaryRequest;
import com.yujianghuai.fund.model.vo.FundHoldingSummaryVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BizUserFundHoldingMapper extends BaseMapper<BizUserFundHolding> {

    List<FundHoldingSummaryVO> selectUserSummary(@Param("tenantId") Long tenantId,
                                                 @Param("userId") Long userId,
                                                 @Param("query") FundHoldingSummaryRequest query);

    List<FundHoldingSummaryVO> selectPortfolioSummary(@Param("tenantId") Long tenantId,
                                                      @Param("userId") Long userId,
                                                      @Param("query") FundHoldingSummaryRequest query);

    List<FundHoldingSummaryVO> selectGroupSummary(@Param("tenantId") Long tenantId,
                                                  @Param("userId") Long userId,
                                                  @Param("query") FundHoldingSummaryRequest query);
}
