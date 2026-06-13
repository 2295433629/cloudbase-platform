# CloudBase 基础平台

基于 **Spring Boot 3 + Vue 3** 的企业级后台管理系统，提供完整的用户认证、权限管理、系统监控等功能。

## 技术栈

| 层次 | 技术 | 版本 |
|---|---|---|
| **后端框架** | Spring Boot | 3.4.3 |
| **ORM** | MyBatis-Plus | 3.5.9 |
| **数据库** | MySQL | 9.2 |
| **连接池** | Druid | 1.2.24 |
| **认证** | JWT (jjwt) | 0.12.6 |
| **缓存** | Redis / 内存缓存（可配置切换） | — |
| **API文档** | Knife4j (OpenAPI 3) | 4.5.0 |
| **前端框架** | Vue 3 + Vite | — |
| **UI组件** | Element Plus | — |
| **状态管理** | Pinia | — |
| **工具库** | Hutool / FastJSON2 / EasyExcel | — |

## 项目结构

```
cloudbase-platform/
├── cloudbase-admin/                    # 启动模块（Application入口 + 配置文件）
│   └── src/main/resources/
│       └── application.yml             # 全局配置
│
├── cloudbase-common/                   # 公共模块
│   ├── cloudbase-common-core/          #   核心层：注解、异常、领域模型、枚举、工具类
│   └── cloudbase-common-web/           #   Web层：拦截器、缓存、JWT、全局异常处理、MyBatis配置
│
├── cloudbase-modules/
│   └── cloudbase-system/               # 系统管理模块（业务核心）
│       ├── controller/                 #   控制器层
│       ├── service/                    #   业务逻辑层
│       ├── mapper/                     #   数据访问层
│       ├── entity/                     #   实体类
│       ├── aspect/                     #   AOP切面（操作日志、数据权限）
│       └── quartz/                     #   定时任务框架
│
├── cloudbase-web/                      # 前端项目（Vue 3）
│   ├── src/
│   │   ├── api/                        #   API接口封装
│   │   ├── layout/                     #   全局布局（侧边栏 + 顶部栏）
│   │   ├── views/                      #   页面组件
│   │   ├── router/                     #   路由配置
│   │   ├── stores/                     #   Pinia状态管理
│   │   └── utils/                      #   工具（Axios封装等）
│   └── package.json
│
├── pom.xml                             # Maven父POM
└── .gitignore
```

## 功能模块

### 认证与权限
- 用户登录 / 退出（JWT Token + 图形验证码）
- 接口级 JWT 强制鉴权（AuthInterceptor）
- 个人中心（查看/修改个人信息、修改密码）

### 系统管理
- **用户管理** — 用户CRUD、状态启停、角色分配
- **角色管理** — 角色CRUD、菜单权限分配
- **菜单管理** — 树形菜单CRUD、状态管理
- **部门管理** — 树形部门CRUD、状态管理
- **字典管理** — 数据字典CRUD、按类型查询

### 系统监控
- **操作日志** — 全量接口操作日志记录（AOP自动采集）
- **登录日志** — 登录成功/失败日志
- **在线用户** — 在线用户列表、强制下线
- **服务监控** — CPU、内存、磁盘、JVM 实时信息

### 系统工具
- **参数配置** — 系统参数CRUD（带缓存）
- **通知公告** — 通知/公告管理
- **定时任务** — 基于 Quartz 的任务调度
- **代码生成** — 根据数据库表自动生成前后端代码

## 缓存架构

支持 **Redis** 和 **内存缓存** 两种模式，通过配置切换：

```yaml
cloudbase:
  cache:
    type: redis    # 或 memory（无Redis环境时使用）
```

- `CacheService` 统一接口，业务代码无感知切换
- 内存模式基于 `ConcurrentHashMap`，支持过期清理

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Node.js 18+
- Maven 3.8+
- Redis（可选，可切换为内存缓存）

### 后端启动

```bash
# 1. 创建数据库并导入SQL脚本
mysql -u root -p < sql/cloudbase.sql

# 2. 修改配置
# 编辑 cloudbase-admin/src/main/resources/application.yml
# 修改数据库连接、Redis等配置

# 3. 编译并启动
mvn clean install -DskipTests
cd cloudbase-admin
mvn spring-boot:run
```

### 前端启动

```bash
cd cloudbase-web

# 安装依赖
npm install

# 开发模式启动
npm run dev
```

### 访问

- 前端地址：http://localhost:5173
- 后端API：http://localhost:8080
- API文档：http://localhost:8080/doc.html
- 默认账号：`admin`

## 设计规范

- **响应格式**：统一使用 `AjaxResult {code, msg, data}` 和 `TableDataInfo {code, msg, rows, total}`
- **接口风格**：全部使用 `@PostMapping`
- **主键策略**：雪花算法 `@TableId(type = IdType.ASSIGN_ID)`
- **操作日志**：通过 `@Log` 注解 + AOP 自动采集
- **密码加密**：SHA-256（密码 + 账号作为盐值）
