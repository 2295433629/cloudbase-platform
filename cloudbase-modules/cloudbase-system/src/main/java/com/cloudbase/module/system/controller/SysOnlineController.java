package com.cloudbase.module.system.controller;

import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.common.web.cache.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 在线用户管理
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/sys/online")
@RequiredArgsConstructor
public class SysOnlineController {

    private final CacheService cacheService;
    private final ObjectMapper objectMapper;

    private static final String TOKEN_PREFIX = "login_tokens:";

    /**
     * 在线用户列表
     */
    @Log(title = "在线用户管理", businessType = BusinessType.QUERY)
    @PostMapping("/list")
    public AjaxResult list() {
        Set<String> keys = cacheService.keys(TOKEN_PREFIX + "*");
        log.debug("在线用户查询: keys={}", keys);
        if (keys == null || keys.isEmpty()) {
            return AjaxResult.success(Collections.emptyList());
        }

        return AjaxResult.success(keys.stream().map(key -> {
            String token = key.replace(TOKEN_PREFIX, "");
            String userJson = cacheService.get(key);
            Long expire = cacheService.getExpire(key);

            Map<String, Object> user = new HashMap<>();
            user.put("token", token);
            user.put("expire", expire != null ? expire : 0);

            if (userJson != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userInfo = objectMapper.readValue(userJson, Map.class);
                    user.putAll(userInfo);
                } catch (Exception ignored) {
                }
            }
            return user;
        }).collect(Collectors.toList()));
    }

    /**
     * 强制下线
     */
    @Log(title = "在线用户管理", businessType = BusinessType.DELETE)
    @PostMapping("/forceLogout")
    public AjaxResult forceLogout(@RequestBody Map<String, String> params) {
        String token = params.get("token");
        cacheService.delete(TOKEN_PREFIX + token);
        return AjaxResult.success();
    }
}
