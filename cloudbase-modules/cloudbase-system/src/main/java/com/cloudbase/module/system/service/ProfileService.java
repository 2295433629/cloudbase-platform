package com.cloudbase.module.system.service;

import com.cloudbase.module.system.entity.SysUser;
import com.cloudbase.module.system.model.vo.UserVO;

/**
 * 个人中心服务接口
 */
public interface ProfileService {

    /**
     * 获取当前用户个人信息
     */
    UserVO getProfile(Long userId);

    /**
     * 更新个人基本信息
     */
    void updateProfile(Long userId, String realName, String phone, String email, String avatar);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);
}
