# 基础底座能力补充方案

## 调研来源

分析 GitHub/Gitee 上最优秀的三个开源后台管理平台：

| 项目 | Stars | 技术栈 | 核心亮点 |
|------|-------|--------|----------|
| **RuoYi-Vue3** | 最经典 | SpringBoot + Security + Vue3 | 5级数据权限、注解驱动(@DataScope/@Log)、OSHI服务器监控、Quartz定时任务、代码生成器 |
| **Sz-Admin** | 最现代 | SpringBoot 4 + JDK21 + Sa-Token | 3层模块边界(sz-common→module→service)、MyBatis-Flex、Liquibase、Docker部署、OSS文件管理 |
| **cjbi/admin3** | 最轻量 | SpringBoot 3.2 + Java21 + Vue3+TS | 极简设计、只保留登录+RBAC核心 |

## 补充能力清单（按优先级）

### P0 - 核心缺失（立即补充）

**1. 部门管理（Department Tree）**
- 原因：目前只有用户-角色-菜单三级，缺少组织结构维度
- 方案：
  - 新增 `sys_dept` 表（dept_id, parent_id, ancestors, dept_name, sort, leader, phone, status）
  - ancestors 字段存储祖先路径（如 `0,100,200`），用 `find_in_set` 快速查找子部门
  - 前端使用 Element Plus `el-tree` 组件渲染
  - 用户表新增 `dept_id` 外键关联所属部门

**2. 数据权限（@DataScope + AOP）**
- 原因：企业级后台的核心能力，不同角色看不同数据
- 方案：
  - 角色表新增 `data_scope` 字段（1=全部, 2=自定义, 3=本部门, 4=本部门及以下, 5=仅本人）
  - 新增 `sys_role_dept` 关联表（角色-部门）
  - 自定义 `@DataScope` 注解
  - `DataScopeAspect` AOP切面，在SQL执行前注入 `params.dataScope` 过滤条件
  - SQL中用 `${params.dataScope}` 占位符拼接

**3. 操作日志（@Log + AOP + 异步存储）**
- 原因：审计追踪是基础底座必须能力
- 方案：
  - 自定义 `@OperLog` 注解（module, operType, businessType）
  - `OperLogAspect` 切面：用Spring AOP环绕通知，记录IP/参数/耗时/结果
  - 使用 `@Async` 异步写入，不阻塞主流程
  - 前端提供日志查询页面（按模块/类型/时间/操作人筛选）

**4. 登录日志**
- 原因：安全审计，记录每次登录成功/失败
- 方案：`sys_login_log` 表（login_id, user_name, ip_address, browser, os, status, msg, login_time）
  - 在 AuthService.login() 中记录

### P1 - 重要增强（本次补充）

**5. 在线用户管理**
- 原因：活跃用户监控+强踢下线
- 方案：Redis存储在线用户信息（key=`login_tokens:{token}`），`/sys/online/list` 查询、`/sys/online/forceLogout` 强制下线

**6. 参数/配置管理**
- 原因：系统动态配置（如开关功能、修改阈值），不需重启
- 方案：`sys_config` 表，Redis缓存，`@Config` 方式注入

**7. 通知公告**
- 原因：系统级消息公告
- 方案：`sys_notice` 表，支持已读/未读状态

### P2 - 增强功能（后续补充）

**8. 服务监控（OSHI）**
- 方案：集成 OSHI 库，获取CPU/内存/JVM/磁盘/网络信息，实时刷新

**9. 定时任务（Quartz）**
- 方案：内置任务调度管理，支持在线增删改+执行日志

**10. 代码生成器**
- 方案：Velocity/FreeMarker模板引擎，根据表结构生成 Controller/Service/Mapper/Entity/Vue 文件

**11. 防重复提交 + 数据脱敏 + 接口限流**
- 方案：@RepeatSubmit(Redis锁)、@Sensitive(脱敏)、@RateLimiter(令牌桶)

## 实施计划
本次实施 P0（部门管理+数据权限+操作日志+登录日志）+ P1（在线用户+参数管理+通知公告），共约15张表的扩展。
