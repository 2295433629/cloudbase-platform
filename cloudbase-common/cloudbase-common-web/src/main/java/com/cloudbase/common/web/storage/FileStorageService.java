package com.cloudbase.common.web.storage;

import java.io.InputStream;

/**
 * 文件存储服务接口
 * <p>
 * 抽象文件上传/下载/删除操作，支持多种存储后端（本地磁盘、MinIO、OSS 等）。
 * 通过 {@code cloudbase.storage.type} 配置切换实现。
 * </p>
 */
public interface FileStorageService {

    /**
     * 上传文件
     *
     * @param inputStream 文件流
     * @param fileName    原始文件名
     * @param contentType MIME 类型（如 image/png）
     * @return 存储后的访问路径/Key
     */
    String upload(InputStream inputStream, String fileName, String contentType);

    /**
     * 删除文件
     *
     * @param fileKey 文件标识（upload 返回的路径）
     * @return 是否删除成功
     */
    boolean delete(String fileKey);

    /**
     * 获取文件的访问 URL
     *
     * @param fileKey 文件标识
     * @return 完整访问地址
     */
    String getUrl(String fileKey);
}
