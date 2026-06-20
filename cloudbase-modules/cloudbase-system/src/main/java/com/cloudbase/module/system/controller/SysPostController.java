package com.cloudbase.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.domain.TableDataInfo;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.entity.SysPost;
import com.cloudbase.module.system.model.dto.IdDTO;
import com.cloudbase.module.system.model.dto.PostCreateDTO;
import com.cloudbase.module.system.model.dto.PostUpdateDTO;
import com.cloudbase.module.system.service.ISysPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 岗位管理
 */
@Validated
@RestController
@RequestMapping("/sys/post")
@RequiredArgsConstructor
public class SysPostController {

    private final ISysPostService postService;

    /**
     * 分页查询岗位
     */
    @Log(title = "岗位管理", businessType = BusinessType.QUERY)
    @PostMapping("/page")
    public TableDataInfo page(@RequestBody Map<String, Object> params) {
        int pageNo = params.get("pageNo") != null ? Integer.parseInt(params.get("pageNo").toString()) : 1;
        int pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 20;
        String postName = params.get("postName") != null ? params.get("postName").toString() : null;

        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
        if (postName != null && !postName.isEmpty()) {
            wrapper.like(SysPost::getPostName, postName);
        }
        if (params.get("status") != null) {
            wrapper.eq(SysPost::getStatus, Integer.parseInt(params.get("status").toString()));
        }
        wrapper.orderByAsc(SysPost::getSort);

        Page<SysPost> page = postService.page(new Page<>(pageNo, pageSize), wrapper);
        return TableDataInfo.build(page.getRecords(), page.getTotal());
    }

    /**
     * 获取全部岗位列表（不分页，用于下拉选择）
     */
    @Log(title = "岗位管理", businessType = BusinessType.QUERY)
    @PostMapping("/list")
    public AjaxResult list() {
        return AjaxResult.success(postService.getPostList());
    }

    /**
     * 新增岗位
     */
    @Log(title = "岗位管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult add(@Valid @RequestBody PostCreateDTO dto) {
        SysPost post = new SysPost();
        post.setPostCode(dto.getPostCode());
        post.setPostName(dto.getPostName());
        post.setSort(dto.getSort() != null ? dto.getSort() : 0);
        post.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        post.setRemark(dto.getRemark());
        postService.createPost(post);
        return AjaxResult.success();
    }

    /**
     * 编辑岗位
     */
    @Log(title = "岗位管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    public AjaxResult edit(@Valid @RequestBody PostUpdateDTO dto) {
        SysPost post = new SysPost();
        post.setPostId(dto.getPostId());
        post.setPostCode(dto.getPostCode());
        post.setPostName(dto.getPostName());
        post.setSort(dto.getSort() != null ? dto.getSort() : 0);
        post.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        post.setRemark(dto.getRemark());
        postService.updatePost(post);
        return AjaxResult.success();
    }

    /**
     * 删除岗位
     */
    @Log(title = "岗位管理", businessType = BusinessType.DELETE)
    @PostMapping("/delete")
    public AjaxResult delete(@Valid @RequestBody IdDTO dto) {
        postService.deletePost(dto.getId());
        return AjaxResult.success();
    }

    /**
     * 修改岗位状态
     */
    @Log(title = "岗位管理", businessType = BusinessType.UPDATE)
    @PostMapping("/updateStatus")
    public AjaxResult updateStatus(@RequestBody Map<String, Object> params) {
        Long postId = Long.parseLong(params.get("postId").toString());
        Integer status = Integer.parseInt(params.get("status").toString());
        postService.updateStatus(postId, status);
        return AjaxResult.success();
    }
}
