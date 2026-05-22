# Ai-Campus-Academic-Job —— AI 原生校园学业就业智能服务平台

> 第十五届中国软件杯 A6 赛题参赛项目
> 基于金蝶 AI 苍穹平台，以 AI 智能体为核心，打造覆盖学生全校园周期的学业管理与就业服务一体化智能平台。

---

## 核心功能

### 学业管理
| 功能 | 说明 |
|------|------|
| 学业数据总览 | 学生画像（GPA、学分、专业、年级），成绩记录查询 |
| 学业智能预警 | 支持挂科/绩点/学分/出勤四种预警，红橙黄三级，可标记已读/处理 |
| 个性化学习规划 | 教师为学生制定学习计划，学生查看自己的规划并跟踪完成状态 |

### 就业服务
| 功能 | 说明 |
|------|------|
| AI 简历生成与优化 | 输入学号，AI 自动获取学生信息生成专业简历，评分与优化建议 |
| 智能职位匹配 | 简历与岗位多维度匹配打分，输出匹配理由 |
| AI 模拟面试 | 文字/语音双模式（金蝶 Cosmos 语音能力），AI 提问 → 作答 → 评分反馈 |
| 面试记录管理 | 查看历史面试记录，展开查看详细问答 |

### 三端协同 + 角色权限
| 端 | 用户 | 可见范围 |
|------|------|----------|
| PC 管理后台 | 管理员 | 全部数据，系统管理 |
| PC 管理后台 | 教师 | 仅自己辅导的学生数据 |
| PC 管理后台 | 学生 | 仅自己的学业/简历/规划/面试 |
| 微信小程序 | 学生 | 轻量化查看学业与求职信息 |
| 校方大屏 | 校领导 | 7 屏数据可视化大屏（静态展示） |

---

## 技术架构

| 层级 | 技术栈 |
|------|--------|
| 后端 | Spring Boot 3.2 + MyBatis Plus 3.5 + Spring Security + JWT + WebSocket |
| 前端管理后台 | Vue 3.4 + Vite 5 + Element Plus 2.x + Pinia + ECharts 5 |
| 微信小程序 | 原生小程序框架 + WebSocket |
| AI 能力 | 金蝶苍穹 AI 平台（LLM 大模型 + RAG 检索增强 + 提示词工程） |
| 语音能力 | 金蝶 Cosmos 语音识别/合成（小程序端 RecorderManager + Cosmos） |
| 数据层 | MySQL 8.0（本地开发 + 生产）+ 文件持久化 |
| 部署 | Docker Compose + Nginx + GitHub Actions CI/CD |

---

## 项目结构

```
├── backend/                               # Spring Boot 后端
│   └── src/main/java/com/soyorim/acaj/
│       ├── common/                        # 统一响应、分页、异常处理
│       ├── config/security/               # Security、JWT、JwtAuthFilter、SecurityUtils
│       └── module/
│           ├── system/                    # 用户/角色/菜单 + 认证授权
│           ├── academic/                  # 学生画像、课程、成绩、预警、学习规划
│           ├── employment/                # 简历、岗位、人岗匹配、模拟面试
│           └── ai/                        # AI 对话、知识库、AI 服务层、语音接口
├── frontend-admin/                        # Vue 3 管理后台（14 个页面）
│   └── src/
│       ├── api/                           # API 封装（auth/academic/employment/ai/system）
│       │   └── request.ts                 # axios 实例 + 错误拦截（区分 401/403/超时等）
│       ├── router/                        # 路由 + 角色守卫
│       ├── stores/                        # Pinia 状态管理
│       ├── layout/                        # 侧边栏 + 顶部导航（角色菜单过滤）
│       └── views/
│           ├── login/                     # 登录页
│           ├── dashboard/                 # 校方大屏（默认首页）
│           ├── academic/                  # 学生管理、预警、学习规划
│           ├── employment/                # 简历、岗位、AI模拟面试、面试记录
│           ├── ai/                        # 知识库管理
│           └── system/                    # 用户管理、角色管理
├── frontend-miniapp/                      # 微信小程序（7 个页面）
│   └── pages/{index,academic,resume,jobs/{index,detail},interview,profile}/
├── ai-agent/                              # AI 智能体（Python）
│   ├── llm/                               # 大模型调用、提示词模板
│   ├── rag/                               # 文档加载/切分/检索
│   └── knowledge-base/                    # 知识库文档目录
├── docs/                                  # API / 架构 / 部署 / 用户指南
├── scripts/init-db.sql                    # MySQL 初始化脚本（16 张表 + 种子数据）
├── docker-compose.yml                     # Docker 一键部署（MySQL + Redis + 后端 + 前端）
└── .github/workflows/                     # CI/CD
```

---

## 快速开始

### 环境要求
| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 21+ | 21 测试通过 |
| Node.js | 18+ | 24 测试通过 |
| Maven | 3.9+ | 或使用系统安装的 Maven |
| MySQL | 8.0+ | 本地开发必需 |

> **注意**：开发环境已统一使用 MySQL，不再使用 H2。请确保本地 MySQL 服务已启动。

### 1. 准备数据库

MySQL 启动后，执行初始化脚本：

```bash
# Windows PowerShell（需先安装 MySQL 客户端）
mysql -uroot -p你的密码 < scripts/init-db.sql

# 或通过 MySQL 命令行
mysql -uroot -p
CREATE DATABASE IF NOT EXISTS acaj DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE acaj;
source scripts/init-db.sql;
```

`scripts/init-db.sql` 会自动创建 16 张表并导入种子数据（管理员账号、角色、菜单等）。测试数据需额外执行 `scripts/migrate-h2-to-mysql.sql`。

### 2. 后端配置

编辑 `backend/src/main/resources/application-dev.yml`，修改 MySQL 连接信息：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/acaj?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 你的密码
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
```

### 3. 后端启动

```bash
cd backend
mvn clean package -DskipTests
java -jar target/acaj-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
```

启动后：
- API 文档（Knife4j）：http://localhost:8080/doc.html
- 后端运行在 http://localhost:8080

### 4. 管理后台启动

```bash
cd frontend-admin
npm install
npm run dev
```

访问 http://localhost:3000（Vite 代理 `/api` 到 `localhost:8080`）

### 5. 微信小程序

1. 使用微信开发者工具打开 `frontend-miniapp` 目录
2. 配置 AppID（`wx98caabde4bdd6522`）
3. 小程序使用 RecorderManager + 金蝶 Cosmos 进行语音识别/合成

### Docker 部署

```bash
docker-compose up -d
```

Docker Compose 会启动 MySQL + Redis + 后端 + 前端管理后台（Nginx 端口 80），适合生产或演示环境。

---

## 测试账号

所有账号密码统一为 `admin123`。

| 用户名 | 角色 | 真实姓名 | 管辖学生 |
|--------|------|----------|----------|
| admin | 管理员 | 系统管理员 | 全部 |
| teacher1 | 教师 | 张老师 | 刘洋、孙悦、周杰 |
| teacher2 | 教师 | 李辅导员 | 李明、王芳、张伟 |
| teacher3 | 教师 | 王教授 | 陈静、赵磊 |
| student1 | 学生 | 李明 | — |
| student2 | 学生 | 王芳 | — |
| student3 | 学生 | 张伟 | — |
| student4 | 学生 | 刘洋 | — |
| student5 | 学生 | 陈静 | — |
| student6 | 学生 | 赵磊 | — |
| student7 | 学生 | 孙悦 | — |
| student8 | 学生 | 周杰 | — |

---

## API 接口

所有接口前缀 `/api`，需认证的接口 Header 携带 `Authorization: Bearer {token}`。

### 认证
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | /api/auth/login | 公开 | 登录，返回 JWT token |
| GET | /api/auth/info | 登录用户 | 当前用户信息 |

### 学业管理
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/academic/students | 登录用户 | 学生列表（分页，支持学号搜索） |
| GET | /api/academic/student/{id} | 登录用户（归属校验） | 学生详情 |
| GET | /api/academic/warnings | 登录用户 | 预警列表（分页，支持学号搜索） |
| PUT | /api/academic/warning/{id}/read | 登录用户（归属校验） | 标记已读 |
| PUT | /api/academic/warning/{id}/handle | 登录用户（归属校验） | 处理预警 |
| GET | /api/academic/study-plans | 登录用户 | 学习规划列表 |
| GET | /api/academic/study-plan/{id} | 登录用户（归属校验） | 规划详情 |
| POST/PUT/DELETE | /api/academic/study-plan | 登录用户（归属校验） | 规划 CUD |

### 就业服务
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/employ/resumes | 登录用户 | 简历列表（分页） |
| GET | /api/employ/resume/{studentId} | 登录用户（归属校验） | 查询简历 |
| POST | /api/employ/resume | 登录用户（归属校验） | 保存/更新简历 |
| POST | /api/employ/resume/generate | 登录用户（归属校验） | AI 生成简历 |
| GET | /api/employ/jobs | 登录用户 | 岗位列表（分页，关键字搜索） |
| GET/POST/PUT/DELETE | /api/employ/job/* | GET 公开，CUD 管理员/教师 | 岗位管理 |
| POST | /api/employ/match | 登录用户（归属校验） | 人岗匹配 |
| GET | /api/employ/matches/{resumeId} | 登录用户（归属校验） | 匹配结果列表 |
| POST | /api/employ/interview/start | 登录用户（归属校验） | 开始模拟面试（AI 出题） |
| POST | /api/employ/interview/submit | 登录用户（归属校验） | 提交面试答案（AI 评分） |
| GET | /api/employ/interviews | 登录用户 | 面试列表 |
| GET | /api/employ/interviews/{studentId} | 登录用户（归属校验） | 某学生的面试记录 |

### AI 智能体
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | /api/ai/chat | 登录用户 | AI 对话（userId 从 JWT 获取） |
| GET | /api/ai/conversations/{userId} | 登录用户（归属校验） | 对话会话列表 |
| GET | /api/ai/conversation/{sessionId} | 登录用户（归属校验） | 对话详情 |
| GET | /api/ai/knowledge | 登录用户 | 知识库列表 |
| POST/PUT/DELETE | /api/ai/knowledge | 管理员/教师 | 知识库 CUD |
| POST | /api/ai/text-to-speech | 登录用户 | 文本转语音（Cosmos） |
| POST | /api/ai/speech-to-text | 登录用户 | 语音转文本（Cosmos） |

### 系统管理（仅管理员）
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET/POST/PUT/DELETE | /api/system/user | 管理员 | 用户 CRUD |
| GET/POST/PUT/DELETE | /api/system/role | 管理员 | 角色管理 |
| GET | /api/system/menu/tree | 管理员 | 菜单树 |
| POST/PUT/DELETE | /api/system/menu | 管理员 | 菜单 CUD |

### WebSocket
| 路径 | 说明 |
|------|------|
| ws://localhost:8080/ws/notification/{userId} | 实时预警推送 |

---

## 安全机制

### 方法级权限控制

系统使用 Spring Security `@EnableMethodSecurity` + `@PreAuthorize` 实现方法级权限：

| 注解 | 保护范围 |
|------|----------|
| `@PreAuthorize("hasRole('ADMIN')")` | 系统管理（用户/角色/菜单 CUD） |
| `@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")` | 岗位管理 CUD、知识库 CUD |

### 数据隔离

所有单条数据查询和写操作均经过所有权校验：

| 角色 | 可访问数据 |
|------|-----------|
| 管理员 | 全部数据 |
| 教师 | 仅管辖学生（通过 `advisor_id` 关联）的数据 |
| 学生 | 仅自己的数据 |

校验规则：
- **学生**：`resource.userId == currentUserId`
- **教师**：`resource.studentId IN (SELECT id FROM academic_student WHERE advisor_id = currentUserId)`
- **管理员**：无限制

### AI 聊天安全

- `POST /api/ai/chat`：`userId` 从 JWT Token 获取，**不接受请求体中的 userId**，防止身份冒充
- `GET /api/ai/conversations/{userId}`：校验请求者是否为会话持有者或管理员
- `GET /api/ai/conversation/{sessionId}`：校验会话归属

### 前端错误处理

axios 拦截器区分以下错误类型，用户看到的是具体错误原因而非模糊提示：

| HTTP 状态 | 前端提示 |
|-----------|----------|
| 401 | 登录已过期，请重新登录 |
| 403 | 权限不足 |
| 5xx | 服务器异常，请稍后重试 |
| 超时 | 请求超时，请检查网络连接 |
| 网络断开 | 网络连接失败，请检查后端服务是否启动 |

---

## 数据权限矩阵

| 角色 | 学生列表 | 预警 | 学习规划 | 简历 | 面试记录 | 岗位管理 | 知识库管理 | 系统管理 |
|------|----------|------|----------|------|----------|----------|------------|----------|
| 管理员 | 全部 | 全部 | 全部 | 全部 | 全部 | 全部 | 全部 | 全部 |
| 教师 | 管辖学生 | 管辖学生 | 管辖学生 | 管辖学生 | 管辖学生 | 全部 | 全部 | 禁止 |
| 学生 | 仅自己 | 仅自己 | 仅自己 | 仅自己 | 仅自己 | 只读 | 只读 | 禁止 |

---

## 许可证

MIT License
