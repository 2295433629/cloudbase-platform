package com.cloudbase.module.system.controller;

import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.domain.TableDataInfo;
import com.cloudbase.common.core.exception.BusinessException;
import com.cloudbase.common.core.exception.CommonExceptionEnum;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.entity.SysDict;
import com.cloudbase.module.system.model.dto.DictCreateDTO;
import com.cloudbase.module.system.model.dto.DictQueryDTO;
import com.cloudbase.module.system.model.dto.DictUpdateDTO;
import com.cloudbase.module.system.model.dto.IdDTO;
import com.cloudbase.module.system.service.ISysDictService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 字典管理（重构后使用DTO+Service）
 */
@Validated
@RestController
@RequestMapping("/sys/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final ISysDictService sysDictService;

    /**
     * 分页查询字典
     */
    @Log(title = "字典管理", businessType = BusinessType.QUERY)
    @PostMapping("/page")
    public TableDataInfo page(@Valid @RequestBody DictQueryDTO query) {
        var page = sysDictService.pageDicts(query);
        return TableDataInfo.build(page.getRecords(), page.getTotal());
    }

    /**
     * 按字典类型查询字典值列表（不分页，前端下拉用）
     */
    @Log(title = "字典管理", businessType = BusinessType.QUERY)
    @PostMapping("/listByType")
    public AjaxResult listByType(@RequestBody java.util.Map<String, String> params) {
        return AjaxResult.success(sysDictService.listByDictType(params.get("dictType")));
    }

    /**
     * 新增字典
     */
    @Log(title = "字典管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult add(@Valid @RequestBody DictCreateDTO dto) {
        SysDict dict = new SysDict();
        dict.setDictType(dto.getDictType());
        dict.setDictLabel(dto.getDictLabel());
        dict.setDictValue(dto.getDictValue());
        dict.setSort(dto.getSort());
        dict.setStatus(dto.getStatus());
        dict.setRemark(dto.getRemark());
        sysDictService.createDict(dict);
        return AjaxResult.success();
    }

    /**
     * 编辑字典
     */
    @Log(title = "字典管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    public AjaxResult edit(@Valid @RequestBody DictUpdateDTO dto) {
        SysDict dict = new SysDict();
        dict.setDictId(dto.getDictId());
        dict.setDictType(dto.getDictType());
        dict.setDictLabel(dto.getDictLabel());
        dict.setDictValue(dto.getDictValue());
        dict.setSort(dto.getSort());
        dict.setStatus(dto.getStatus());
        dict.setRemark(dto.getRemark());
        sysDictService.updateDict(dict);
        return AjaxResult.success();
    }

    /**
     * 删除字典
     */
    @Log(title = "字典管理", businessType = BusinessType.DELETE)
    @PostMapping("/delete")
    public AjaxResult delete(@Valid @RequestBody IdDTO dto) {
        sysDictService.deleteDict(dto.getId());
        return AjaxResult.success();
    }

    /**
     * 修改字典状态
     */
    @Log(title = "字典管理", businessType = BusinessType.UPDATE)
    @PostMapping("/updateStatus")
    public AjaxResult updateStatus(@RequestBody java.util.Map<String, Object> params) {
        Long dictId = Long.parseLong(params.get("dictId").toString());
        Integer status = Integer.parseInt(params.get("status").toString());
        sysDictService.updateStatus(dictId, status);
        return AjaxResult.success();
    }

    /**
     * 批量导入字典
     */
    @Log(title = "字典管理", businessType = BusinessType.INSERT)
    @PostMapping("/batchImport")
    public AjaxResult batchImport(@RequestBody List<SysDict> list) {
        if (list == null || list.isEmpty()) {
            throw new BusinessException(CommonExceptionEnum.PARAM_ERROR.getErrorCode(), "导入数据为空");
        }
        int successCount = 0;
        for (SysDict dict : list) {
            if (dict.getDictType() == null || dict.getDictType().isEmpty()
                    || dict.getDictLabel() == null || dict.getDictLabel().isEmpty()
                    || dict.getDictValue() == null || dict.getDictValue().isEmpty()) {
                continue;
            }
            if (dict.getSort() == null) dict.setSort(0);
            if (dict.getStatus() == null) dict.setStatus(1);
            sysDictService.createDict(dict);
            successCount++;
        }
        return AjaxResult.success("成功导入 " + successCount + " 条数据");
    }
}
