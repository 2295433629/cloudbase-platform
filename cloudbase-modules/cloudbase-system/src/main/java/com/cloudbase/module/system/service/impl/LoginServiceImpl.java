package com.cloudbase.module.system.service.impl;

import com.cloudbase.common.core.exception.BusinessException;
import com.cloudbase.common.core.exception.CommonExceptionEnum;
import com.cloudbase.common.web.auth.JwtProperties;
import com.cloudbase.common.web.auth.JwtTokenUtil;
import com.cloudbase.common.web.cache.CacheService;
import com.cloudbase.module.system.entity.SysLoginLog;
import com.cloudbase.module.system.entity.SysUser;
import com.cloudbase.module.system.mapper.SysLoginLogMapper;
import com.cloudbase.module.system.mapper.SysUserMapper;
import com.cloudbase.module.system.model.vo.LoginVO;
import com.cloudbase.module.system.model.vo.UserInfoVO;
import com.cloudbase.module.system.service.LoginService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.concurrent.TimeUnit;

/**
 * 登录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final SysUserMapper sysUserMapper;
    private final SysLoginLogMapper loginLogMapper;
    private final CacheService cacheService;
    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;

    @Override
    public LoginVO login(String account, String password, String uuid, String captcha, String ip) {
        // 校验验证码
        String cachedCaptcha = cacheService.get("captcha:" + uuid);
        if (cachedCaptcha == null || !cachedCaptcha.equalsIgnoreCase(captcha)) {
            recordLoginLog(account, ip, 0, "验证码错误");
            throw new BusinessException(CommonExceptionEnum.CAPTCHA_ERROR);
        }
        cacheService.delete("captcha:" + uuid);

        // 查询用户
        SysUser user = sysUserMapper.selectOne(
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

        // SHA-256加密密码
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
        try {
            cacheService.set("login_tokens:" + token,
                    objectMapper.writeValueAsString(onlineInfo),
                    jwtProperties.getExpiration(), TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("序列化在线用户信息失败", e);
        }
        log.info("用户[{}]登录成功, token={}...", account,
                token.substring(0, Math.min(20, token.length())));

        // 构建返回
        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setUserId(user.getUserId());
        userInfo.setAccount(user.getAccount());
        userInfo.setRealName(user.getRealName());
        userInfo.setDeptId(user.getDeptId() != null ? user.getDeptId() : 0);
        userInfo.setAvatar(user.getAvatar() != null ? user.getAvatar() : "");

        LoginVO result = new LoginVO();
        result.setToken(token);
        result.setUserInfo(userInfo);
        return result;
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isBlank()) {
            log.warn("用户退出登录: token为空，忽略");
            return;
        }
        cacheService.delete("login_tokens:" + token);
        log.info("用户已退出登录, token={}...",
                token.substring(0, Math.min(20, token.length())));
    }

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

    /** SHA-256哈希 */
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
