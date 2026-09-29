# AI 医院药品进销存信息管理系统

本项目是基于 RuoYi-Vue 二次开发的医院药房进销存管理系统，包含药品基础资料、入库/出库/退库、库存监管、权限控制，以及 AI 问答、知识库和库存月报能力。仓库采用前后端分离结构，后端为 Spring Boot + MyBatis，管理端为 Vue 2 + Element UI。

## 提交内容

本仓库已经按课程项目的最终提交范围精简：

```text
ai-pharmacy-inventory-system/
├─ RuoYi-Vue-master/            后端主工程、测试代码和数据库脚本
│  └─ sql/                      初始化、药品业务、库存业务及菜单脚本
├─ RuoYi-Vue2-master/           最终 Web 管理端
├─ .github/workflows/verify.yml 后端测试和 Web 生产构建验证
├─ docs/成员运行项目说明.md      组员本地运行与协作说明
└─ README.md                    项目总览与快速启动说明
```

旧移动端、根目录重复 SQL、阶段性设计稿和过时部署文档不属于最终交付内容，未包含在本次提交中。

## 主要功能

| 模块 | 功能 |
| --- | --- |
| 库存工作台 | 药品、库存、预警、临期数据概览，近 12 个月入库/出库趋势和最近业务单 |
| 基础资料 | 药品分类、药品信息、供应商信息维护 |
| 进销存业务 | 入库、出库、退库草稿与确认，按批次更新库存并生成库存流水 |
| 库存监管 | 库存盘点、上下限预警、效期管理、过期药品清理和库存重算 |
| AI 服务 | 流式对话、库存工具调用、资料导入、RAG 知识库、库存月报 |
| 系统能力 | 用户、角色、菜单、数据权限、操作日志、Redis 缓存和定时任务 |
| 质量保障 | 后端单元/契约/MySQL 集成测试，Web 生产构建，GitHub Actions 自动验证 |

库存变更在事务中同步更新药品总库存、批次剩余数量和库存流水。盘点审核、清理确认等关键操作使用数据库锁和状态校验，避免重复处理或并发导致库存不一致。

## 技术栈

- 后端：Java 17、Spring Boot 4.1、Spring Security、MyBatis、Maven
- AI：Spring AI、OpenAI 兼容模型接口、本地知识库检索、SSE 流式响应
- 数据：MySQL 8、Druid、Redis、Quartz
- 前端：Vue 2、Vue Router、Vuex、Element UI、ECharts、Axios
- 验证：JUnit、Mockito、MySQL 集成测试、GitHub Actions

## 快速启动

### 1. 准备环境

建议使用以下版本：

| 软件 | 建议版本 |
| --- | --- |
| JDK | 17 或 21 |
| Maven | 3.8+ |
| MySQL | 8.0 |
| Redis | 5.0+ |
| Node.js | 18 |
| npm | 随 Node.js 安装 |

### 2. 获取代码

```powershell
git clone https://github.com/kongbaihui/ai-pharmacy-inventory-system.git
cd ai-pharmacy-inventory-system
```

### 3. 初始化数据库

先创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS `ry-vue`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;
USE `ry-vue`;
```

全新数据库按以下顺序执行 `RuoYi-Vue-master/sql/` 中的脚本：

1. `ry_20260417.sql`：若依系统基础表和初始数据。
2. `quartz.sql`：定时任务表。
3. `01_med_schema.sql`：药品、供应商、库存、批次、盘点、预警等业务表。
4. `03_stock_business.sql`：入库、出库、退库业务单和批次唯一索引。
5. `02_med_menu.sql`：药品基础资料与库存监管菜单。
6. `04_web_menu.sql`：最终 Web 菜单、AI 菜单和普通角色权限整合。

`05_common_role_permissions.sql` 是普通角色权限的独立修复脚本，供已有数据库只重置 `common` 角色菜单时使用；全新数据库执行最新版 `04_web_menu.sql` 后无需重复执行。

其中 `ry_20260417.sql`、`quartz.sql` 和 `01_med_schema.sql` 会重建相关表，不要在已有正式数据的数据库上重复执行。

### 4. 配置本地参数

在 `RuoYi-Vue-master/` 下新建 `.env.local`。该文件已被 Git 忽略，不会提交本机密码或模型密钥：

```properties
spring.datasource.druid.master.url=jdbc:mysql://localhost:3306/ry-vue?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
spring.datasource.druid.master.username=root
spring.datasource.druid.master.password=你的MySQL密码

# AI 为可选功能；未配置时普通进销存仍可运行
AI_CHAT_PROVIDER=openai
AI_CHAT_BASE_URL=https://你的模型服务地址
AI_CHAT_API_KEY=你的模型服务密钥
AI_CHAT_MODEL=你的模型名称
```

Redis 默认连接 `localhost:6379`、数据库 `0`、无密码。如果本机设置了密码，可在 `.env.local` 中增加：

```properties
spring.data.redis.password=你的Redis密码
```

可复制后端目录中的 `.env.example` 作为起点。不要提交 `.env.local`。

### 5. 启动后端

先启动 MySQL 和 Redis，然后执行：

```powershell
cd RuoYi-Vue-master
mvn -pl ruoyi-admin -am spring-boot:run
```

后端默认地址为 `http://localhost:8080`。

### 6. 启动 Web 管理端

另开一个终端：

```powershell
cd RuoYi-Vue2-master
npm install --legacy-peer-deps
npm run dev
```

Web 默认地址为 `http://localhost`，开发代理连接 `http://localhost:8080`。默认管理员账号为 `admin`，默认密码为 `admin123`。

执行菜单脚本或修改角色权限后，应退出账号并重新登录，以重新加载动态菜单。

## AI 功能说明

AI 页面提供以下能力：

- 普通与流式问答；
- 查询库存概览、药品库存、临期批次和补货建议；
- 导入药品说明书或药房制度资料并重建知识库；
- 基于知识库回答问题并返回引用来源；
- 获取库存经营数据并生成流式月报。

未配置模型时，系统使用回退配置保证后端正常启动，药品和库存业务不受影响。启用 AI 后，应使用具有 `system:ai:knowledge` 等对应权限的账号操作知识库和报告功能。

## 测试与构建

后端测试：

```powershell
cd RuoYi-Vue-master
mvn -B test
```

Web 生产构建：

```powershell
cd RuoYi-Vue2-master
npm install --legacy-peer-deps
npm run build:prod
```

`.github/workflows/verify.yml` 会在推送到 `main` 或创建 Pull Request 时自动执行后端测试和 Web 生产构建。后端任务使用独立 MySQL 8 测试数据库，不依赖个人本地数据库。

## 项目文档

组员首次运行、更新数据库、处理常见错误或提交代码前，请阅读 [成员运行项目说明](docs/成员运行项目说明.md)。

## 安全与提交约定

- 不提交 `.env.local`、真实数据库密码、Redis 密码或 AI API Key。
- 不提交 `node_modules/`、`target/`、`dist/`、日志、上传文件和运行时知识库。
- 数据库结构或菜单发生变化时，同步更新 `RuoYi-Vue-master/sql/` 和运行说明。
- 提交前至少执行 `mvn -B test` 与 `npm run build:prod`，并检查 `git status`。
