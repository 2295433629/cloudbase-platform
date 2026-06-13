package com.cloudbase.module.system.controller;

import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.service.IGenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 代码生成管理
 *
 * @author ruoyi
 */
@RestController
@RequiredArgsConstructor
public class GenController {

    private final IGenService genService;

    /**
     * 查询所有数据库表
     */
    @Log(title = "代码生成管理", businessType = BusinessType.QUERY)
    @PostMapping("/sys/gen/db/list")
    public AjaxResult dbList() {
        return AjaxResult.success(genService.listDbTables());
    }

    /**
     * 查询表的列信息
     */
    @Log(title = "代码生成管理", businessType = BusinessType.QUERY)
    @PostMapping("/sys/gen/db/columns")
    public AjaxResult dbColumns(@RequestBody Map<String, String> param) {
        return AjaxResult.success(genService.listTableColumns(param.get("tableName")));
    }

    /**
     * 预览生成代码
     */
    @Log(title = "代码生成管理", businessType = BusinessType.QUERY)
    @PostMapping("/sys/gen/preview")
    public AjaxResult preview(@RequestBody Map<String, Object> param) {
        @SuppressWarnings("unchecked")
        List<String> tableNames = (List<String>) param.get("tableNames");
        return AjaxResult.success(genService.previewCode(tableNames));
    }

    /**
     * 生成代码
     */
    @Log(title = "代码生成管理", businessType = BusinessType.OTHER)
    @PostMapping("/sys/gen/generate")
    public AjaxResult generate(@RequestBody Map<String, Object> param) {
        @SuppressWarnings("unchecked")
        List<String> tableNames = (List<String>) param.get("tableNames");
        return AjaxResult.success(genService.generateCode(tableNames));
    }
}