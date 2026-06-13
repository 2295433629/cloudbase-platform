package com.cloudbase.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudbase.module.system.entity.SysDept;

import java.util.List;

public interface ISysDeptService extends IService<SysDept> {
    /** 获取部门树 */
    List<SysDept> getDeptTree();
}
