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
-- 密码: admin123 (SHA2(CONCAT(password, account), 256))
-- ----------------------------
INSERT INTO sys_user (user_id, account, password, real_name, status)
VALUES (1, 'admin', SHA2(CONCAT('admin123', 'admin'), 256), '超级管理员', 1);

-- ----------------------------
-- 初始化菜单
-- ----------------------------
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, icon, sort, status) VALUES
(1,  0,  '系统管理', 1, '/system',     NULL,            'Setting', 1, 1),
(2,  1,  '用户管理', 2, '/system/user', 'system/user/index', 'User', 1, 1),
(3,  1,  '角色管理', 2, '/system/role', 'system/role/index', 'UserFilled', 2, 1),
(4,  1,  '菜单管理', 2, '/system/menu', 'system/menu/index', 'Menu', 3, 1),
(5,  1,  '字典管理', 2, '/system/dict', 'system/dict/index', 'Notebook', 4, 1),
(6,  2,  '用户新增', 3, NULL, NULL, 'sys:user:add', 1, 1),
(7,  2,  '用户编辑', 3, NULL, NULL, 'sys:user:edit', 2, 1),
(8,  2,  '用户删除', 3, NULL, NULL, 'sys:user:delete', 3, 1),
(9,  1,  '部门管理', 2, '/system/dept', 'system/dept/index', 'OfficeBuilding', 5, 1),
(10, 1,  '操作日志', 2, '/system/operlog', 'system/operlog/index', 'Document', 6, 1),
(11, 1,  '登录日志', 2, '/system/loginlog', 'system/loginlog/index', 'Lock', 7, 1),
(12, 1,  '在线用户', 2, '/system/online', 'system/online/index', 'Connection', 8, 1),
(13, 1,  '参数配置', 2, '/system/config', 'system/config/index', 'Tools', 9, 1),
(14, 1,  '通知公告', 2, '/system/notice', 'system/notice/index', 'Bell', 10, 1),
(15, 14, '公告新增', 3, NULL, NULL, 'sys:notice:add', 1, 1),
(16, 14, '公告编辑', 3, NULL, NULL, 'sys:notice:edit', 2, 1);

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

-- 初始化示例定时任务
INSERT INTO sys_job (job_id, job_name, invoke_target, cron_expression, status) VALUES
(1, '每分钟测试', 'com.cloudbase.module.system.service.impl.NoOpJob.execute("hello")', '0 */1 * * * ?', 0);
