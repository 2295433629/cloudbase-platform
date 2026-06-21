package com.cloudbase.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudbase.module.system.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    /**
     * 查询角色已分配的菜单ID列表
     */
    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}")
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询用户所有角色关联的菜单ID（去重）
     */
    @Select("SELECT DISTINCT rm.menu_id FROM sys_user_role ur " +
            "INNER JOIN sys_role_menu rm ON ur.role_id = rm.role_id " +
            "INNER JOIN sys_role r ON ur.role_id = r.role_id AND r.status = 1 " +
            "WHERE ur.user_id = #{userId}")
    List<Long> selectMenuIdsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户所有角色的权限标识（去重，排除空值）
     */
    @Select("SELECT DISTINCT m.perms FROM sys_user_role ur " +
            "INNER JOIN sys_role_menu rm ON ur.role_id = rm.role_id " +
            "INNER JOIN sys_menu m ON rm.menu_id = m.menu_id AND m.status = 1 " +
            "INNER JOIN sys_role r ON ur.role_id = r.role_id AND r.status = 1 " +
            "WHERE ur.user_id = #{userId} AND m.perms IS NOT NULL AND m.perms != ''")
    List<String> selectPermsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的角色编码列表
     */
    @Select("SELECT DISTINCT r.role_code FROM sys_user_role ur " +
            "INNER JOIN sys_role r ON ur.role_id = r.role_id AND r.status = 1 " +
            "WHERE ur.user_id = #{userId}")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 删除角色的所有菜单关联
     */
    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);
}
