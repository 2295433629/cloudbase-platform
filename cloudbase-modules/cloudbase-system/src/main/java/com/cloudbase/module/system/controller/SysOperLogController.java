package com.cloudbase.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.domain.TableDataInfo;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.entity.SysOperLog;
import com.cloudbase.module.system.mapper.SysOperLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 操作日志管理
 *
 * @author ruoyi
 */
@RestController
@RequiredArgsConstructor
public class SysOperLogController {

    private final SysOperLogMapper operLogMapper;

    /**
     * 查询操作日志
     */
    @Log(title = "操作日志管理", businessType = BusinessType.QUERY)
    @PostMapping("/sys/operLog/page")
    public TableDataInfo page(@RequestBody Map<String, Object> params) {
        int pageNo = params.containsKey("pageNo") ? Integer.parseInt(params.get("pageNo").toString()) : 1;
        int pageSize = params.containsKey("pageSize") ? Integer.parseInt(params.get("pageSize").toString()) : 20;
        pageNo = Math.max(pageNo, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 200);

        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        if (params.containsKey("module")) {
            wrapper.like(SysOperLog::getModule, params.get("module"));
        }
        if (params.containsKey("operUserName")) {
            wrapper.like(SysOperLog::getOperUserName, params.get("operUserName"));
        }
        wrapper.orderByDesc(SysOperLog::getOperTime);

        Page<SysOperLog> page = operLogMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return TableDataInfo.build(page.getRecords(), page.getTotal());
    }

    /**
     * 清空操作日志
     */
    @Log(title = "操作日志管理", businessType = BusinessType.DELETE)
    @PostMapping("/sys/operLog/clear")
    public AjaxResult clear() {
        operLogMapper.delete(new LambdaQueryWrapper<>());
        return AjaxResult.success();
    }
}