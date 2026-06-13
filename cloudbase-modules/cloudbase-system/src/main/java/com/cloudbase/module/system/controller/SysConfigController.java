package com.cloudbase.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.domain.TableDataInfo;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.common.web.cache.CacheService;
import com.cloudbase.module.system.entity.SysConfig;
import com.cloudbase.module.system.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 参数配置管理
 *
 * @author ruoyi
 */
@RestController
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigMapper configMapper;
    private final CacheService cacheService;

    /**
     * 查询参数列表
     */
    @Log(title = "参数配置管理", businessType = BusinessType.QUERY)
    @PostMapping("/sys/config/page")
    public TableDataInfo page(@RequestBody Map<String, Object> params) {
        int pageNo = params.containsKey("pageNo") ? Integer.parseInt(params.get("pageNo").toString()) : 1;
        int pageSize = params.containsKey("pageSize") ? Integer.parseInt(params.get("pageSize").toString()) : 20;
        pageNo = Math.max(pageNo, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 200);

        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        if (params.containsKey("configName")) {
            wrapper.like(SysConfig::getConfigName, params.get("configName"));
        }
        wrapper.orderByAsc(SysConfig::getConfigId);

        Page<SysConfig> page = configMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return TableDataInfo.build(page.getRecords(), page.getTotal());
    }

    /**
     * 根据键名查询参数
     */
    @Log(title = "参数配置管理", businessType = BusinessType.QUERY)
    @PostMapping("/sys/config/getByKey")
    public AjaxResult getByKey(@RequestBody Map<String, String> params) {
        String key = params.get("configKey");
        if (key == null || key.isEmpty()) {
            return AjaxResult.error("参数configKey不能为空");
        }
        String cached = cacheService.get("sys:config:" + key);
        if (cached != null) {
            return AjaxResult.success(cached);
        }

        SysConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>()
                        .eq(SysConfig::getConfigKey, key));
        if (config != null) {
            cacheService.set("sys:config:" + key, config.getConfigValue());
            return AjaxResult.success(config.getConfigValue());
        }
        return AjaxResult.success();
    }

    /**
     * 新增参数
     */
    @Log(title = "参数配置管理", businessType = BusinessType.INSERT)
    @PostMapping("/sys/config/add")
    public AjaxResult add(@RequestBody SysConfig config) {
        configMapper.insert(config);
        cacheService.set("sys:config:" + config.getConfigKey(), config.getConfigValue());
        return AjaxResult.success();
    }

    /**
     * 编辑参数
     */
    @Log(title = "参数配置管理", businessType = BusinessType.UPDATE)
    @PostMapping("/sys/config/edit")
    public AjaxResult edit(@RequestBody SysConfig config) {
        // 先获取旧记录，处理 configKey 可能被修改的情况
        SysConfig oldConfig = configMapper.selectById(config.getConfigId());
        if (oldConfig == null) {
            return AjaxResult.error("参数不存在");
        }
        configMapper.updateById(config);
        // 如果 configKey 发生变更，需删除旧缓存
        if (oldConfig.getConfigKey() != null
                && !oldConfig.getConfigKey().equals(config.getConfigKey())) {
            cacheService.delete("sys:config:" + oldConfig.getConfigKey());
        }
        cacheService.set("sys:config:" + config.getConfigKey(), config.getConfigValue());
        return AjaxResult.success();
    }

    /**
     * 删除参数
     */
    @Log(title = "参数配置管理", businessType = BusinessType.DELETE)
    @PostMapping("/sys/config/delete")
    public AjaxResult delete(@RequestBody Map<String, Long> params) {
        SysConfig config = configMapper.selectById(params.get("configId"));
        if (config != null) {
            configMapper.deleteById(params.get("configId"));
            cacheService.delete("sys:config:" + config.getConfigKey());
        }
        return AjaxResult.success();
    }
}