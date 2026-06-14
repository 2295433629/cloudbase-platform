package com.cloudbase.module.system.service;

import com.cloudbase.module.system.model.vo.CaptchaVO;
import com.cloudbase.module.system.model.vo.LoginVO;

/**
 * 登录服务接口
 */
public interface LoginService {

    /**
     * 用户登录
     */
    LoginVO login(String account, String password, String uuid, String captcha, String ip);

    /**
     * 退出登录
     */
    void logout(String token);
}
