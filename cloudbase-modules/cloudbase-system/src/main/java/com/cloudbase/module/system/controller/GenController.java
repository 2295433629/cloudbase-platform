package com.cloudbase.module.system.controller;

import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.enums.BusinessType;
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

/**
 * 代码生成管理（保持不变，无对应Vue页面，保持基本结构）
 */
@Validated
@RestController
@RequestMapping("/sys/gen")
@RequiredArgsConstructor
public class GenController {

    private final IGenService genService;

    /**
     * 查询所有数据库表
     */
    @Log(title = "代码生成管理", businessType = BusinessType.QUERY)
    @PostMapping("/db/list")
    public AjaxResult dbList() {
        return AjaxResult.success(genService.listDbTables());
    }

    /**
     * 查询表的列信息
     */
    @Log(title = "代码生成管理", businessType = BusinessType.QUERY)
    @PostMapping("/db/columns")
    public AjaxResult dbColumns(@RequestBody Map<String, String> param) {
        return AjaxResult.success(genService.listTableColumns(param.get("tableName")));
    }

    /**
     * 预览生成代码
     */
    @Log(title = "代码生成管理", businessType = BusinessType.QUERY)
    @PostMapping("/preview")
    public AjaxResult preview(@RequestBody Map<String, Object> param) {
        @SuppressWarnings("unchecked")
        List<String> tableNames = (List<String>) param.get("tableNames");
        return AjaxResult.success(genService.previewCode(tableNames));
    }

    /**
     * 生成代码（返回 ZIP 二进制流）
     */
    @Log(title = "代码生成管理", businessType = BusinessType.OTHER)
    @PostMapping("/generate")
    public void generate(@RequestBody Map<String, Object> param, HttpServletResponse response) {
        @SuppressWarnings("unchecked")
        List<String> tableNames = (List<String>) param.get("tableNames");
        byte[] data = genService.generateCode(tableNames);
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
