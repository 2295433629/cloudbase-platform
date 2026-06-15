package com.cloudbase.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudbase.module.system.entity.SysMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysMessageMapper extends BaseMapper<SysMessage> {

    /**
     * 查询用户消息分页列表（关联已读状态）
     */
    @Select("<script>" +
            "SELECT m.*, " +
            "CASE WHEN mr.read_time IS NOT NULL THEN 1 ELSE 0 END AS is_read, " +
            "mr.read_time AS read_time " +
            "FROM sys_message m " +
            "LEFT JOIN sys_message_read mr ON m.id = mr.message_id AND mr.user_id = #{userId} " +
            "WHERE m.status = 1 " +
            "<if test='type != null and type != \"\"'> AND m.msg_type = #{type} </if>" +
            "ORDER BY m.create_time DESC" +
            "</script>")
    List<Map<String, Object>> selectMessagePage(@Param("userId") Long userId, @Param("type") String type);

    /**
     * 标记消息已读
     */
    @Insert("INSERT IGNORE INTO sys_message_read (message_id, user_id, read_time) VALUES (#{messageId}, #{userId}, NOW())")
    int markRead(@Param("messageId") Long messageId, @Param("userId") Long userId);

    /**
     * 全部标为已读
     */
    @Insert("<script>" +
            "INSERT IGNORE INTO sys_message_read (message_id, user_id, read_time) " +
            "SELECT m.id, #{userId}, NOW() FROM sys_message m " +
            "WHERE m.status = 1 AND m.id NOT IN " +
            "(SELECT mr.message_id FROM sys_message_read mr WHERE mr.user_id = #{userId})" +
            "</script>")
    int markAllRead(@Param("userId") Long userId);

    /**
     * 查询未读数量
     */
    @Select("SELECT COUNT(*) FROM sys_message m " +
            "WHERE m.status = 1 AND m.id NOT IN " +
            "(SELECT mr.message_id FROM sys_message_read mr WHERE mr.user_id = #{userId})")
    int countUnread(@Param("userId") Long userId);
}
