package com.cloudbase.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudbase.module.system.entity.SysDept;

import java.util.List;

/**
 * 部门服务接口
 */
public interface ISysDeptService extends IService<SysDept> {

    /**
     * 获取部门树
     */
    List<SysDept> getDeptTree();

    /**
     * 创建部门
     */
    void createDept(SysDept dept);

    /**
     * 更新部门
     */
    void updateDept(SysDept dept);

    /**
     * 删除部门（检查子部门）
     */
    void deleteDept(Long deptId);

    /**
     * 修改部门状态
     */
    void updateStatus(Long deptId, Integer status);
}
