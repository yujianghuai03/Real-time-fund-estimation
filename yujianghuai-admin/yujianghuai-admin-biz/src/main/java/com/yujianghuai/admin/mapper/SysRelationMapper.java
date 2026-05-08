package com.yujianghuai.admin.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysRelationMapper {

    List<Long> selectRoleIdsByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    List<String> selectRoleCodesByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    List<String> selectPermissionsByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    int deleteUserRoles(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    int insertUserRole(@Param("tenantId") Long tenantId, @Param("userId") Long userId, @Param("roleId") Long roleId);

    List<Long> selectMenuIdsByRoleId(@Param("tenantId") Long tenantId, @Param("roleId") Long roleId);

    int deleteRoleMenus(@Param("tenantId") Long tenantId, @Param("roleId") Long roleId);

    int insertRoleMenu(@Param("tenantId") Long tenantId, @Param("roleId") Long roleId, @Param("menuId") Long menuId);

    Long countLoginMenusByUserId(@Param("tenantId") Long tenantId,
                                 @Param("userId") Long userId,
                                 @Param("menuScope") String menuScope);
}
