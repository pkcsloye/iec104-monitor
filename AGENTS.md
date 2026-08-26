# AGENTS.md

> 本文件为 AI 开发助手提供项目上下文与开发规范，请在修改代码前始终参考此文件。

---

## 项目概述

**项目名称**：若依管理系统（EMS 104）  
**版本**：3.9.2  
**架构**：前后端分离  
**仓库根路径**：`d:\idea_workspace\RuoYi-Vue-20260825`

| 层级 | 技术栈 |
|------|--------|
| 前端 | Vue 3.5 + Vite 6.4 + Element Plus 2.13 + Pinia 3.0 + Vue Router 4.6 |
| 后端 | Java 17 + Spring Boot 4.1 + MyBatis 4.1 + Spring Security |
| 数据库 | MySQL（阿里云 RDS） |
| 缓存 | Redis（Lettuce 连接池） |
| 连接池 | Druid 1.2.28 |
| 构建工具 | Maven（后端）、Yarn/Vite（前端） |

---

## 目录结构

```
RuoYi-Vue-20260825/
├── ruoyi-admin/          # Web 服务入口（Controller 层、启动类）
├── ruoyi-framework/      # 框架核心（安全、拦截器、AOP、数据源、异常处理）
├── ruoyi-system/         # 系统业务模块（domain/mapper/service）
├── ruoyi-quartz/         # 定时任务模块
├── ruoyi-generator/      # 代码生成模块
├── ruoyi-common/         # 通用工具（注解、常量、异常、工具类）
├── RuoYi-Vue3/           # 前端工程（Vue 3 + Vite）
├── sql/                  # 数据库初始化脚本
├── bin/                  # 构建/运行脚本
└── pom.xml               # Maven 父 POM
```

---

## 后端结构

### 模块依赖关系

```
ruoyi-admin
  ├── ruoyi-framework
  │     └── ruoyi-system
  │           └── ruoyi-common
  ├── ruoyi-quartz
  │     └── ruoyi-common
  └── ruoyi-generator
        └── ruoyi-common
```

### 各模块职责

#### `ruoyi-admin`（Web 入口）
- 启动类：`com.ruoyi.RuoYiApplication`
- Controller 按业务域分包：
  - `web/controller/system/` — 系统管理接口（用户、角色、菜单、部门、岗位、字典、配置、通知）
  - `web/controller/monitor/` — 系统监控接口（在线用户、定时任务、服务监控、缓存、日志）
  - `web/controller/tool/` — 系统工具接口（代码生成、表单构建、Swagger）
  - `web/controller/common/` — 公共控制器（文件上传/下载等）
  - `web/core/config/` — Web 核心配置（Swagger 等）
- 配置文件：
  - `resources/application.yml` — 主配置（端口 8080、Redis、token、MyBatis、PageHelper、Springdoc）
  - `resources/application-druid.yml` — 数据源配置（主从库、Druid 连接池）
  - `resources/mybatis/mybatis-config.xml` — MyBatis 全局配置
  - `resources/i18n/messages.properties` — 国际化消息
  - `resources/logback.xml` — 日志配置

#### `ruoyi-framework`（框架核心）
- `aspectj/` — AOP 切面（数据权限、日志、数据源、限流）
- `config/` — Spring 配置（应用、线程池、Redis、MyBatis、Swagger、过滤器、属性配置）
- `datasource/` — 多数据源支持（主从切换）
- `interceptor/` — 自定义拦截器
- `manager/` — 异步管理器（工厂模式，日志记录等）
- `security/` — Spring Security 配置（过滤器、认证处理、权限上下文）
- `web/` — Web 通用处理（全局异常、服务封装、域名服务信息）

#### `ruoyi-system`（系统业务模块）
- `domain/` — 实体类（SysUser、SysRole、SysDept、SysMenu、SysPost、SysDictType、SysDictData、SysConfig、SysNotice 等）
- `domain/vo/` — 视图对象（TreeSelect、RouterVo、MetaVo 等）
- `mapper/` — MyBatis Mapper 接口
- `service/` — Service 接口
- `service/impl/` — Service 实现
- `resources/mapper/system/` — MyBatis XML 映射文件（16 个）

#### `ruoyi-quartz`（定时任务）
- `domain/` — 任务实体（SysJob、SysJobLog）
- `mapper/` — Mapper 接口
- `service/` — Service 层
- `controller/` — 任务管理接口
- `task/` — 具体定时任务实现
- `util/` — Quartz 工具类
- `config/` — Quartz 配置

#### `ruoyi-generator`（代码生成）
- `domain/` — 代码生成实体（GenTable、GenTableColumn）
- `mapper/` — Mapper 接口
- `service/` — Service 层
- `controller/` — 代码生成接口
- `util/` — Velocity 模板工具类
- `resources/` — 代码生成 vm 模板文件

#### `ruoyi-common`（通用工具）
- `annotation/` — 自定义注解（`@DataScope`、`@DataSource`、`@Excel`、`@Log`、`@RateLimiter`、`@Anonymous` 等）
- `config/` — 通用配置（序列化器）
- `constant/` — 常量定义（CacheConstants、Constants、HttpStatus、UserConstants 等）
- `core/` — 核心基础类：
  - `controller/BaseController` — 控制器基类（分页、响应封装）
  - `domain/AjaxResult` — 统一响应对象
  - `domain/BaseEntity` — 实体基类（createBy/createTime/updateBy/updateTime/remark）
  - `domain/entity/SysUser/SysRole/SysDept` — 核心实体
  - `domain/model/LoginUser/LoginBody/RegisterBody` — 登录相关模型
  - `page/TableDataInfo/PageDomain` — 分页封装
  - `redis/RedisCache` — Redis 操作封装
  - `text/Convert` — 类型转换工具
- `enums/` — 枚举（BusinessType、OperatorType、UserStatus、LimitType 等）
- `exception/` — 异常体系（BaseException、GlobalException、ServiceException、文件/任务/用户异常）
- `filter/` — 过滤器（RepeatableFilter、XSSFilter、XssFilter 等）
- `utils/` — 工具类（StringUtils、DateUtils、ServletUtils、BeanUtils、FileUtils、HttpUtils、IpUtils、SecurityUtils、ServletUtils、SpringUtils、SqlUtils、UuidUtils、poi/ExcelUtil、sign/Base64、html/EscapeUtil 等）
- `xss/` — XSS 防护工具

### 后端分层规范（新增业务模块时遵循）

```
controller/   → 接收 HTTP 请求，参数校验，调用 service
service/      → 业务逻辑接口
service/impl/ → 业务逻辑实现
mapper/       → MyBatis Mapper 接口（@Mapper 注解）
domain/       → 实体类（继承 BaseEntity）
domain/vo/    → 视图对象（返回给前端的专用对象）
resources/mapper/xxx/*Mapper.xml → MyBatis SQL 映射文件
```

**命名规范**：
- 实体类：`Sys{Module}`（如 `SysUser`）
- Mapper：`Sys{Module}Mapper`
- Service：`ISys{Module}Service`
- ServiceImpl：`Sys{Module}ServiceImpl`
- Controller：`Sys{Module}Controller`
- 权限标识格式：`{module}:{entity}:{action}`（如 `system:user:query`）

**接口响应格式**（AjaxResult）：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

**分页响应格式**（startPage() + getDataTable()）：
```json
{
  "total": 100,
  "rows": [],
  "code": 200,
  "msg": "查询成功"
}
```

---

## 前端结构

### 目录说明

```
RuoYi-Vue3/
├── src/
│   ├── api/              # 后端接口调用（按后端模块结构分包）
│   │   ├── login.js      # 登录/登出/获取用户信息
│   │   ├── menu.js       # 获取路由菜单
│   │   ├── system/       # 系统管理接口（user/role/menu/dept/post/dict/config/notice）
│   │   ├── monitor/      # 监控接口（cache/job/online/operlog/logininfor/server）
│   │   └── tool/         # 工具接口（gen）
│   ├── assets/           # 静态资源
│   │   ├── icons/svg/    # SVG 图标（90+）
│   │   ├── images/       # 图片资源
│   │   └── styles/       # 全局样式（SCSS）
│   ├── components/       # 全局公共组件
│   │   ├── Breadcrumb/   # 面包屑
│   │   ├── DictTag/      # 字典标签
│   │   ├── Editor/       # 富文本编辑器（Vue Quill）
│   │   ├── FileUpload/   # 文件上传
│   │   ├── ImageUpload/  # 图片上传
│   │   ├── ImagePreview/ # 图片预览
│   │   ├── Pagination/   # 分页组件
│   │   ├── RightToolbar/ # 表格右侧工具栏
│   │   ├── SvgIcon/      # SVG 图标组件
│   │   ├── TreePanel/    # 树形面板
│   │   └── iFrame/       # 内嵌 iframe
│   ├── directive/        # 自定义指令（权限 v-hasPermi、角色 v-hasRole）
│   ├── layout/           # 页面布局框架（侧边栏、头部、标签页、主体）
│   ├── plugins/          # 全局插件（auth/cache/download/modal/tab）
│   ├── router/           # 路由配置（静态路由 + 动态权限路由）
│   ├── store/            # Pinia 状态管理
│   │   └── modules/
│   │       ├── app.js        # 应用状态（侧边栏、设备）
│   │       ├── dict.js       # 字典缓存
│   │       ├── lock.js       # 锁屏状态
│   │       ├── permission.js # 权限与动态路由
│   │       ├── settings.js   # 系统设置
│   │       ├── tagsView.js   # 标签页管理
│   │       └── user.js       # 用户信息（token/roles/用户数据）
│   ├── utils/            # 工具函数
│   │   ├── auth.js       # Token 存取（Cookie）
│   │   ├── dict.js       # 字典数据获取
│   │   ├── permission.js # 权限校验
│   │   ├── request.js    # Axios 封装（拦截器、防重复提交、通用下载）
│   │   ├── ruoyi.js      # 若依通用方法（时间格式化、树处理、表单重置等）
│   │   ├── validate.js   # 校验工具
│   │   └── jsencrypt.js  # RSA 加密
│   ├── views/            # 页面视图（与后端 Controller 分包对应）
│   │   ├── system/       # 系统管理（user/role/menu/dept/post/dict/config/notice）
│   │   ├── monitor/      # 系统监控（online/job/druid/server/cache/logininfor/operlog）
│   │   ├── tool/         # 系统工具（build/gen/swagger）
│   │   ├── error/        # 错误页（401/404）
│   │   ├── login.vue     # 登录页
│   │   ├── register.vue  # 注册页
│   │   ├── lock.vue      # 锁屏页
│   │   └── index.vue     # 首页
│   ├── App.vue           # 根组件
│   ├── main.js           # 入口文件（全局注册组件/指令/插件）
│   ├── permission.js     # 路由守卫（权限控制、动态路由加载）
│   └── settings.js       # 系统显示设置（主题、侧边栏、标签页等）
├── vite/                 # Vite 插件配置
├── .env.development      # 开发环境变量（VITE_APP_BASE_API = /dev-api）
├── .env.production       # 生产环境变量
├── .env.staging          # 预发布环境变量
├── vite.config.js        # Vite 配置（代理、别名、打包）
└── package.json          # 前端依赖
```

### 前端开发规范

**新增页面流程**：
1. 在 `src/api/` 对应目录下新增接口调用函数
2. 在 `src/views/` 对应目录下新增 `.vue` 页面文件
3. 后端 `sys_menu` 表中配置菜单路由（path/component/perms）
4. 动态路由由后端菜单配置自动加载，无需手动修改 `router/index.js`

**API 调用规范**：
```js
import request from '@/utils/request'

// 查询列表
export function listXxx(query) {
  return request({ url: '/module/xxx/list', method: 'get', params: query })
}
// 新增
export function addXxx(data) {
  return request({ url: '/module/xxx', method: 'post', data })
}
// 修改
export function updateXxx(data) {
  return request({ url: '/module/xxx', method: 'put', data })
}
// 删除
export function delXxx(ids) {
  return request({ url: `/module/xxx/${ids}`, method: 'delete' })
}
```

**Vue 组件规范**：
- 使用 `<script setup>` 语法（Composition API）
- 全局组件已在 `main.js` 注册：`DictTag`、`Pagination`、`FileUpload`、`ImageUpload`、`ImagePreview`、`RightToolbar`、`Editor`
- 权限指令：`v-hasPermi="['system:user:add']"`、`v-hasRole="['admin']"`
- 字典数据：`useDict('dict_type')` 获取字典，`<dict-tag :options="dict.type.xxx" :value="scope.row.field"/>` 展示

---

## 数据库结构

数据库名：`zhaozh`（见 `application-druid.yml`）  
初始化脚本：`sql/ry_20260417.sql`（系统表）、`sql/quartz.sql`（定时任务表）

### 核心系统表

| 表名 | 说明 |
|------|------|
| `sys_user` | 用户信息表 |
| `sys_role` | 角色信息表 |
| `sys_dept` | 部门表（树形结构） |
| `sys_menu` | 菜单权限表（目录/菜单/按钮三级） |
| `sys_post` | 岗位信息表 |
| `sys_user_role` | 用户-角色关联表 |
| `sys_role_menu` | 角色-菜单关联表 |
| `sys_role_dept` | 角色-部门关联表（数据权限） |
| `sys_user_post` | 用户-岗位关联表 |
| `sys_dict_type` | 字典类型表 |
| `sys_dict_data` | 字典数据表 |
| `sys_config` | 参数配置表 |
| `sys_notice` | 通知公告表 |

### 监控相关表

| 表名 | 说明 |
|------|------|
| `sys_oper_log` | 操作日志表 |
| `sys_logininfor` | 登录日志表 |
| `sys_online` | 在线用户表（Redis 存储） |
| `sys_job` | 定时任务表 |
| `sys_job_log` | 定时任务日志表 |

### 代码生成表

| 表名 | 说明 |
|------|------|
| `gen_table` | 代码生成业务表 |
| `gen_table_column` | 代码生成字段信息 |

### 表字段通用约定

所有业务表包含以下审计字段（继承 `BaseEntity`）：
- `create_by` VARCHAR(64) — 创建者
- `create_time` DATETIME — 创建时间
- `update_by` VARCHAR(64) — 更新者
- `update_time` DATETIME — 更新时间
- `remark` VARCHAR(500) — 备注

逻辑删除字段：`del_flag` CHAR(1)（`0`=存在，`2`=已删除）  
状态字段：`status` CHAR(1)（`0`=正常，`1`=停用）

---

## 认证与权限

- **认证方式**：JWT Token（存于 Cookie，Header: `Authorization: Bearer {token}`）
- **Token 有效期**：30 分钟（可配置）
- **权限模型**：RBAC（用户 → 角色 → 菜单/按钮权限）
- **数据权限**：通过 `@DataScope` 注解 + AOP 实现（全部/自定义/本部门/本部门及以下）
- **前端路由**：动态路由由后端根据用户权限返回菜单树，前端自动注册

---

## 构建与运行

### 后端

```bash
# 编译打包（根目录执行）
mvn clean package -DskipTests

# 运行
java -jar ruoyi-admin/target/ruoyi-admin.jar

# 或使用脚本
bin/run.bat      # Windows
bin/run.sh       # Linux
```

- 启动端口：`8080`
- Druid 监控：`http://localhost:8080/druid`（账号：ruoyi/123456）
- Swagger 文档：`http://localhost:8080/swagger-ui.html`

### 前端

```bash
cd RuoYi-Vue3

# 安装依赖
yarn install

# 开发模式（http://localhost:8000）
yarn dev

# 生产构建
yarn build:prod
```

- 开发代理：`/dev-api` → `http://localhost:8080`（在 `vite.config.js` 配置）
- 路径别名：`@` → `src/`，`~` → 项目根目录

---

## 新增业务模块开发指南

### 后端步骤

1. **建表**：在 MySQL 中创建业务表，包含审计字段（`create_by`/`create_time`/`update_by`/`update_time`/`remark`）
2. **代码生成**：使用系统工具 → 代码生成，导入表后生成 Controller/Service/Mapper/Vue 代码
3. **或手动创建**（在 `ruoyi-system` 模块中）：
   - `domain/Sys{Module}.java` — 实体类，继承 `BaseEntity`
   - `mapper/Sys{Module}Mapper.java` — Mapper 接口
   - `resources/mapper/system/Sys{Module}Mapper.xml` — SQL 映射
   - `service/ISys{Module}Service.java` — Service 接口
   - `service/impl/Sys{Module}ServiceImpl.java` — Service 实现
   - 在 `ruoyi-admin` 的 `web/controller/system/` 下创建 Controller
4. **配置菜单**：在 `sys_menu` 表中插入菜单、按钮权限记录
5. **配置字典**：如需字典，在 `sys_dict_type` 和 `sys_dict_data` 中插入数据

### 前端步骤

1. 在 `src/api/` 对应目录创建接口调用文件
2. 在 `src/views/` 对应目录创建 `.vue` 页面
3. 后端配置菜单后，前端动态路由自动加载，无需手动配置 `router/index.js`
4. 使用 `useDict('dict_type')` 获取字典数据
5. 使用 `v-hasPermi` 指令控制按钮权限

---

## 关键配置说明

| 配置文件 | 说明 |
|----------|------|
| `ruoyi-admin/src/main/resources/application.yml` | 主配置（端口、Redis、Token、MyBatis、PageHelper、XSS） |
| `ruoyi-admin/src/main/resources/application-druid.yml` | 数据源（主从库、Druid 连接池参数） |
| `RuoYi-Vue3/.env.development` | 前端开发环境（base API） |
| `RuoYi-Vue3/.env.production` | 前端生产环境 |
| `RuoYi-Vue3/vite.config.js` | Vite 构建配置（代理、别名、打包策略） |
| `RuoYi-Vue3/src/settings.js` | 前端显示设置（主题、侧边栏、标签页样式） |

---

## 常用约定

- **接口路径**：RESTful 风格，`GET /list` 查询、`POST /` 新增、`PUT /` 修改、`DELETE /{ids}` 删除
- **响应状态码**：`200`=成功、`401`=未认证、`500`=服务端错误、`601`=警告
- **分页参数**：`pageNum`（页码）、`pageSize`（每页条数）、`orderByColumn`（排序字段）、`isAsc`（排序方向）
- **日期格式**：`yyyy-MM-dd HH:mm:ss`（GMT+8）
- **字符编码**：统一 UTF-8
- **逻辑删除**：`del_flag`（`0`=存在，`2`=删除），查询时默认过滤已删除记录
- **树形数据**：使用 `ancestors` 字段存储祖级列表（逗号分隔），`handleTree()` 工具方法转换为树结构
