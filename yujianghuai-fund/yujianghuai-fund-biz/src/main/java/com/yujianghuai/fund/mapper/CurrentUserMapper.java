package com.yujianghuai.fund.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CurrentUserMapper {

    @Select("""
            select id
            from sys_user
            where tenant_id = #{tenantId}
              and username = #{username}
              and status = 1
              and del_flag = '0'
            limit 1
            """)
    Long selectUserIdByUsername(@Param("tenantId") Long tenantId, @Param("username") String username);
}
