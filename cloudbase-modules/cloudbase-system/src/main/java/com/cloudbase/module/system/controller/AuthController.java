package com.cloudbase.module.system.controller;

import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.common.web.auth.UserContext;
import com.cloudbase.module.system.model.dto.ChangePasswordDTO;
import com.cloudbase.module.system.model.dto.LoginDTO;
import com.cloudbase.module.system.model.dto.UpdateProfileDTO;
import com.cloudbase.module.system.service.CaptchaService;
import com.cloudbase.module.system.service.LoginService;
import com.cloudbase.module.system.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证管理（重构后：分别委托给LoginService/CaptchaService/ProfileService）
 */
@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginService loginService;
    private final CaptchaService captchaService;
    private final ProfileService profileService;

    /**
     * 获取验证码
     */
    @Log(title = "认证管理", businessType = BusinessType.QUERY)
    @GetMapping("/captcha")
    public AjaxResult captcha() {
        return AjaxResult.success(captchaService.generateCaptcha());
    }

    /**
     * 用户登录
     */
    @Log(title = "认证管理", businessType = BusinessType.OTHER)
    @PostMapping("/login")
    public AjaxResult login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        String ip = getClientIp(request);
        return AjaxResult.success(loginService.login(
                dto.getAccount(), dto.getPassword(), dto.getUuid(), dto.getCaptcha(), ip));
    }

    /**
     * 退出登录
     */
    @Log(title = "认证管理", businessType = BusinessType.OTHER)
    @PostMapping("/logout")
    public AjaxResult logout(@RequestBody Map<String, String> params) {
        loginService.logout(params.get("token"));
        return AjaxResult.success();
    }

    // ==================== 个人中心 ====================

    /**
     * 获取当前登录用户个人信息
     */
    @Log(title = "个人中心", businessType = BusinessType.QUERY)
    @PostMapping("/profile")
    public AjaxResult getProfile() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return AjaxResult.error("未登录");
        }
        return AjaxResult.success(profileService.getProfile(userId));
    }

    /**
     * 修改个人基本信息
     */
    @Log(title = "个人中心", businessType = BusinessType.UPDATE)
    @PostMapping("/updateProfile")
    public AjaxResult updateProfile(@RequestBody UpdateProfileDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return AjaxResult.error("未登录");
        }
        profileService.updateProfile(userId, dto.getRealName(), dto.getPhone(),
                dto.getEmail(), dto.getAvatar());
        return AjaxResult.success();
    }

    /**
     * 修改密码
     */
    @Log(title = "个人中心", businessType = BusinessType.UPDATE)
    @PostMapping("/changePassword")
    public AjaxResult changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return AjaxResult.error("未登录");
        }
        profileService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return AjaxResult.success();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}
