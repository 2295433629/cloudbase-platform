package com.cloudbase.common.web.storage;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 本地文件存储实现
 * <p>
 * 将文件存储到服务器本地磁盘，按日期目录归档。
 * 文件路径格式：{basePath}/{yyyy/MM/dd}/{uuid}.{ext}
 * </p>
 */
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    private final String basePath;
    private final String accessPath;

    /**
     * @param basePath    文件存储根目录（如 D:/uploads/cloudbase 或 /var/uploads/cloudbase）
     * @param accessPath  Web 访问前缀（如 /files），配合静态资源映射使用
     */
    public LocalFileStorageService(String basePath, String accessPath) {
        this.basePath = basePath;
        this.accessPath = accessPath;
        // 确保根目录存在
        FileUtil.mkdir(basePath);
        log.info("本地文件存储已初始化: basePath={}, accessPath={}", basePath, accessPath);
    }

    @Override
    public String upload(InputStream inputStream, String fileName, String contentType) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String ext = StrUtil.isNotBlank(fileName) ? FileUtil.extName(fileName) : "";
        String storedName = IdUtil.fastSimpleUUID() + (StrUtil.isNotBlank(ext) ? "." + ext : "");

        String relativePath = datePath + "/" + storedName;
        Path targetPath = Paths.get(basePath, datePath, storedName);

        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(inputStream, targetPath);
            log.info("文件上传成功: {}", targetPath);
            return accessPath + "/" + relativePath;
        } catch (IOException e) {
            log.error("文件上传失败: {}", targetPath, e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public boolean delete(String fileKey) {
        if (StrUtil.isBlank(fileKey) || !fileKey.startsWith(accessPath + "/")) {
            return false;
        }
        String relativePath = fileKey.substring(accessPath.length() + 1);
        Path filePath = Paths.get(basePath, relativePath);
        try {
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("文件删除失败: {}", filePath, e);
            return false;
        }
    }

    @Override
    public String getUrl(String fileKey) {
        // 本地存储直接返回相对路径，由 Nginx 或静态资源映射处理
        return fileKey;
    }

    /**
     * 工具方法：将 MultipartFile 转为 InputStream 上传
     */
    public String upload(MultipartFile file) {
        try {
            return upload(file.getInputStream(), file.getOriginalFilename(), file.getContentType());
        } catch (IOException e) {
            throw new RuntimeException("读取上传文件失败", e);
        }
    }
}
