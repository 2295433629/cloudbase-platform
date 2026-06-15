-- =====================================================
-- CloudBase Platform 初始化SQL
-- Database: cloudbase, Charset: utf8mb4
-- =====================================================

-- CREATE DATABASE IF NOT EXISTS cloudbase DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE cloudbase;

-- ----------------------------
-- 系统用户表
-- ----------------------------
-- ----------------------------
-- 系统部门表
-- ----------------------------
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    dept_id     BIGINT       NOT NULL COMMENT '部门ID',
    parent_id   BIGINT       DEFAULT 0 COMMENT '父部门ID',
    ancestors   VARCHAR(500) DEFAULT '' COMMENT '祖级列表(逗号分隔)',
    dept_name   VARCHAR(50)  NOT NULL COMMENT '部门名称',
    sort        INT          DEFAULT 0 COMMENT '排序',
    leader      VARCHAR(50)  DEFAULT NULL COMMENT '负责人',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    email       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    status      TINYINT      DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT       DEFAULT NULL COMMENT '创建人',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_user BIGINT       DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统部门';

DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    user_id       BIGINT        NOT NULL COMMENT '用户ID',
    dept_id       BIGINT        DEFAULT NULL COMMENT '所属部门ID',
    account       VARCHAR(50)   NOT NULL COMMENT '账号',
    password      VARCHAR(200)  NOT NULL COMMENT '密码',
    real_name     VARCHAR(50)   DEFAULT NULL COMMENT '姓名',
    phone         VARCHAR(20)   DEFAULT NULL COMMENT '手机号',
    email         VARCHAR(100)  DEFAULT NULL COMMENT '邮箱',
    avatar        VARCHAR(500)  DEFAULT NULL COMMENT '头像',
    status        TINYINT       DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    last_login_ip VARCHAR(50)   DEFAULT NULL COMMENT '最后登录IP',
    last_login_time VARCHAR(50) DEFAULT NULL COMMENT '最后登录时间',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user   BIGINT        DEFAULT NULL COMMENT '创建人',
    update_time   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_user   BIGINT        DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_account (account)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

-- ----------------------------
-- 系统角色表
-- ----------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    role_id     BIGINT       NOT NULL COMMENT '角色ID',
    role_name   VARCHAR(50)  NOT NULL COMMENT '角色名称',
    role_code   VARCHAR(50)  NOT NULL COMMENT '角色编码',
    data_scope  TINYINT      DEFAULT 1 COMMENT '数据权限 1=全部 2=自定义 3=本部门 4=本部门及以下 5=仅本人',
    sort        INT          DEFAULT 0 COMMENT '排序',
    status      TINYINT      DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    remark      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT       DEFAULT NULL COMMENT '创建人',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_user BIGINT       DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色';

-- ----------------------------
-- 系统菜单表
-- ----------------------------
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    menu_id     BIGINT        NOT NULL COMMENT '菜单ID',
    parent_id   BIGINT        DEFAULT 0 COMMENT '父菜单ID',
    menu_name   VARCHAR(100)  NOT NULL COMMENT '菜单名称',
    menu_type   TINYINT       DEFAULT 2 COMMENT '类型 1-目录 2-菜单 3-按钮',
    path        VARCHAR(200)  DEFAULT NULL COMMENT '路由路径',
    component   VARCHAR(200)  DEFAULT NULL COMMENT '组件路径',
    perms       VARCHAR(100)  DEFAULT NULL COMMENT '权限标识',
    icon        VARCHAR(100)  DEFAULT NULL COMMENT '图标',
    sort        INT           DEFAULT 0 COMMENT '排序',
    hidden      TINYINT       DEFAULT 0 COMMENT '是否隐藏 0-否 1-是',
    status      TINYINT       DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT        DEFAULT NULL COMMENT '创建人',
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_user BIGINT        DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单';

-- ----------------------------
-- 用户角色关联表
-- ----------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

-- ----------------------------
-- 角色菜单关联表
-- ----------------------------
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联';

-- ----------------------------
-- 系统字典表
-- ----------------------------
DROP TABLE IF EXISTS sys_dict;
CREATE TABLE sys_dict (
    dict_id     BIGINT        NOT NULL COMMENT '字典ID',
    dict_type   VARCHAR(50)   NOT NULL COMMENT '字典类型',
    dict_label  VARCHAR(100)  NOT NULL COMMENT '字典标签',
    dict_value  VARCHAR(100)  NOT NULL COMMENT '字典值',
    sort        INT           DEFAULT 0 COMMENT '排序',
    status      TINYINT       DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    remark      VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT        DEFAULT NULL COMMENT '创建人',
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_user BIGINT        DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (dict_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统字典';

-- ----------------------------
-- 操作日志表
-- ----------------------------
DROP TABLE IF EXISTS sys_oper_log;
CREATE TABLE sys_oper_log (
    log_id          BIGINT        NOT NULL COMMENT '日志ID',
    module          VARCHAR(50)   DEFAULT NULL COMMENT '操作模块',
    oper_type       VARCHAR(50)   DEFAULT NULL COMMENT '操作类型',
    method          VARCHAR(200)  DEFAULT NULL COMMENT '操作方法',
    request_param   TEXT          DEFAULT NULL COMMENT '请求参数',
    response_result TEXT          DEFAULT NULL COMMENT '响应结果',
    oper_user_id    BIGINT        DEFAULT NULL COMMENT '操作人ID',
    oper_user_name  VARCHAR(50)   DEFAULT NULL COMMENT '操作人名称',
    oper_ip         VARCHAR(50)   DEFAULT NULL COMMENT '操作IP',
    oper_time       DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    cost_time       BIGINT        DEFAULT NULL COMMENT '耗时(ms)',
    success         TINYINT       DEFAULT 1 COMMENT '是否成功 1-成功 0-失败',
    PRIMARY KEY (log_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

-- ----------------------------
-- 登录日志表
-- ----------------------------
DROP TABLE IF EXISTS sys_login_log;
CREATE TABLE sys_login_log (
    login_id      BIGINT        NOT NULL COMMENT '登录日志ID',
    user_name     VARCHAR(50)   DEFAULT NULL COMMENT '用户名称',
    ip_address    VARCHAR(50)   DEFAULT NULL COMMENT '登录IP',
    browser       VARCHAR(100)  DEFAULT NULL COMMENT '浏览器',
    os            VARCHAR(100)  DEFAULT NULL COMMENT '操作系统',
    status        TINYINT       DEFAULT 1 COMMENT '状态 1-成功 0-失败',
    msg           VARCHAR(500)  DEFAULT NULL COMMENT '提示消息',
    login_time    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (login_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志';

-- ----------------------------
-- 参数配置表
-- ----------------------------
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
    config_id    BIGINT        NOT NULL COMMENT '配置ID',
    config_name  VARCHAR(100)  DEFAULT NULL COMMENT '参数名称',
    config_key   VARCHAR(100)  NOT NULL COMMENT '参数键名',
    config_value VARCHAR(500)  DEFAULT NULL COMMENT '参数键值',
    config_type  TINYINT       DEFAULT 1 COMMENT '系统内置 1-是 0-否',
    remark       VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    create_time  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user  BIGINT        DEFAULT NULL COMMENT '创建人',
    update_time  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_user  BIGINT        DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (config_id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数配置';

-- ----------------------------
-- 通知公告表
-- ----------------------------
DROP TABLE IF EXISTS sys_notice;
CREATE TABLE sys_notice (
    notice_id     BIGINT        NOT NULL COMMENT '公告ID',
    notice_title  VARCHAR(100)  NOT NULL COMMENT '公告标题',
    notice_type   TINYINT       DEFAULT 1 COMMENT '类型 1-通知 2-公告',
    notice_content TEXT         DEFAULT NULL COMMENT '公告内容',
    status        TINYINT       DEFAULT 1 COMMENT '状态 1-正常 0-关闭',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user   BIGINT        DEFAULT NULL COMMENT '创建人',
    update_time   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_user   BIGINT        DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (notice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知公告';

-- ----------------------------
-- 初始化数据：超级管理员
-- 密码: 123456 (SHA2(CONCAT(password, account), 256))
-- ----------------------------
INSERT INTO sys_user (user_id, account, password, real_name, status)
VALUES (1, 'admin', SHA2(CONCAT('123456', 'admin'), 256), '超级管理员', 1);

-- ----------------------------
-- 初始化菜单（与前端 Sidebar.vue / router 保持一致）
-- menu_type: 1-目录 2-菜单 3-按钮
-- ----------------------------
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, status) VALUES
-- ===== 目录：系统管理 =====
(1,  0,  '系统管理',   1, '/system',            NULL,                         NULL,                'Setting',        1, 1),
(2,  1,  '用户管理',   2, '/system/user',       'system/user/index',          NULL,                'User',           1, 1),
(3,  1,  '角色管理',   2, '/system/role',       'system/role/index',          NULL,                'UserFilled',     2, 1),
(4,  1,  '菜单管理',   2, '/system/menu',       'system/menu/index',          NULL,                'Menu',           3, 1),
(5,  1,  '字典管理',   2, '/system/dict',       'system/dict/index',          NULL,                'Notebook',       4, 1),
(6,  1,  '部门管理',   2, '/system/dept',       'system/dept/index',          NULL,                'OfficeBuilding', 5, 1),
-- 用户管理按钮
(7,  2,  '用户新增',   3, NULL, NULL, 'sys:user:add',    NULL, 1, 1),
(8,  2,  '用户编辑',   3, NULL, NULL, 'sys:user:edit',   NULL, 2, 1),
(9,  2,  '用户删除',   3, NULL, NULL, 'sys:user:delete', NULL, 3, 1),
-- 角色管理按钮
(10, 3,  '角色新增',   3, NULL, NULL, 'sys:role:add',    NULL, 1, 1),
(11, 3,  '角色编辑',   3, NULL, NULL, 'sys:role:edit',   NULL, 2, 1),
(12, 3,  '角色删除',   3, NULL, NULL, 'sys:role:delete', NULL, 3, 1),
-- 菜单管理按钮
(13, 4,  '菜单新增',   3, NULL, NULL, 'sys:menu:add',    NULL, 1, 1),
(14, 4,  '菜单编辑',   3, NULL, NULL, 'sys:menu:edit',   NULL, 2, 1),
(15, 4,  '菜单删除',   3, NULL, NULL, 'sys:menu:delete', NULL, 3, 1),
-- 字典管理按钮
(16, 5,  '字典新增',   3, NULL, NULL, 'sys:dict:add',    NULL, 1, 1),
(17, 5,  '字典编辑',   3, NULL, NULL, 'sys:dict:edit',   NULL, 2, 1),
(18, 5,  '字典删除',   3, NULL, NULL, 'sys:dict:delete', NULL, 3, 1),
-- 部门管理按钮
(19, 6,  '部门新增',   3, NULL, NULL, 'sys:dept:add',    NULL, 1, 1),
(20, 6,  '部门编辑',   3, NULL, NULL, 'sys:dept:edit',   NULL, 2, 1),
(21, 6,  '部门删除',   3, NULL, NULL, 'sys:dept:delete', NULL, 3, 1),
-- ===== 目录：系统监控 =====
(30, 0,  '系统监控',   1, '/system/monitor',    NULL,                         NULL,                'Monitor',        2, 1),
(31, 30, '操作日志',   2, '/system/operlog',    'system/operlog/index',       NULL,                'Document',       1, 1),
(32, 30, '登录日志',   2, '/system/loginlog',   'system/loginlog/index',      NULL,                'Lock',           2, 1),
(33, 30, '在线用户',   2, '/system/online',     'system/online/index',        NULL,                'Connection',     3, 1),
(34, 30, '服务监控',   2, '/system/monitor/server', 'system/monitor/server/index', NULL,           'Cpu',            4, 1),
-- ===== 目录：系统工具 =====
(40, 0,  '系统工具',   1, '/system/tools',      NULL,                         NULL,                'Tools',          3, 1),
(41, 40, '参数配置',   2, '/system/config',     'system/config/index',        NULL,                'SetUp',          1, 1),
(42, 40, '通知公告',   2, '/system/notice',     'system/notice/index',        NULL,                'Bell',           2, 1),
(43, 40, '定时任务',   2, '/system/job',        'system/job/index',           NULL,                'Clock',          3, 1),
(44, 40, '代码生成',   2, '/system/gen',        'system/gen/index',           NULL,                'MagicStick',     4, 1),
-- 通知公告按钮
(45, 42, '公告新增',   3, NULL, NULL, 'sys:notice:add',    NULL, 1, 1),
(46, 42, '公告编辑',   3, NULL, NULL, 'sys:notice:edit',   NULL, 2, 1),
(47, 42, '公告删除',   3, NULL, NULL, 'sys:notice:delete', NULL, 3, 1);

-- ----------------------------
-- 初始化角色
-- ----------------------------
INSERT INTO sys_role (role_id, role_name, role_code, data_scope, sort, status) VALUES
(1, '超级管理员', 'admin', 1, 1, 1);

-- 关联管理员角色（user_id=1 → role_id=1）
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 超级管理员关联所有菜单
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu;

-- 初始化部门
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, sort, status) VALUES
(100, 0, '0', '总公司', 1, 1),
(101, 100, '0,100', '研发部', 1, 1),
(102, 100, '0,100', '产品部', 2, 1);

-- 初始化参数配置
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, remark) VALUES
(1, '用户初始化密码', 'sys.user.initPassword', '123456', 1, '新建用户时的初始密码'),
(2, '验证码开关', 'sys.captcha.enable', 'true', 1, '是否启用登录验证码');

-- ==================== 定时任务 ====================
DROP TABLE IF EXISTS sys_job;
CREATE TABLE sys_job (
    job_id          BIGINT          NOT NULL COMMENT '任务ID',
    job_name        VARCHAR(64)     NOT NULL COMMENT '任务名称',
    job_group       VARCHAR(64)     DEFAULT 'DEFAULT' COMMENT '任务组名',
    invoke_target   VARCHAR(500)    NOT NULL COMMENT '调用目标字符串',
    cron_expression VARCHAR(255)    DEFAULT '' COMMENT 'cron表达式',
    status          TINYINT         DEFAULT 1 COMMENT '状态 1-正常 0-暂停',
    concurrent      TINYINT         DEFAULT 1 COMMENT '是否并发 1-允许 0-禁止',
    remark          VARCHAR(500)    DEFAULT '' COMMENT '备注',
    create_time     DATETIME        COMMENT '创建时间',
    create_user     BIGINT          COMMENT '创建人',
    update_time     DATETIME        COMMENT '更新时间',
    update_user     BIGINT          COMMENT '更新人',
    PRIMARY KEY (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务表';

DROP TABLE IF EXISTS sys_job_log;
CREATE TABLE sys_job_log (
    log_id          BIGINT          NOT NULL COMMENT '日志ID',
    job_name        VARCHAR(64)     NOT NULL COMMENT '任务名称',
    job_group       VARCHAR(64)     DEFAULT 'DEFAULT' COMMENT '任务组名',
    invoke_target   VARCHAR(500)    DEFAULT '' COMMENT '调用目标',
    status          TINYINT         DEFAULT 1 COMMENT '执行状态 1-成功 0-失败',
    exception_info  VARCHAR(2000)   DEFAULT '' COMMENT '异常信息',
    start_time      DATETIME        COMMENT '开始时间',
    end_time        DATETIME        COMMENT '结束时间',
    cost_time       BIGINT          DEFAULT 0 COMMENT '耗时(ms)',
    PRIMARY KEY (log_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务执行日志表';

-- ==================== 代码生成 ====================
DROP TABLE IF EXISTS gen_table;
CREATE TABLE gen_table (
    table_id        BIGINT          NOT NULL COMMENT '编号',
    table_name      VARCHAR(64)     COMMENT '表名',
    table_comment   VARCHAR(200)    COMMENT '表描述',
    class_name      VARCHAR(100)    COMMENT '实体类名',
    package_name    VARCHAR(200)    COMMENT '包名',
    module_name     VARCHAR(30)     COMMENT '模块名',
    business_name   VARCHAR(30)     COMMENT '业务名',
    function_name   VARCHAR(50)     COMMENT '功能名',
    PRIMARY KEY (table_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码生成业务表';

DROP TABLE IF EXISTS gen_table_column;
CREATE TABLE gen_table_column (
    column_id       BIGINT          NOT NULL COMMENT '编号',
    table_id        BIGINT          COMMENT '归属表编号',
    column_name     VARCHAR(64)     COMMENT '列名',
    column_comment  VARCHAR(500)    COMMENT '列描述',
    java_field      VARCHAR(200)    COMMENT 'Java字段名',
    java_type       VARCHAR(200)    COMMENT 'Java类型',
    is_pk           TINYINT         DEFAULT 0 COMMENT '是否主键 1-是',
    is_required     TINYINT         DEFAULT 0 COMMENT '是否必填',
    is_insert       TINYINT         DEFAULT 1 COMMENT '是否插入',
    is_edit         TINYINT         DEFAULT 1 COMMENT '是否编辑',
    is_list         TINYINT         DEFAULT 1 COMMENT '是否列表',
    is_query        TINYINT         DEFAULT 1 COMMENT '是否查询',
    query_type      VARCHAR(200)    DEFAULT '=' COMMENT '查询方式',
    html_type       VARCHAR(200)    DEFAULT 'input' COMMENT '显示类型',
    sort            INT             DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (column_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码生成业务表字段';

-- ----------------------------
-- 站内信消息表
-- ----------------------------
DROP TABLE IF EXISTS sys_message;
CREATE TABLE sys_message (
    id           BIGINT        NOT NULL COMMENT '消息ID',
    title        VARCHAR(200)  NOT NULL COMMENT '消息标题',
    content      TEXT          DEFAULT NULL COMMENT '消息内容',
    msg_type     VARCHAR(20)   DEFAULT 'NOTICE' COMMENT '消息类型 NOTICE-通知 ANNOUNCEMENT-公告',
    send_type    VARCHAR(20)   DEFAULT 'ALL' COMMENT '发送方式 ALL-全部',
    status       TINYINT       DEFAULT 1 COMMENT '状态 1-已发布 0-已撤回',
    create_time  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_msg_type (msg_type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内信消息';

-- ----------------------------
-- 消息已读记录表
-- ----------------------------
DROP TABLE IF EXISTS sys_message_read;
CREATE TABLE sys_message_read (
    message_id   BIGINT        NOT NULL COMMENT '消息ID',
    user_id      BIGINT        NOT NULL COMMENT '用户ID',
    read_time    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    PRIMARY KEY (message_id, user_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息已读记录';

-- ----------------------------
-- 初始化示例消息
-- ----------------------------
INSERT INTO sys_message (id, title, content, msg_type, send_type, status) VALUES
(1, '欢迎使用 CloudBase 平台', '欢迎使用 CloudBase 基础平台，这是一款企业级管理系统。', 'NOTICE', 'ALL', 1),
(2, '系统维护通知', '系统将于本周六凌晨2点进行例行维护，届时服务将暂停约30分钟。', 'ANNOUNCEMENT', 'ALL', 1);

-- 初始化示例定时任务
INSERT INTO sys_job (job_id, job_name, invoke_target, cron_expression, status) VALUES
(1, '每分钟测试', 'com.cloudbase.module.system.service.impl.NoOpJob.execute("hello")', '0 */1 * * * ?', 0);
