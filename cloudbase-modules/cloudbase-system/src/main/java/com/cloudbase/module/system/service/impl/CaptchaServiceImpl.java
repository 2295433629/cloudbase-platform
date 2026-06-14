package com.cloudbase.module.system.service.impl;

import com.cloudbase.common.web.cache.CacheService;
import com.cloudbase.module.system.model.vo.CaptchaVO;
import com.cloudbase.module.system.service.CaptchaService;
import com.wf.captcha.SpecCaptcha;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final CacheService cacheService;

    @Override
    public CaptchaVO generateCaptcha() {
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        String uuid = UUID.randomUUID().toString();
        String captchaText = captcha.text().toLowerCase();
        cacheService.set("captcha:" + uuid, captchaText, 5, TimeUnit.MINUTES);

        CaptchaVO vo = new CaptchaVO();
        vo.setUuid(uuid);
        vo.setImage(captcha.toBase64());
        return vo;
    }
}
