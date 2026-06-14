package com.cloudbase.module.system.service.impl;

import com.cloudbase.common.core.exception.BusinessException;
import com.cloudbase.module.system.entity.SysUser;
import com.cloudbase.module.system.mapper.SysUserMapper;
import com.cloudbase.module.system.model.vo.UserVO;
import com.cloudbase.module.system.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 个人中心服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final SysUserMapper sysUserMapper;

    @Override
    public UserVO getProfile(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserVO vo = new UserVO();
        vo.setUserId(user.getUserId());
        vo.setAccount(user.getAccount());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setDeptId(user.getDeptId());
        vo.setStatus(user.getStatus());
        vo.setLastLoginIp(user.getLastLoginIp());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    @Override
    public void updateProfile(Long userId, String realName, String phone, String email, String avatar) {
        SysUser user = new SysUser();
        user.setUserId(userId);
        if (realName != null) user.setRealName(realName);
        if (phone != null) user.setPhone(phone);
        if (email != null) user.setEmail(email);
        if (avatar != null) user.setAvatar(avatar);
        sysUserMapper.updateById(user);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 验证旧密码
        String oldHash = LoginServiceImpl.sha256Hex(oldPassword + user.getAccount());
        if (!oldHash.equalsIgnoreCase(user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("新密码长度不能少于6位");
        }
        // 更新密码
        String newHash = LoginServiceImpl.sha256Hex(newPassword + user.getAccount());
        SysUser update = new SysUser();
        update.setUserId(userId);
        update.setPassword(newHash);
        sysUserMapper.updateById(update);
    }
}
