package com.cloudbase.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudbase.module.system.entity.SysPost;

import java.util.List;

/**
 * 岗位服务接口
 */
public interface ISysPostService extends IService<SysPost> {

    /**
     * 获取岗位列表
     */
    List<SysPost> getPostList();

    /**
     * 创建岗位
     */
    void createPost(SysPost post);

    /**
     * 更新岗位
     */
    void updatePost(SysPost post);

    /**
     * 删除岗位
     */
    void deletePost(Long postId);

    /**
     * 修改岗位状态
     */
    void updateStatus(Long postId, Integer status);
}
