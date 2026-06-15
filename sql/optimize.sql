-- ============================================================
-- CloudBase Platform 数据库优化脚本
-- 1. 字段类型修正 2. 索引优化 3. 数据修正
-- 执行前请备份数据库！
-- ============================================================

-- ----------------------------
-- 1. 字段类型修正
-- ----------------------------

-- sys_user.last_login_time: VARCHAR → DATETIME
ALTER TABLE sys_user MODIFY COLUMN last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间';

-- sys_login_log.login_time: 已经是 DATETIME，确认
-- sys_oper_log.oper_time: 已经是 DATETIME，确认

-- ----------------------------
-- 2. 索引优化（高频查询字段）
-- ----------------------------

-- 用户表
ALTER TABLE sys_user ADD INDEX idx_status (status);
ALTER TABLE sys_user ADD INDEX idx_dept_id (dept_id);
ALTER TABLE sys_user ADD INDEX idx_create_time (create_time);

-- 角色表
ALTER TABLE sys_role ADD INDEX idx_status (status);

-- 菜单表
ALTER TABLE sys_menu ADD INDEX idx_parent_id (parent_id);
ALTER TABLE sys_menu ADD INDEX idx_status (status);
ALTER TABLE sys_menu ADD INDEX idx_sort (sort);

-- 部门表
ALTER TABLE sys_dept ADD INDEX idx_parent_id (parent_id);
ALTER TABLE sys_dept ADD INDEX idx_status (status);

-- 字典表
ALTER TABLE sys_dict ADD INDEX idx_dict_type (dict_type);
ALTER TABLE sys_dict ADD INDEX idx_status (status);

-- 操作日志表
ALTER TABLE sys_oper_log ADD INDEX idx_oper_time (oper_time);
ALTER TABLE sys_oper_log ADD INDEX idx_oper_user_id (oper_user_id);
ALTER TABLE sys_oper_log ADD INDEX idx_success (success);

-- 登录日志表
ALTER TABLE sys_login_log ADD INDEX idx_login_time (login_time);
ALTER TABLE sys_login_log ADD INDEX idx_user_name (user_name);
ALTER TABLE sys_login_log ADD INDEX idx_status (status);

-- 用户角色关联表
ALTER TABLE sys_user_role ADD INDEX idx_user_id (user_id);
ALTER TABLE sys_user_role ADD INDEX idx_role_id (role_id);

-- 角色菜单关联表
ALTER TABLE sys_role_menu ADD INDEX idx_role_id (role_id);
ALTER TABLE sys_role_menu ADD INDEX idx_menu_id (menu_id);

-- 参数配置表
ALTER TABLE sys_config ADD INDEX idx_config_type (config_type);

-- 通知公告表
ALTER TABLE sys_notice ADD INDEX idx_notice_type (notice_type);
ALTER TABLE sys_notice ADD INDEX idx_status (status);
ALTER TABLE sys_notice ADD INDEX idx_create_time (create_time);

-- 定时任务表
ALTER TABLE sys_job ADD INDEX idx_status (status);

-- ----------------------------
-- 3. 数据修正
-- ----------------------------

-- 将 admin 用户的 last_login_time 改为 NULL（未登录过）
UPDATE sys_user SET last_login_time = NULL WHERE user_id = 1 AND last_login_time IS NOT NULL;

-- ----------------------------
-- 4. 新增站内信消息表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_message (
    id           BIGINT        NOT NULL COMMENT '消息ID',
    title        VARCHAR(200)  NOT NULL COMMENT '消息标题',
    content      TEXT          DEFAULT NULL COMMENT '消息内容',
    msg_type     VARCHAR(20)   DEFAULT 'NOTICE' COMMENT '消息类型',
    send_type    VARCHAR(20)   DEFAULT 'ALL' COMMENT '发送方式',
    status       TINYINT       DEFAULT 1 COMMENT '状态 1-已发布 0-已撤回',
    create_time  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_msg_type (msg_type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内信消息';

CREATE TABLE IF NOT EXISTS sys_message_read (
    message_id   BIGINT        NOT NULL COMMENT '消息ID',
    user_id      BIGINT        NOT NULL COMMENT '用户ID',
    read_time    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    PRIMARY KEY (message_id, user_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息已读记录';
