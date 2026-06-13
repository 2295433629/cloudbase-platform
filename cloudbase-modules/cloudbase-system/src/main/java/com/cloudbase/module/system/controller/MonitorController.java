package com.cloudbase.module.system.controller;

import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.service.OshiMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务器监控管理
 *
 * @author ruoyi
 */
@RestController
@RequiredArgsConstructor
public class MonitorController {

    private final OshiMonitorService oshiMonitorService;

    /**
     * 获取服务器信息
     */
    @Log(title = "系统监控", businessType = BusinessType.QUERY)
    @PostMapping("/sys/monitor/server")
    public AjaxResult getServerInfo() {
        return AjaxResult.success(oshiMonitorService.getServerInfo());
    }
}
