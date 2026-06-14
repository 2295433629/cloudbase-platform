package com.cloudbase.module.system.controller;

import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.entity.SysDept;
import com.cloudbase.module.system.model.dto.DeptCreateDTO;
import com.cloudbase.module.system.model.dto.DeptUpdateDTO;
import com.cloudbase.module.system.model.dto.IdDTO;
import com.cloudbase.module.system.service.ISysDeptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 部门管理（重构后使用DTO+Service）
 */
@Validated
@RestController
@RequestMapping("/sys/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final ISysDeptService deptService;

    /**
     * 获取部门树
     */
    @Log(title = "部门管理", businessType = BusinessType.QUERY)
    @PostMapping("/tree")
    public AjaxResult tree() {
        return AjaxResult.success(deptService.getDeptTree());
    }

    /**
     * 新增部门
     */
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult add(@Valid @RequestBody DeptCreateDTO dto) {
        SysDept dept = new SysDept();
        dept.setParentId(dto.getParentId());
        dept.setDeptName(dto.getDeptName());
        dept.setSort(dto.getSort());
        dept.setLeader(dto.getLeader());
        dept.setPhone(dto.getPhone());
        dept.setEmail(dto.getEmail());
        dept.setStatus(dto.getStatus());
        deptService.createDept(dept);
        return AjaxResult.success();
    }

    /**
     * 编辑部门
     */
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    public AjaxResult edit(@Valid @RequestBody DeptUpdateDTO dto) {
        SysDept dept = new SysDept();
        dept.setDeptId(dto.getDeptId());
        dept.setParentId(dto.getParentId());
        dept.setDeptName(dto.getDeptName());
        dept.setSort(dto.getSort());
        dept.setLeader(dto.getLeader());
        dept.setPhone(dto.getPhone());
        dept.setEmail(dto.getEmail());
        dept.setStatus(dto.getStatus());
        deptService.updateDept(dept);
        return AjaxResult.success();
    }

    /**
     * 删除部门
     */
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @PostMapping("/delete")
    public AjaxResult delete(@Valid @RequestBody IdDTO dto) {
        deptService.deleteDept(dto.getId());
        return AjaxResult.success();
    }

    /**
     * 修改部门状态
     */
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PostMapping("/updateStatus")
    public AjaxResult updateStatus(@RequestBody java.util.Map<String, Object> params) {
        Long deptId = Long.parseLong(params.get("deptId").toString());
        Integer status = Integer.parseInt(params.get("status").toString());
        deptService.updateStatus(deptId, status);
        return AjaxResult.success();
    }
}
