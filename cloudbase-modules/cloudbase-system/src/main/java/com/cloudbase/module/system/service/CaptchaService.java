package com.cloudbase.module.system.service;

import com.cloudbase.module.system.model.vo.CaptchaVO;

/**
 * 验证码服务接口
 */
public interface CaptchaService {

    /**
     * 生成验证码
     */
    CaptchaVO generateCaptcha();
}
