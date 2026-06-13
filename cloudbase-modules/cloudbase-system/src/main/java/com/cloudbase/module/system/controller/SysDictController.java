package com.cloudbase.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.domain.TableDataInfo;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.entity.SysDict;
import com.cloudbase.module.system.service.ISysDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 字典管理
 */
@RestController
@RequiredArgsConstructor
public class SysDictController {

    private final ISysDictService sysDictService;

    /**
     * 分页查询字典
     */
    @Log(title = "字典管理", businessType = BusinessType.QUERY)
    @PostMapping("/sys/dict/page")
    public TableDataInfo page(@RequestBody Map<String, Object> params) {
        int pageNo = params.containsKey("pageNo") ? Integer.parseInt(params.get("pageNo").toString()) : 1;
        int pageSize = params.containsKey("pageSize") ? Integer.parseInt(params.get("pageSize").toString()) : 20;
        pageNo = Math.max(pageNo, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 200);

        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        if (params.containsKey("dictType") && params.get("dictType") != null
                && !params.get("dictType").toString().isEmpty()) {
            wrapper.like(SysDict::getDictType, params.get("dictType"));
        }
        if (params.containsKey("dictLabel") && params.get("dictLabel") != null
                && !params.get("dictLabel").toString().isEmpty()) {
            wrapper.like(SysDict::getDictLabel, params.get("dictLabel"));
        }
        if (params.containsKey("status") && params.get("status") != null) {
            wrapper.eq(SysDict::getStatus, params.get("status"));
        }
        wrapper.orderByAsc(SysDict::getSort);

        Page<SysDict> page = sysDictService.page(new Page<>(pageNo, pageSize), wrapper);
        return TableDataInfo.build(page.getRecords(), page.getTotal());
    }

    /**
     * 按字典类型查询字典值列表（不分页，前端下拉用）
     */
    @Log(title = "字典管理", businessType = BusinessType.QUERY)
    @PostMapping("/sys/dict/listByType")
    public AjaxResult listByType(@RequestBody Map<String, String> params) {
        String dictType = params.get("dictType");
        List<SysDict> list = sysDictService.list(
                new LambdaQueryWrapper<SysDict>()
                        .eq(SysDict::getDictType, dictType)
                        .eq(SysDict::getStatus, 1)
                        .orderByAsc(SysDict::getSort)
        );
        return AjaxResult.success(list);
    }

    /**
     * 新增字典
     */
    @Log(title = "字典管理", businessType = BusinessType.INSERT)
    @PostMapping("/sys/dict/add")
    public AjaxResult add(@RequestBody SysDict dict) {
        sysDictService.save(dict);
        return AjaxResult.success();
    }

    /**
     * 编辑字典
     */
    @Log(title = "字典管理", businessType = BusinessType.UPDATE)
    @PostMapping("/sys/dict/edit")
    public AjaxResult edit(@RequestBody SysDict dict) {
        sysDictService.updateById(dict);
        return AjaxResult.success();
    }

    /**
     * 删除字典
     */
    @Log(title = "字典管理", businessType = BusinessType.DELETE)
    @PostMapping("/sys/dict/delete")
    public AjaxResult delete(@RequestBody Map<String, Long> params) {
        sysDictService.removeById(params.get("dictId"));
        return AjaxResult.success();
    }

    /**
     * 修改字典状态
     */
    @Log(title = "字典管理", businessType = BusinessType.UPDATE)
    @PostMapping("/sys/dict/updateStatus")
    public AjaxResult updateStatus(@RequestBody Map<String, Object> params) {
        if (params.get("dictId") == null || params.get("status") == null) {
            return AjaxResult.error("参数缺失");
        }
        SysDict dict = new SysDict();
        dict.setDictId(Long.parseLong(params.get("dictId").toString()));
        dict.setStatus(Integer.parseInt(params.get("status").toString()));
        sysDictService.updateById(dict);
        return AjaxResult.success();
    }
}
