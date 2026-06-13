package com.cloudbase.module.system.service;

import com.cloudbase.common.web.cache.CacheService;
import com.cloudbase.common.core.exception.BusinessException;
import com.cloudbase.common.core.exception.CommonExceptionEnum;
import com.cloudbase.common.web.auth.JwtProperties;
import com.cloudbase.common.web.auth.JwtTokenUtil;
import com.cloudbase.module.system.entity.SysLoginLog;
import com.cloudbase.module.system.entity.SysUser;
import com.cloudbase.module.system.mapper.SysLoginLogMapper;
import com.cloudbase.module.system.mapper.SysUserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wf.captcha.SpecCaptcha;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final SysLoginLogMapper loginLogMapper;
    private final CacheService cacheService;
    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;

    /** 生成验证码 */
    public Map<String, String> generateCaptcha() {
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        String uuid = UUID.randomUUID().toString();
        String captchaText = captcha.text().toLowerCase();
        cacheService.set("captcha:" + uuid, captchaText, 5, TimeUnit.MINUTES);
        return Map.of("uuid", uuid, "image", captcha.toBase64());
    }

    /** 登录 */
    public Map<String, Object> login(String account, String password, String uuid, String captcha, String ip) {
        SysUser user = null;
        try {
            // 校验验证码
            String cachedCaptcha = cacheService.get("captcha:" + uuid);
            if (cachedCaptcha == null || !cachedCaptcha.equalsIgnoreCase(captcha)) {
                recordLoginLog(account, ip, 0, "验证码错误");
                throw new BusinessException(CommonExceptionEnum.CAPTCHA_ERROR);
            }
            cacheService.delete("captcha:" + uuid);

            // 查询用户
            user = sysUserMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getAccount, account)
            );
            if (user == null) {
                recordLoginLog(account, ip, 0, "账号或密码错误");
                throw new BusinessException("账号或密码错误");
            }
            if (user.getStatus() == 0) {
                recordLoginLog(account, ip, 0, "账号已被禁用");
                throw new BusinessException(CommonExceptionEnum.ACCOUNT_DISABLED);
            }

            // SHA-256加密密码（密码+账号作为盐值）
            String encryptedPassword = sha256Hex(password + user.getAccount());
            if (!encryptedPassword.equalsIgnoreCase(user.getPassword())) {
                recordLoginLog(account, ip, 0, "密码错误");
                throw new BusinessException("账号或密码错误");
            }

            // 记录登录成功
            recordLoginLog(account, ip, 1, "登录成功");

            // 更新最后登录信息
            SysUser updateLogin = new SysUser();
            updateLogin.setUserId(user.getUserId());
            updateLogin.setLastLoginIp(ip);
            updateLogin.setLastLoginTime(LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            sysUserMapper.updateById(updateLogin);

            // 生成Token
            String token = JwtTokenUtil.generateToken(
                    Map.of("userId", user.getUserId(), "username", user.getRealName()),
                    jwtProperties.getSecret(),
                    jwtProperties.getExpiration()
            );

            // 存储在线用户到缓存
            Map<String, Object> onlineInfo = new HashMap<>();
            onlineInfo.put("userId", user.getUserId());
            onlineInfo.put("account", user.getAccount());
            onlineInfo.put("realName", user.getRealName());
            onlineInfo.put("deptId", user.getDeptId());
            onlineInfo.put("loginTime", new Date().getTime());
            onlineInfo.put("ipAddress", ip);
            String onlineInfoJson;
            try {
                onlineInfoJson = objectMapper.writeValueAsString(onlineInfo);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                throw new RuntimeException("序列化在线用户信息失败", e);
            }
            cacheService.set(
                    "login_tokens:" + token,
                    onlineInfoJson,
                    jwtProperties.getExpiration(),
                    TimeUnit.SECONDS
            );
            log.info("在线用户缓存已存储: login_tokens:{}", token.substring(0, Math.min(20, token.length())) + "...");

            return Map.of(
                    "token", token,
                    "userInfo", Map.of(
                            "userId", user.getUserId(),
                            "account", user.getAccount(),
                            "realName", user.getRealName(),
                            "deptId", user.getDeptId() != null ? user.getDeptId() : 0,
                            "avatar", user.getAvatar() != null ? user.getAvatar() : ""
                    )
            );
        } finally {
            // 登录流程结束，无需额外清理
        }
    }

    /**
     * 记录登录日志（同步，@Async 在同类内部调用时不走代理）
     * loginId 由 MyBatis-Plus ASSIGN_ID（雪花算法）自动生成
     */
    void recordLoginLog(String account, String ip, int status, String msg) {
        try {
            SysLoginLog logEntry = new SysLoginLog();
            logEntry.setUserName(account);
            logEntry.setIpAddress(ip);
            logEntry.setStatus(status);
            logEntry.setMsg(msg);
            logEntry.setLoginTime(new Date());
            loginLogMapper.insert(logEntry);
        } catch (Exception e) {
            log.error("记录登录日志失败", e);
        }
    }

    /** 退出登录 */
    public void logout(String token) {
        cacheService.delete("login_tokens:" + token);
    }

    // ==================== 个人中心 ====================

    /**
     * 获取当前用户个人信息
     */
    public SysUser getProfile(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null); // 不返回密码
        return user;
    }

    /**
     * 修改个人基本信息（姓名、手机、邮箱、头像）
     */
    public void updateProfile(Long userId, String realName, String phone, String email, String avatar) {
        SysUser user = new SysUser();
        user.setUserId(userId);
        if (realName != null) user.setRealName(realName);
        if (phone != null) user.setPhone(phone);
        if (email != null) user.setEmail(email);
        if (avatar != null) user.setAvatar(avatar);
        sysUserMapper.updateById(user);
    }

    /**
     * 修改密码
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 验证旧密码
        String oldHash = sha256Hex(oldPassword + user.getAccount());
        if (!oldHash.equalsIgnoreCase(user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("新密码长度不能少于6位");
        }
        // 更新密码
        String newHash = sha256Hex(newPassword + user.getAccount());
        SysUser update = new SysUser();
        update.setUserId(userId);
        update.setPassword(newHash);
        sysUserMapper.updateById(update);
    }

    /** SHA-256哈希（公开方法，供用户管理等场景复用） */
    public static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256加密失败", e);
        }
    }
}
