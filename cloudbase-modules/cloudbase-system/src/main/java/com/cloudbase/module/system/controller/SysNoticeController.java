package com.cloudbase.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.domain.TableDataInfo;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.entity.SysNotice;
import com.cloudbase.module.system.mapper.SysNoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 通知公告管理
 *
 * @author ruoyi
 */
@RestController
@RequiredArgsConstructor
public class SysNoticeController {

    private final SysNoticeMapper noticeMapper;

    /**
     * 查询公告列表
     */
    @Log(title = "通知公告管理", businessType = BusinessType.QUERY)
    @PostMapping("/sys/notice/page")
    public TableDataInfo page(@RequestBody Map<String, Object> params) {
        int pageNo = params.containsKey("pageNo") ? Integer.parseInt(params.get("pageNo").toString()) : 1;
        int pageSize = params.containsKey("pageSize") ? Integer.parseInt(params.get("pageSize").toString()) : 20;
        pageNo = Math.max(pageNo, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 200);

        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        if (params.containsKey("noticeTitle")) {
            wrapper.like(SysNotice::getNoticeTitle, params.get("noticeTitle"));
        }
        wrapper.orderByDesc(SysNotice::getCreateTime);

        Page<SysNotice> page = noticeMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return TableDataInfo.build(page.getRecords(), page.getTotal());
    }

    /**
     * 查看公告详情
     */
    @Log(title = "通知公告管理", businessType = BusinessType.QUERY)
    @PostMapping("/sys/notice/detail")
    public AjaxResult detail(@RequestBody Map<String, Long> params) {
        return AjaxResult.success(noticeMapper.selectById(params.get("noticeId")));
    }

    /**
     * 新增公告
     */
    @Log(title = "通知公告管理", businessType = BusinessType.INSERT)
    @PostMapping("/sys/notice/add")
    public AjaxResult add(@RequestBody SysNotice notice) {
        noticeMapper.insert(notice);
        return AjaxResult.success();
    }

    /**
     * 编辑公告
     */
    @Log(title = "通知公告管理", businessType = BusinessType.UPDATE)
    @PostMapping("/sys/notice/edit")
    public AjaxResult edit(@RequestBody SysNotice notice) {
        noticeMapper.updateById(notice);
        return AjaxResult.success();
    }

    /**
     * 删除公告
     */
    @Log(title = "通知公告管理", businessType = BusinessType.DELETE)
    @PostMapping("/sys/notice/delete")
    public AjaxResult delete(@RequestBody Map<String, Long> params) {
        noticeMapper.deleteById(params.get("noticeId"));
        return AjaxResult.success();
    }
}