package com.cloudbase.module.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.domain.PageQuery;
import com.cloudbase.common.core.domain.TableDataInfo;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.module.system.entity.GenTable;
import com.cloudbase.module.system.service.IGenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 代码生成管理
 */
@Validated
@RestController
@RequestMapping("/sys/gen")
@RequiredArgsConstructor
public class GenController {

    private final IGenService genService;

    // ========================= 数据库表查询 =========================

    /**
     * 查询数据库中所有表（含未导入的）
     */
    @Log(title = "代码生成管理", businessType = BusinessType.QUERY)
    @PostMapping("/db/list")
    public AjaxResult dbList() {
        return AjaxResult.success(genService.listDbTables());
    }

    /**
     * 查询指定数据库表的列信息
     */
    @Log(title = "代码生成管理", businessType = BusinessType.QUERY)
    @PostMapping("/db/columns")
    public AjaxResult dbColumns(@RequestBody Map<String, String> param) {
        return AjaxResult.success(genService.listDbTableColumns(param.get("tableName")));
    }

    // ========================= 导入表管理 =========================

    /**
     * 导入数据库表到代码生成配置
     */
    @Log(title = "代码生成管理", businessType = BusinessType.INSERT)
    @PostMapping("/import")
    public AjaxResult importTables(@RequestBody Map<String, Object> param) {
        @SuppressWarnings("unchecked")
        List<String> tableNames = (List<String>) param.get("tableNames");
        if (tableNames == null || tableNames.isEmpty()) {
            return AjaxResult.error("请选择要导入的表");
        }
        genService.importTables(tableNames);
        return AjaxResult.success("成功导入 " + tableNames.size() + " 张表");
    }

    /**
     * 分页查询已导入的表列表
     */
    @Log(title = "代码生成管理", businessType = BusinessType.QUERY)
    @PostMapping("/page")
    public TableDataInfo page(@RequestBody Map<String, Object> params) {
        var pageInfo = PageQuery.of(params);
        String tableName = params.containsKey("tableName") ? params.get("tableName").toString() : null;
        IPage<GenTable> page = genService.selectGenTablePage(pageInfo.pageNo(), pageInfo.pageSize(), tableName);
        return TableDataInfo.build(page.getRecords(), page.getTotal());
    }

    /**
     * 查询表详情（含列配置）
     */
    @Log(title = "代码生成管理", businessType = BusinessType.QUERY)
    @PostMapping("/detail")
    public AjaxResult detail(@RequestBody Map<String, Long> param) {
        Long tableId = param.get("tableId");
        if (tableId == null) {
            return AjaxResult.error("参数tableId不能为空");
        }
        return AjaxResult.success(genService.selectGenTableById(tableId));
    }

    /**
     * 修改表生成配置（含列配置）
     */
    @Log(title = "代码生成管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody GenTable genTable) {
        if (genTable.getTableId() == null) {
            return AjaxResult.error("参数tableId不能为空");
        }
        genService.updateGenTable(genTable);
        return AjaxResult.success();
    }

    /**
     * 同步数据库列信息（刷新列配置）
     */
    @Log(title = "代码生成管理", businessType = BusinessType.UPDATE)
    @PostMapping("/sync")
    public AjaxResult sync(@RequestBody Map<String, Long> param) {
        Long tableId = param.get("tableId");
        if (tableId == null) {
            return AjaxResult.error("参数tableId不能为空");
        }
        genService.syncDbColumns(tableId);
        return AjaxResult.success("同步成功");
    }

    /**
     * 删除已导入的表配置
     */
    @Log(title = "代码生成管理", businessType = BusinessType.DELETE)
    @PostMapping("/delete")
    public AjaxResult delete(@RequestBody Map<String, Object> param) {
        @SuppressWarnings("unchecked")
        List<Number> ids = (List<Number>) param.get("tableIds");
        if (ids == null || ids.isEmpty()) {
            return AjaxResult.error("请选择要删除的表");
        }
        List<Long> tableIds = ids.stream().map(Number::longValue).collect(Collectors.toList());
        genService.deleteGenTables(tableIds);
        return AjaxResult.success();
    }

    // ========================= 代码生成 =========================

    /**
     * 预览生成代码
     */
    @Log(title = "代码生成管理", businessType = BusinessType.QUERY)
    @PostMapping("/preview")
    public AjaxResult preview(@RequestBody Map<String, Object> param) {
        @SuppressWarnings("unchecked")
        List<Number> ids = (List<Number>) param.get("tableIds");
        if (ids == null || ids.isEmpty()) {
            return AjaxResult.error("请选择要预览的表");
        }
        List<Long> tableIds = ids.stream().map(Number::longValue).collect(Collectors.toList());
        return AjaxResult.success(genService.previewCode(tableIds));
    }

    /**
     * 生成代码（返回 ZIP 二进制流）
     */
    @Log(title = "代码生成管理", businessType = BusinessType.OTHER)
    @PostMapping("/generate")
    public void generate(@RequestBody Map<String, Object> param, HttpServletResponse response) {
        @SuppressWarnings("unchecked")
        List<Number> ids = (List<Number>) param.get("tableIds");
        if (ids == null || ids.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        List<Long> tableIds = ids.stream().map(Number::longValue).collect(Collectors.toList());
        byte[] data = genService.generateCode(tableIds);
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=generated.zip");
        response.setContentLength(data.length);
        try {
            response.getOutputStream().write(data);
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new RuntimeException("写入ZIP文件失败", e);
        }
    }
}
