package com.yujianghuai.admin.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysRelationMapper {

    @Select("select role_id from sys_user_role where tenant_id = #{tenantId} and user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    @Select("select r.role_code from sys_user_role ur inner join sys_role r on r.id = ur.role_id where ur.tenant_id = #{tenantId} and ur.user_id = #{userId}")
    List<String> selectRoleCodesByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    @Select("""
            select distinct m.permission
            from sys_user_role ur
            inner join sys_role r on r.id = ur.role_id and r.tenant_id = ur.tenant_id
            inner join sys_role_menu rm on rm.role_id = ur.role_id and rm.tenant_id = ur.tenant_id
            inner join sys_menu m on m.id = rm.menu_id and m.tenant_id = rm.tenant_id
            where ur.tenant_id = #{tenantId}
              and ur.user_id = #{userId}
              and r.del_flag = '0'
              and r.status = 1
              and m.del_flag = '0'
              and m.status = 1
              and m.permission is not null
              and m.permission <> ''
            """)
    List<String> selectPermissionsByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    @Delete("delete from sys_user_role where tenant_id = #{tenantId} and user_id = #{userId}")
    int deleteUserRoles(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    @Insert("insert ignore into sys_user_role(user_id, role_id, tenant_id) values(#{userId}, #{roleId}, #{tenantId})")
    int insertUserRole(@Param("tenantId") Long tenantId, @Param("userId") Long userId, @Param("roleId") Long roleId);

    @Select("select menu_id from sys_role_menu where tenant_id = #{tenantId} and role_id = #{roleId}")
    List<Long> selectMenuIdsByRoleId(@Param("tenantId") Long tenantId, @Param("roleId") Long roleId);

    @Delete("delete from sys_role_menu where tenant_id = #{tenantId} and role_id = #{roleId}")
    int deleteRoleMenus(@Param("tenantId") Long tenantId, @Param("roleId") Long roleId);

    @Insert("insert ignore into sys_role_menu(role_id, menu_id, tenant_id) values(#{roleId}, #{menuId}, #{tenantId})")
    int insertRoleMenu(@Param("tenantId") Long tenantId, @Param("roleId") Long roleId, @Param("menuId") Long menuId);

    @Select("""
            select count(distinct m.id)
            from sys_user_role ur
            inner join sys_role_menu rm on rm.role_id = ur.role_id and rm.tenant_id = ur.tenant_id
            inner join sys_menu m on m.id = rm.menu_id and m.tenant_id = rm.tenant_id
            where ur.tenant_id = #{tenantId}
              and ur.user_id = #{userId}
              and m.del_flag = '0'
              and m.status = 1
              and m.menu_type in ('CATALOG', 'MENU')
              and m.menu_scope = #{menuScope}
            """)
    Long countLoginMenusByUserId(@Param("tenantId") Long tenantId,
                                 @Param("userId") Long userId,
                                 @Param("menuScope") String menuScope);
}
