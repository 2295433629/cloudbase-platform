package com.cloudbase.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudbase.common.core.annotation.Log;
import com.cloudbase.common.core.domain.AjaxResult;
import com.cloudbase.common.core.domain.TableDataInfo;
import com.cloudbase.common.enums.BusinessType;
import com.cloudbase.common.web.auth.UserContext;
import com.cloudbase.module.system.entity.SysMessage;
import com.cloudbase.module.system.mapper.SysMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 站内信消息管理
 */
@Validated
@RestController
@RequestMapping("/sys/message")
@RequiredArgsConstructor
public class SysMessageController {

    private final SysMessageMapper messageMapper;

    /**
     * 查询消息分页列表
     */
    @Log(title = "消息管理", businessType = BusinessType.QUERY)
    @PostMapping("/page")
    public TableDataInfo page(@RequestBody Map<String, Object> params) {
        Long userId = UserContext.getUserId();
        if (userId == null) return TableDataInfo.build(List.of(), 0);

        int pageNo = params.containsKey("pageNo") ? Integer.parseInt(params.get("pageNo").toString()) : 1;
        int pageSize = params.containsKey("pageSize") ? Integer.parseInt(params.get("pageSize").toString()) : 10;
        pageNo = Math.max(pageNo, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 100);
        String type = params.containsKey("type") ? params.get("type").toString() : null;

        // 先查总数
        LambdaQueryWrapper<SysMessage> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(SysMessage::getStatus, 1);
        if (type != null && !type.isEmpty()) {
            countWrapper.eq(SysMessage::getMsgType, type);
        }
        Long total = messageMapper.selectCount(countWrapper);

        // 查分页数据（关联已读状态）
        List<Map<String, Object>> allRecords = messageMapper.selectMessagePage(userId, type);
        int fromIndex = Math.min((pageNo - 1) * pageSize, allRecords.size());
        int toIndex = Math.min(fromIndex + pageSize, allRecords.size());
        List<Map<String, Object>> pageRecords = allRecords.subList(fromIndex, toIndex);

        return TableDataInfo.build(pageRecords, total);
    }

    /**
     * 消息详情
     */
    @Log(title = "消息管理", businessType = BusinessType.QUERY)
    @PostMapping("/detail")
    public AjaxResult detail(@RequestParam(required = false) Long id,
                             @RequestBody(required = false) Map<String, Object> body) {
        Long msgId = id;
        if (msgId == null && body != null && body.containsKey("id")) {
            msgId = Long.parseLong(body.get("id").toString());
        }
        if (msgId == null) return AjaxResult.error("缺少消息ID");
        SysMessage message = messageMapper.selectById(msgId);
        if (message == null) {
            return AjaxResult.error("消息不存在");
        }
        // 同时标记已读
        Long userId = UserContext.getUserId();
        if (userId != null) {
            messageMapper.markRead(msgId, userId);
        }
        return AjaxResult.success(message);
    }

    /**
     * 标记消息已读
     */
    @PostMapping("/read")
    public AjaxResult read(@RequestBody Map<String, Object> params) {
        Long userId = UserContext.getUserId();
        if (userId == null) return AjaxResult.error("未登录");
        Long messageId = Long.parseLong(params.get("messageId").toString());
        messageMapper.markRead(messageId, userId);
        return AjaxResult.success();
    }

    /**
     * 全部标为已读
     */
    @PostMapping("/readAll")
    public AjaxResult readAll() {
        Long userId = UserContext.getUserId();
        if (userId == null) return AjaxResult.error("未登录");
        messageMapper.markAllRead(userId);
        return AjaxResult.success();
    }

    /**
     * 获取未读消息数量
     */
    @PostMapping("/unreadCount")
    public AjaxResult unreadCount() {
        Long userId = UserContext.getUserId();
        if (userId == null) return AjaxResult.success(0);
        return AjaxResult.success(messageMapper.countUnread(userId));
    }
}
