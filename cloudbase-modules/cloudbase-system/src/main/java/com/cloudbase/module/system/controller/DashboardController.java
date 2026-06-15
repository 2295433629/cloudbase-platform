package com.cloudbase.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.entity.SysOperLog;
import com.cloudbase.module.system.mapper.SysDeptMapper;
import com.cloudbase.module.system.mapper.SysDictMapper;
import com.cloudbase.module.system.mapper.SysMenuMapper;
import com.cloudbase.module.system.mapper.SysNoticeMapper;
import com.cloudbase.module.system.mapper.SysOperLogMapper;
import com.cloudbase.module.system.mapper.SysRoleMapper;
import com.cloudbase.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 仪表盘统计
 */
@Validated
@RestController
@RequestMapping("/sys/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysDictMapper dictMapper;
    private final SysDeptMapper deptMapper;
    private final SysNoticeMapper noticeMapper;
    private final SysOperLogMapper operLogMapper;

    /**
     * 获取仪表盘统计数据
     */
    @Log(title = "仪表盘", businessType = BusinessType.QUERY)
    @PostMapping("/stats")
    public AjaxResult stats() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userCount", userMapper.selectCount(null));
        data.put("roleCount", roleMapper.selectCount(null));
        data.put("menuCount", menuMapper.selectCount(null));
        data.put("dictCount", dictMapper.selectCount(null));
        data.put("deptCount", deptMapper.selectCount(null));
        data.put("noticeCount", noticeMapper.selectCount(null));
        // 在线用户数暂用0占位（需结合在线用户管理模块）
        data.put("onlineUserCount", 0);
        // 今日操作日志数
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Long todayCount = operLogMapper.selectCount(
                new LambdaQueryWrapper<SysOperLog>().ge(SysOperLog::getOperTime, todayStart));
        data.put("operLogTodayCount", todayCount);
        return AjaxResult.success(data);
    }
}
