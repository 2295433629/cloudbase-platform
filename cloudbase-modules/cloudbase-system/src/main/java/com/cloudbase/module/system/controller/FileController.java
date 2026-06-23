package com.cloudbase.module.system.controller;

import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.exception.BusinessException;
import com.cloudbase.common.core.exception.CommonExceptionEnum;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.common.web.storage.FileStorageService;
import com.cloudbase.common.web.storage.LocalFileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件管理
 * <p>
 * 提供文件上传、删除接口。
 * 文件访问通过静态资源映射（/files/**）直接访问，无需经过 Controller。
 * </p>
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * 单文件上传
     */
    @Log(title = "文件管理", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(CommonExceptionEnum.PARAM_ERROR.getErrorCode(), "上传文件不能为空");
        }
        String url;
        if (fileStorageService instanceof LocalFileStorageService local) {
            url = local.upload(file);
        } else {
            try {
                url = fileStorageService.upload(file.getInputStream(),
                        file.getOriginalFilename(), file.getContentType());
            } catch (Exception e) {
                log.error("文件上传失败", e);
                throw new BusinessException(CommonExceptionEnum.SYSTEM_ERROR.getErrorCode(), "文件上传失败: " + e.getMessage());
            }
        }
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        result.put("fileName", file.getOriginalFilename());
        return AjaxResult.success(result);
    }

    /**
     * 多文件上传（最多10个）
     */
    @Log(title = "文件管理", businessType = BusinessType.INSERT)
    @PostMapping("/uploadBatch")
    public AjaxResult uploadBatch(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BusinessException(CommonExceptionEnum.PARAM_ERROR.getErrorCode(), "上传文件不能为空");
        }
        if (files.length > 10) {
            throw new BusinessException(CommonExceptionEnum.PARAM_ERROR.getErrorCode(), "单次最多上传10个文件");
        }
        List<Map<String, String>> results = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String url;
            if (fileStorageService instanceof LocalFileStorageService local) {
                url = local.upload(file);
            } else {
                try {
                    url = fileStorageService.upload(file.getInputStream(),
                            file.getOriginalFilename(), file.getContentType());
                } catch (Exception e) {
                    log.error("文件上传失败: {}", file.getOriginalFilename(), e);
                    continue;
                }
            }
            Map<String, String> item = new HashMap<>();
            item.put("url", url);
            item.put("fileName", file.getOriginalFilename());
            results.add(item);
        }
        return AjaxResult.success(results);
    }

    /**
     * 删除文件
     */
    @Log(title = "文件管理", businessType = BusinessType.DELETE)
    @PostMapping("/delete")
    public AjaxResult delete(@RequestBody Map<String, String> params) {
        String fileKey = params.get("fileKey");
        if (fileKey == null || fileKey.isBlank()) {
            throw new BusinessException(CommonExceptionEnum.PARAM_ERROR.getErrorCode(), "fileKey 不能为空");
        }
        boolean result = fileStorageService.delete(fileKey);
        if (!result) {
            throw new BusinessException(CommonExceptionEnum.DATA_NOT_FOUND.getErrorCode(), "文件不存在或删除失败");
        }
        return AjaxResult.success();
    }
}
