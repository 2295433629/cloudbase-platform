package com.cloudbase.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudbase.module.system.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {

    /**
     * 按日期统计操作数量（近N天）
     */
    @Select("SELECT DATE(oper_time) AS date, COUNT(*) AS count " +
            "FROM sys_oper_log " +
            "WHERE oper_time >= #{startDate} " +
            "GROUP BY DATE(oper_time) " +
            "ORDER BY date ASC")
    List<Map<String, Object>> countByDateRange(@Param("startDate") String startDate);

    /**
     * 统计各操作类型数量（近N天）
     */
    @Select("SELECT oper_type AS type, COUNT(*) AS count " +
            "FROM sys_oper_log " +
            "WHERE oper_time >= #{startDate} " +
            "GROUP BY oper_type " +
            "ORDER BY count DESC")
    List<Map<String, Object>> countByOperType(@Param("startDate") String startDate);
}
