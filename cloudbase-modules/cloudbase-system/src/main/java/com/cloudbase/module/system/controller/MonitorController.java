package com.cloudbase.module.system.controller;

import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.service.OshiMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务器监控管理（保持不变）
 */
@Validated
@RestController
@RequestMapping("/sys/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final OshiMonitorService oshiMonitorService;

    /**
     * 获取服务器信息
     */
    @Log(title = "系统监控", businessType = BusinessType.QUERY)
    @PostMapping("/server")
    public AjaxResult getServerInfo() {
        return AjaxResult.success(oshiMonitorService.getServerInfo());
    }
}
