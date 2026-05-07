package com.yujianghuai.fund.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yujianghuai.fund.entity.BizFundTagRelation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BizFundTagRelationMapper extends BaseMapper<BizFundTagRelation> {

    int deleteByFundId(@Param("tenantId") Long tenantId, @Param("fundId") Long fundId);

    int deleteByTagId(@Param("tenantId") Long tenantId, @Param("tagId") Long tagId);
}
