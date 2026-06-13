package com.cloudbase.module.system.controller;

import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.domain.TableDataInfo;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.common.web.auth.UserContext;
import com.cloudbase.module.system.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证管理
 *
 * @author ruoyi
 */
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 获取验证码
     */
    @Log(title = "认证管理", businessType = BusinessType.QUERY)
    @GetMapping("/auth/captcha")
    public AjaxResult captcha() {
        return AjaxResult.success(authService.generateCaptcha());
    }

    /**
     * 用户登录
     */
    @Log(title = "认证管理", businessType = BusinessType.OTHER)
    @PostMapping("/auth/login")
    public AjaxResult login(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String ip = getClientIp(request);
        return AjaxResult.success(authService.login(
                params.get("account"),
                params.get("password"),
                params.get("uuid"),
                params.get("captcha"),
                ip
        ));
    }

    /**
     * 退出登录
     */
    @Log(title = "认证管理", businessType = BusinessType.OTHER)
    @PostMapping("/auth/logout")
    public AjaxResult logout(@RequestBody Map<String, String> params) {
        authService.logout(params.get("token"));
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

    // ==================== 个人中心 ====================

    /**
     * 获取当前登录用户个人信息
     */
    @Log(title = "个人中心", businessType = BusinessType.QUERY)
    @PostMapping("/auth/profile")
    public AjaxResult getProfile() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return AjaxResult.error("未登录");
        }
        return AjaxResult.success(authService.getProfile(userId));
    }

    /**
     * 修改个人基本信息
     */
    @Log(title = "个人中心", businessType = BusinessType.UPDATE)
    @PostMapping("/auth/updateProfile")
    public AjaxResult updateProfile(@RequestBody Map<String, String> params) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return AjaxResult.error("未登录");
        }
        authService.updateProfile(
                userId,
                params.get("realName"),
                params.get("phone"),
                params.get("email"),
                params.get("avatar")
        );
        return AjaxResult.success();
    }

    /**
     * 修改密码
     */
    @Log(title = "个人中心", businessType = BusinessType.UPDATE)
    @PostMapping("/auth/changePassword")
    public AjaxResult changePassword(@RequestBody Map<String, String> params) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return AjaxResult.error("未登录");
        }
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        if (oldPassword == null || oldPassword.isEmpty()) {
            return AjaxResult.error("原密码不能为空");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return AjaxResult.error("新密码不能为空");
        }
        authService.changePassword(userId, oldPassword, newPassword);
        return AjaxResult.success();
    }
}