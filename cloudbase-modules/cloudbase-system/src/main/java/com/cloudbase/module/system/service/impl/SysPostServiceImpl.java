package com.cloudbase.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbase.common.core.exception.BusinessException;
import com.cloudbase.module.system.entity.SysPost;
import com.cloudbase.module.system.entity.SysUser;
import com.cloudbase.module.system.mapper.SysPostMapper;
import com.cloudbase.module.system.mapper.SysUserMapper;
import com.cloudbase.module.system.service.ISysPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 岗位服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPost> implements ISysPostService {

    private final SysUserMapper userMapper;

    @Override
    public List<SysPost> getPostList() {
        return list(new LambdaQueryWrapper<SysPost>().orderByAsc(SysPost::getSort));
    }

    @Override
    public void createPost(SysPost post) {
        save(post);
    }

    @Override
    public void updatePost(SysPost post) {
        updateById(post);
    }

    @Override
    public void deletePost(Long postId) {
        // 检查是否有用户关联该岗位
        long userCount = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getPostId, postId));
        if (userCount > 0) {
            throw new BusinessException("该岗位下存在关联用户，不允许删除");
        }
        removeById(postId);
    }

    @Override
    public void updateStatus(Long postId, Integer status) {
        SysPost post = new SysPost();
        post.setPostId(postId);
        post.setStatus(status);
        updateById(post);
    }
}
