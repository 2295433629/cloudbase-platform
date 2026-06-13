package com.cloudbase.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.domain.TableDataInfo;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.entity.SysLoginLog;
import com.cloudbase.module.system.mapper.SysLoginLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 登录日志管理
 *
 * @author ruoyi
 */
@RestController
@RequiredArgsConstructor
public class SysLoginLogController {

    private final SysLoginLogMapper loginLogMapper;

    /**
     * 查询登录日志
     */
    @Log(title = "登录日志管理", businessType = BusinessType.QUERY)
    @PostMapping("/sys/loginLog/page")
    public TableDataInfo page(@RequestBody Map<String, Object> params) {
        int pageNo = params.containsKey("pageNo") ? Integer.parseInt(params.get("pageNo").toString()) : 1;
        int pageSize = params.containsKey("pageSize") ? Integer.parseInt(params.get("pageSize").toString()) : 20;
        pageNo = Math.max(pageNo, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 200);

        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        if (params.containsKey("userName")) {
            wrapper.like(SysLoginLog::getUserName, params.get("userName"));
        }
        wrapper.orderByDesc(SysLoginLog::getLoginTime);

        Page<SysLoginLog> page = loginLogMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return TableDataInfo.build(page.getRecords(), page.getTotal());
    }

    /**
     * 清空登录日志
     */
    @Log(title = "登录日志管理", businessType = BusinessType.DELETE)
    @PostMapping("/sys/loginLog/clear")
    public AjaxResult clear() {
        loginLogMapper.delete(new LambdaQueryWrapper<>());
        return AjaxResult.success();
    }
}