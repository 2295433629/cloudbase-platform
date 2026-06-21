package com.cloudbase.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.common.web.cache.CacheService;
import com.cloudbase.module.system.entity.SysOperLog;
import com.cloudbase.module.system.mapper.SysDeptMapper;
import com.cloudbase.module.system.mapper.SysDictMapper;
import com.cloudbase.module.system.mapper.SysMenuMapper;
import com.cloudbase.module.system.mapper.SysNoticeMapper;
import com.cloudbase.module.system.mapper.SysOperLogMapper;
import com.cloudbase.module.system.mapper.SysPostMapper;
import com.cloudbase.module.system.mapper.SysRoleMapper;
import com.cloudbase.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
    private final SysPostMapper postMapper;
    private final SysNoticeMapper noticeMapper;
    private final SysOperLogMapper operLogMapper;
    private final CacheService cacheService;

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
        data.put("postCount", postMapper.selectCount(null));
        data.put("noticeCount", noticeMapper.selectCount(null));
        // 在线用户数（从缓存中统计 login_tokens:* 的 key 数量）
        var onlineKeys = cacheService.keys("login_tokens:*");
        data.put("onlineUserCount", onlineKeys != null ? onlineKeys.size() : 0);
        // 今日操作日志数
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Long todayCount = operLogMapper.selectCount(
                new LambdaQueryWrapper<SysOperLog>().ge(SysOperLog::getOperTime, todayStart));
        data.put("operLogTodayCount", todayCount);
        return AjaxResult.success(data);
    }

    /**
     * 近7日操作趋势（每日新增用户数 + 操作次数）
     */
    @Log(title = "仪表盘", businessType = BusinessType.QUERY)
    @PostMapping("/trend")
    public AjaxResult trend() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        String startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

        // 查询近7天每日操作数量
        List<Map<String, Object>> operCounts = operLogMapper.countByDateRange(startDateStr);
        // 查询近7天每日新增用户数量
        List<Map<String, Object>> userCounts = userMapper.countNewUsersByDateRange(startDateStr);

        // 构建日期→数量的映射
        Map<String, Long> operMap = new LinkedHashMap<>();
        for (Map<String, Object> row : operCounts) {
            operMap.put(row.get("date").toString(), ((Number) row.get("count")).longValue());
        }
        Map<String, Long> userMap = new LinkedHashMap<>();
        for (Map<String, Object> row : userCounts) {
            userMap.put(row.get("date").toString(), ((Number) row.get("count")).longValue());
        }

        // 按近7天日期顺序填充（无数据的日期补0）
        List<String> dates = new ArrayList<>();
        List<Long> newUserList = new ArrayList<>();
        List<Long> operList = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M/d");
        for (int i = 0; i < 7; i++) {
            LocalDate d = startDate.plusDays(i);
            String isoDate = d.format(DateTimeFormatter.ISO_LOCAL_DATE);
            dates.add(d.format(fmt));
            newUserList.add(userMap.getOrDefault(isoDate, 0L));
            operList.add(operMap.getOrDefault(isoDate, 0L));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("dates", dates);
        data.put("newUsers", newUserList);
        data.put("operations", operList);
        return AjaxResult.success(data);
    }

    /**
     * 操作类型分布（近30天）
     */
    @Log(title = "仪表盘", businessType = BusinessType.QUERY)
    @PostMapping("/typeDistribution")
    public AjaxResult typeDistribution() {
        LocalDate startDate = LocalDate.now().minusDays(30);
        String startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

        List<Map<String, Object>> rows = operLogMapper.countByOperType(startDateStr);

        // 操作类型名称映射
        Map<String, String> typeNameMap = Map.of(
                "INSERT", "新增",
                "UPDATE", "编辑",
                "DELETE", "删除",
                "QUERY", "查询",
                "EXPORT", "导出",
                "GRANT", "授权",
                "OTHER", "其他"
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String type = row.get("type") != null ? row.get("type").toString() : "OTHER";
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", typeNameMap.getOrDefault(type, type));
            item.put("value", ((Number) row.get("count")).longValue());
            result.add(item);
        }

        return AjaxResult.success(result);
    }
}
