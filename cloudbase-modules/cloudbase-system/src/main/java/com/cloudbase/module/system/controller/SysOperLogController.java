package com.cloudbase.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.domain.PageQuery;
import com.cloudbase.common.core.domain.TableDataInfo;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.entity.SysOperLog;
import com.cloudbase.module.system.mapper.SysOperLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 操作日志管理（重构后使用DTO）
 */
@Validated
@RestController
@RequestMapping("/sys/operLog")
@RequiredArgsConstructor
public class SysOperLogController {

    private final SysOperLogMapper operLogMapper;

    /**
     * 查询操作日志
     */
    @Log(title = "操作日志管理", businessType = BusinessType.QUERY)
    @PostMapping("/page")
    public TableDataInfo page(@RequestBody Map<String, Object> params) {
        var pageInfo = PageQuery.of(params);

        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        if (params.containsKey("module")) {
            wrapper.like(SysOperLog::getModule, params.get("module"));
        }
        if (params.containsKey("operUserName")) {
            wrapper.like(SysOperLog::getOperUserName, params.get("operUserName"));
        }
        if (params.containsKey("operType") && params.get("operType") != null
                && !params.get("operType").toString().isEmpty()) {
            wrapper.eq(SysOperLog::getOperType, params.get("operType"));
        }
        if (params.containsKey("status") && params.get("status") != null) {
            wrapper.eq(SysOperLog::getSuccess, Integer.parseInt(params.get("status").toString()));
        }
        // 时间范围过滤
        if (params.containsKey("beginTime") && params.get("beginTime") != null
                && !params.get("beginTime").toString().isEmpty()) {
            wrapper.ge(SysOperLog::getOperTime,
                    LocalDateTime.parse(params.get("beginTime").toString(),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (params.containsKey("endTime") && params.get("endTime") != null
                && !params.get("endTime").toString().isEmpty()) {
            wrapper.le(SysOperLog::getOperTime,
                    LocalDateTime.parse(params.get("endTime").toString(),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        wrapper.orderByDesc(SysOperLog::getOperTime);

        Page<SysOperLog> page = operLogMapper.selectPage(new Page<>(pageInfo.pageNo(), pageInfo.pageSize()), wrapper);
        return TableDataInfo.build(page.getRecords(), page.getTotal());
    }

    /**
     * 查询最近操作日志（仪表盘用）
     */
    @Log(title = "操作日志管理", businessType = BusinessType.QUERY)
    @PostMapping("/recent")
    public TableDataInfo recent(@RequestBody Map<String, Object> params) {
        var pageInfo = PageQuery.of(params, 5);

        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        if (params.containsKey("module")) {
            wrapper.like(SysOperLog::getModule, params.get("module"));
        }
        wrapper.orderByDesc(SysOperLog::getOperTime);

        Page<SysOperLog> page = operLogMapper.selectPage(new Page<>(pageInfo.pageNo(), pageInfo.pageSize()), wrapper);
        return TableDataInfo.build(page.getRecords(), page.getTotal());
    }

    /**
     * 清空操作日志
     */
    @Log(title = "操作日志管理", businessType = BusinessType.DELETE)
    @PostMapping("/clear")
    public AjaxResult clear() {
        operLogMapper.delete(new LambdaQueryWrapper<>());
        return AjaxResult.success();
    }
}
