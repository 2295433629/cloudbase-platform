package com.cloudbase.module.system.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 验证码响应VO
 */
@Data
public class CaptchaVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String uuid;
    private String image;
}
