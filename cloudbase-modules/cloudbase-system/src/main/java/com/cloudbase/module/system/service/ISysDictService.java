package com.cloudbase.module.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudbase.module.system.entity.SysDict;
import com.cloudbase.module.system.model.dto.DictQueryDTO;

import java.util.List;

/**
 * 字典服务接口
 */
public interface ISysDictService extends IService<SysDict> {

    /**
     * 创建字典
     */
    void createDict(SysDict dict);

    /**
     * 更新字典
     */
    void updateDict(SysDict dict);

    /**
     * 删除字典
     */
    void deleteDict(Long dictId);

    /**
     * 分页查询字典
     */
    Page<SysDict> pageDicts(DictQueryDTO query);

    /**
     * 按字典类型查询字典值列表（不分页）
     */
    List<SysDict> listByDictType(String dictType);

    /**
     * 修改字典状态
     */
    void updateStatus(Long dictId, Integer status);
}
