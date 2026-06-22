package com.cloudbase.module.system.controller;

import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.common.web.annotation.ApiResource;
import com.cloudbase.module.system.service.OshiMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 服务器监控管理
 */
@Slf4j
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

    /**
     * 接收前端错误日志上报（批量，免认证保证离线场景也能上报）
     */
    @ApiResource(requiredToken = false)
    @PostMapping("/errorLog")
    public AjaxResult receiveErrorLogs(@RequestBody List<Map<String, Object>> logs) {
        if (logs != null) {
            for (Map<String, Object> logEntry : logs) {
                log.error("[前端异常] source={}, message={}, url={}, stack={}",
                        logEntry.get("source"),
                        logEntry.get("message"),
                        logEntry.get("url"),
                        logEntry.get("stack"));
            }
        }
        return AjaxResult.success();
    }
}
