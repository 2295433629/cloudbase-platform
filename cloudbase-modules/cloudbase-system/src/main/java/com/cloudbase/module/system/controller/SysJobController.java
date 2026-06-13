package com.cloudbase.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.domain.PageDomain;
import com.cloudbase.common.core.domain.TableDataInfo;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.entity.SysJob;
import com.cloudbase.module.system.entity.SysJobLog;
import com.cloudbase.module.system.mapper.SysJobLogMapper;
import com.cloudbase.module.system.service.ISysJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 定时任务管理
 *
 * @author ruoyi
 */
@RestController
@RequiredArgsConstructor
public class SysJobController {

    private final ISysJobService sysJobService;
    private final SysJobLogMapper sysJobLogMapper;

    /**
     * 查询定时任务列表
     */
    @Log(title = "定时任务管理", businessType = BusinessType.QUERY)
    @PostMapping("/sys/job/page")
    public TableDataInfo page(@RequestBody PageDomain pageDomain) {
        Page<SysJob> page = sysJobService.page(
                new Page<>(pageDomain.getPageNum(), pageDomain.getPageSize()),
                new LambdaQueryWrapper<SysJob>().orderByDesc(SysJob::getCreateTime)
        );
        return TableDataInfo.build(page.getRecords(), page.getTotal());
    }

    /**
     * 新增定时任务
     */
    @Log(title = "定时任务管理", businessType = BusinessType.INSERT)
    @PostMapping("/sys/job/add")
    public AjaxResult add(@RequestBody SysJob job) {
        sysJobService.save(job);
        return AjaxResult.success();
    }

    /**
     * 编辑定时任务
     */
    @Log(title = "定时任务管理", businessType = BusinessType.UPDATE)
    @PostMapping("/sys/job/edit")
    public AjaxResult edit(@RequestBody SysJob job) {
        sysJobService.updateById(job);
        return AjaxResult.success();
    }

    /**
     * 删除定时任务
     */
    @Log(title = "定时任务管理", businessType = BusinessType.DELETE)
    @PostMapping("/sys/job/delete")
    public AjaxResult delete(@RequestBody Map<String, Long> param) {
        sysJobService.removeById(param.get("jobId"));
        return AjaxResult.success();
    }

    /**
     * 执行定时任务
     */
    @Log(title = "定时任务管理", businessType = BusinessType.OTHER)
    @PostMapping("/sys/job/run")
    public AjaxResult run(@RequestBody Map<String, Long> param) {
        sysJobService.run(param.get("jobId"));
        return AjaxResult.success();
    }

    /**
     * 暂停定时任务
     */
    @Log(title = "定时任务管理", businessType = BusinessType.UPDATE)
    @PostMapping("/sys/job/pause")
    public AjaxResult pause(@RequestBody Map<String, Long> param) {
        sysJobService.pause(param.get("jobId"));
        return AjaxResult.success();
    }

    /**
     * 恢复定时任务
     */
    @Log(title = "定时任务管理", businessType = BusinessType.UPDATE)
    @PostMapping("/sys/job/resume")
    public AjaxResult resume(@RequestBody Map<String, Long> param) {
        sysJobService.resume(param.get("jobId"));
        return AjaxResult.success();
    }

    /**
     * 查询任务执行日志
     */
    @Log(title = "定时任务管理", businessType = BusinessType.QUERY)
    @PostMapping("/sys/job/log/page")
    public TableDataInfo logPage(@RequestBody PageDomain pageDomain) {
        Page<SysJobLog> page = sysJobLogMapper.selectPage(
                new Page<>(pageDomain.getPageNum(), pageDomain.getPageSize()),
                new LambdaQueryWrapper<SysJobLog>().orderByDesc(SysJobLog::getStartTime)
        );
        return TableDataInfo.build(page.getRecords(), page.getTotal());
    }

    /**
     * 清空任务执行日志
     */
    @Log(title = "定时任务管理", businessType = BusinessType.DELETE)
    @PostMapping("/sys/job/log/clear")
    public AjaxResult logClear() {
        sysJobLogMapper.delete(new LambdaQueryWrapper<>());
        return AjaxResult.success();
    }
}