package com.cloudbase.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.domain.PageQuery;
import com.cloudbase.common.core.domain.TableDataInfo;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.entity.SysLoginLog;
import com.cloudbase.module.system.mapper.SysLoginLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * 登录日志管理（重构后使用DTO）
 */
@Validated
@RestController
@RequestMapping("/sys/loginLog")
@RequiredArgsConstructor
public class SysLoginLogController {

    private final SysLoginLogMapper loginLogMapper;

    /**
     * 查询登录日志
     */
    @Log(title = "登录日志管理", businessType = BusinessType.QUERY)
    @PostMapping("/page")
    public TableDataInfo page(@RequestBody Map<String, Object> params) {
        var pageInfo = PageQuery.of(params);

        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        if (params.containsKey("userName")) {
            wrapper.like(SysLoginLog::getUserName, params.get("userName"));
        }
        if (params.containsKey("status") && params.get("status") != null) {
            wrapper.eq(SysLoginLog::getStatus, Integer.parseInt(params.get("status").toString()));
        }
        // 时间范围过滤
        if (params.containsKey("beginTime") && params.get("beginTime") != null
                && !params.get("beginTime").toString().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                wrapper.ge(SysLoginLog::getLoginTime, sdf.parse(params.get("beginTime").toString()));
            } catch (Exception ignored) {}
        }
        if (params.containsKey("endTime") && params.get("endTime") != null
                && !params.get("endTime").toString().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                wrapper.le(SysLoginLog::getLoginTime, sdf.parse(params.get("endTime").toString()));
            } catch (Exception ignored) {}
        }
        wrapper.orderByDesc(SysLoginLog::getLoginTime);

        Page<SysLoginLog> page = loginLogMapper.selectPage(new Page<>(pageInfo.pageNo(), pageInfo.pageSize()), wrapper);
        return TableDataInfo.build(page.getRecords(), page.getTotal());
    }

    /**
     * 清空登录日志
     */
    @Log(title = "登录日志管理", businessType = BusinessType.DELETE)
    @PostMapping("/clear")
    public AjaxResult clear() {
        loginLogMapper.delete(new LambdaQueryWrapper<>());
        return AjaxResult.success();
    }
}
