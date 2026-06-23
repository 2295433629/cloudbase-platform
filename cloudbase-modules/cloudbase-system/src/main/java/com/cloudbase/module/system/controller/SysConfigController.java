package com.cloudbase.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.domain.PageQuery;
import com.cloudbase.common.core.domain.TableDataInfo;
import com.cloudbase.common.core.exception.BusinessException;
import com.cloudbase.common.core.exception.CommonExceptionEnum;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.common.web.cache.CacheService;
import com.cloudbase.module.system.entity.SysConfig;
import com.cloudbase.module.system.mapper.SysConfigMapper;
import com.cloudbase.module.system.model.dto.ConfigCreateDTO;
import com.cloudbase.module.system.model.dto.ConfigUpdateDTO;
import com.cloudbase.module.system.model.dto.IdDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 参数配置管理（重构后使用DTO）
 */
@Validated
@RestController
@RequestMapping("/sys/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigMapper configMapper;
    private final CacheService cacheService;

    /**
     * 查询参数列表
     */
    @Log(title = "参数配置管理", businessType = BusinessType.QUERY)
    @PostMapping("/page")
    public TableDataInfo page(@RequestBody Map<String, Object> params) {
        var pageInfo = PageQuery.of(params);

        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        if (params.containsKey("configName")) {
            wrapper.like(SysConfig::getConfigName, params.get("configName"));
        }
        wrapper.orderByAsc(SysConfig::getConfigId);

        Page<SysConfig> page = configMapper.selectPage(new Page<>(pageInfo.pageNo(), pageInfo.pageSize()), wrapper);
        return TableDataInfo.build(page.getRecords(), page.getTotal());
    }

    /**
     * 根据键名查询参数
     */
    @Log(title = "参数配置管理", businessType = BusinessType.QUERY)
    @PostMapping("/getByKey")
    public AjaxResult getByKey(@RequestBody Map<String, String> params) {
        String key = params.get("configKey");
        if (key == null || key.isEmpty()) {
            throw new BusinessException(CommonExceptionEnum.PARAM_ERROR.getErrorCode(), "参数configKey不能为空");
        }
        String cached = cacheService.get("sys:config:" + key);
        if (cached != null) {
            return AjaxResult.success(cached);
        }

        SysConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
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
    @PostMapping("/add")
    public AjaxResult add(@Valid @RequestBody ConfigCreateDTO dto) {
        SysConfig config = new SysConfig();
        config.setConfigName(dto.getConfigName());
        config.setConfigKey(dto.getConfigKey());
        config.setConfigValue(dto.getConfigValue());
        config.setConfigType(dto.getConfigType());
        config.setRemark(dto.getRemark());
        configMapper.insert(config);
        cacheService.set("sys:config:" + config.getConfigKey(), config.getConfigValue());
        return AjaxResult.success();
    }

    /**
     * 编辑参数
     */
    @Log(title = "参数配置管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    public AjaxResult edit(@Valid @RequestBody ConfigUpdateDTO dto) {
        SysConfig oldConfig = configMapper.selectById(dto.getConfigId());
        if (oldConfig == null) {
            throw new BusinessException(CommonExceptionEnum.DATA_NOT_FOUND.getErrorCode(), "参数不存在");
        }
        SysConfig config = new SysConfig();
        config.setConfigId(dto.getConfigId());
        config.setConfigName(dto.getConfigName());
        config.setConfigKey(dto.getConfigKey());
        config.setConfigValue(dto.getConfigValue());
        config.setConfigType(dto.getConfigType());
        config.setRemark(dto.getRemark());
        configMapper.updateById(config);
        // 处理缓存
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
    @PostMapping("/delete")
    public AjaxResult delete(@Valid @RequestBody IdDTO dto) {
        SysConfig config = configMapper.selectById(dto.getId());
        if (config != null) {
            configMapper.deleteById(dto.getId());
            cacheService.delete("sys:config:" + config.getConfigKey());
        }
        return AjaxResult.success();
    }
}
