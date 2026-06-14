package com.cloudbase.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.domain.TableDataInfo;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.entity.SysNotice;
import com.cloudbase.module.system.mapper.SysNoticeMapper;
import com.cloudbase.module.system.model.dto.IdDTO;
import com.cloudbase.module.system.model.dto.NoticeCreateDTO;
import com.cloudbase.module.system.model.dto.NoticeUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 通知公告管理（重构后使用DTO）
 */
@Validated
@RestController
@RequestMapping("/sys/notice")
@RequiredArgsConstructor
public class SysNoticeController {

    private final SysNoticeMapper noticeMapper;

    /**
     * 查询公告列表
     */
    @Log(title = "通知公告管理", businessType = BusinessType.QUERY)
    @PostMapping("/page")
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
    @PostMapping("/detail")
    public AjaxResult detail(@Valid @RequestBody IdDTO dto) {
        return AjaxResult.success(noticeMapper.selectById(dto.getId()));
    }

    /**
     * 新增公告
     */
    @Log(title = "通知公告管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult add(@Valid @RequestBody NoticeCreateDTO dto) {
        SysNotice notice = new SysNotice();
        notice.setNoticeTitle(dto.getNoticeTitle());
        notice.setNoticeType(dto.getNoticeType());
        notice.setNoticeContent(dto.getNoticeContent());
        notice.setStatus(dto.getStatus());
        noticeMapper.insert(notice);
        return AjaxResult.success();
    }

    /**
     * 编辑公告
     */
    @Log(title = "通知公告管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    public AjaxResult edit(@Valid @RequestBody NoticeUpdateDTO dto) {
        SysNotice notice = new SysNotice();
        notice.setNoticeId(dto.getNoticeId());
        notice.setNoticeTitle(dto.getNoticeTitle());
        notice.setNoticeType(dto.getNoticeType());
        notice.setNoticeContent(dto.getNoticeContent());
        notice.setStatus(dto.getStatus());
        noticeMapper.updateById(notice);
        return AjaxResult.success();
    }

    /**
     * 删除公告
     */
    @Log(title = "通知公告管理", businessType = BusinessType.DELETE)
    @PostMapping("/delete")
    public AjaxResult delete(@Valid @RequestBody IdDTO dto) {
        noticeMapper.deleteById(dto.getId());
        return AjaxResult.success();
    }
}
