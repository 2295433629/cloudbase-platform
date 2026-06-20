package com.cloudbase.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudbase.module.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 按日期统计新增用户数量（近N天）
     */
    @Select("SELECT DATE(create_time) AS date, COUNT(*) AS count " +
            "FROM sys_user " +
            "WHERE create_time >= #{startDate} " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date ASC")
    List<Map<String, Object>> countNewUsersByDateRange(@Param("startDate") String startDate);
}
