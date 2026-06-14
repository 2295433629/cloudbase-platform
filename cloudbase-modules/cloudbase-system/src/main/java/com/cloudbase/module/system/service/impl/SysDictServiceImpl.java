package com.cloudbase.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbase.module.system.entity.SysDict;
import com.cloudbase.module.system.mapper.SysDictMapper;
import com.cloudbase.module.system.model.dto.DictQueryDTO;
import com.cloudbase.module.system.service.ISysDictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典服务实现
 */
@Slf4j
@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements ISysDictService {

    @Override
    public void createDict(SysDict dict) {
        save(dict);
    }

    @Override
    public void updateDict(SysDict dict) {
        updateById(dict);
    }

    @Override
    public void deleteDict(Long dictId) {
        removeById(dictId);
    }

    @Override
    public Page<SysDict> pageDicts(DictQueryDTO query) {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        if (query.getDictType() != null && !query.getDictType().isEmpty()) {
            wrapper.like(SysDict::getDictType, query.getDictType());
        }
        if (query.getDictLabel() != null && !query.getDictLabel().isEmpty()) {
            wrapper.like(SysDict::getDictLabel, query.getDictLabel());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysDict::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(SysDict::getSort);

        int pageNo = Math.max(query.getPageNo() != null ? query.getPageNo() : 1, 1);
        int pageSize = Math.min(Math.max(query.getPageSize() != null ? query.getPageSize() : 20, 1), 200);

        return page(new Page<>(pageNo, pageSize), wrapper);
    }

    @Override
    public List<SysDict> listByDictType(String dictType) {
        return list(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictType, dictType)
                .eq(SysDict::getStatus, 1)
                .orderByAsc(SysDict::getSort));
    }

    @Override
    public void updateStatus(Long dictId, Integer status) {
        SysDict dict = new SysDict();
        dict.setDictId(dictId);
        dict.setStatus(status);
        updateById(dict);
    }
}
