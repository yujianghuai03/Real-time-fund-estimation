package com.yujianghuai.fund.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yujianghuai.fund.entity.BizUserFundGroupRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BizUserFundGroupRelationMapper extends BaseMapper<BizUserFundGroupRelation> {

    int deleteByHoldingId(@Param("tenantId") Long tenantId,
                          @Param("userId") Long userId,
                          @Param("holdingId") Long holdingId);
}
