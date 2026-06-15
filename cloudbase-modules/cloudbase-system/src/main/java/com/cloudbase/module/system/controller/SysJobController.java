package com.cloudbase.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.domain.TableDataInfo;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.entity.SysJob;
import com.cloudbase.module.system.entity.SysJobLog;
import com.cloudbase.module.system.mapper.SysJobLogMapper;
import com.cloudbase.module.system.model.dto.IdDTO;
import com.cloudbase.module.system.quartz.ScheduleManager;
import com.cloudbase.module.system.service.ISysJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 定时任务管理（重构后使用DTO）
 */
@Validated
@RestController
@RequestMapping("/sys/job")
@RequiredArgsConstructor
public class SysJobController {

    private final ISysJobService sysJobService;
    private final SysJobLogMapper sysJobLogMapper;
    private final ScheduleManager scheduleManager;

    /**
     * 查询定时任务列表
     */
    @Log(title = "定时任务管理", businessType = BusinessType.QUERY)
    @PostMapping("/page")
    public TableDataInfo page(@RequestBody Map<String, Object> params) {
        int pageNo = params.containsKey("pageNo") ? Integer.parseInt(params.get("pageNo").toString()) : 1;
        int pageSize = params.containsKey("pageSize") ? Integer.parseInt(params.get("pageSize").toString()) : 20;
        pageNo = Math.max(pageNo, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 200);

        Page<SysJob> page = sysJobService.page(
                new Page<>(pageNo, pageSize),
                new LambdaQueryWrapper<SysJob>()
                        .like(params.containsKey("jobName") && params.get("jobName") != null
                                && !params.get("jobName").toString().isEmpty(),
                                SysJob::getJobName, params.get("jobName"))
                        .orderByDesc(SysJob::getCreateTime)
        );
        return TableDataInfo.build(page.getRecords(), page.getTotal());
    }

    /**
     * 新增定时任务
     */
    @Log(title = "定时任务管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult add(@RequestBody SysJob job) {
        sysJobService.save(job);
        // 状态为正常则立即启动
        if (job.getStatus() != null && job.getStatus() == 1) {
            scheduleManager.startJob(job);
        }
        return AjaxResult.success();
    }

    /**
     * 编辑定时任务
     */
    @Log(title = "定时任务管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody SysJob job) {
        sysJobService.updateById(job);
        // 先停止旧任务，再根据状态决定是否重启
        scheduleManager.stopJob(job.getJobId());
        if (job.getStatus() != null && job.getStatus() == 1) {
            scheduleManager.startJob(job);
        }
        return AjaxResult.success();
    }

    /**
     * 删除定时任务
     */
    @Log(title = "定时任务管理", businessType = BusinessType.DELETE)
    @PostMapping("/delete")
    public AjaxResult delete(@Valid @RequestBody IdDTO dto) {
        scheduleManager.stopJob(dto.getId());
        sysJobService.removeById(dto.getId());
        return AjaxResult.success();
    }

    /**
     * 执行定时任务
     */
    @Log(title = "定时任务管理", businessType = BusinessType.OTHER)
    @PostMapping("/run")
    public AjaxResult run(@Valid @RequestBody IdDTO dto) {
        sysJobService.run(dto.getId());
        return AjaxResult.success();
    }

    /**
     * 暂停定时任务
     */
    @Log(title = "定时任务管理", businessType = BusinessType.UPDATE)
    @PostMapping("/pause")
    public AjaxResult pause(@Valid @RequestBody IdDTO dto) {
        sysJobService.pause(dto.getId());
        return AjaxResult.success();
    }

    /**
     * 恢复定时任务
     */
    @Log(title = "定时任务管理", businessType = BusinessType.UPDATE)
    @PostMapping("/resume")
    public AjaxResult resume(@Valid @RequestBody IdDTO dto) {
        sysJobService.resume(dto.getId());
        return AjaxResult.success();
    }

    /**
     * 查询任务执行日志
     */
    @Log(title = "定时任务管理", businessType = BusinessType.QUERY)
    @PostMapping("/log/page")
    public TableDataInfo logPage(@RequestBody Map<String, Object> params) {
        int pageNo = params.containsKey("pageNo") ? Integer.parseInt(params.get("pageNo").toString()) : 1;
        int pageSize = params.containsKey("pageSize") ? Integer.parseInt(params.get("pageSize").toString()) : 20;
        pageNo = Math.max(pageNo, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 200);

        Page<SysJobLog> page = sysJobLogMapper.selectPage(
                new Page<>(pageNo, pageSize),
                new LambdaQueryWrapper<SysJobLog>().orderByDesc(SysJobLog::getStartTime)
        );
        return TableDataInfo.build(page.getRecords(), page.getTotal());
    }

    /**
     * 清空任务执行日志
     */
    @Log(title = "定时任务管理", businessType = BusinessType.DELETE)
    @PostMapping("/log/clear")
    public AjaxResult logClear() {
        sysJobLogMapper.delete(new LambdaQueryWrapper<>());
        return AjaxResult.success();
    }
}
