# ACAJ — AI 原生校园学业就业智能服务平台

> 第十五届中国软件杯 A6 赛题参赛项目
> 以 AI 智能体为核心，覆盖学生全校园周期的学业管理与就业服务一体化智能平台。

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
| AI 模拟面试 | 文字/语音双模式，AI 提问 → 作答 → 评分反馈 |
| 面试记录管理 | 查看历史面试记录，展开查看详细问答 |

### 三端协同 + 角色权限
| 端 | 用户 | 可见范围 |
|------|------|----------|
| PC 管理后台 | 管理员 | 全部数据，系统管理 |
| PC 管理后台 | 教师 | 仅自己辅导的学生数据 |
| PC 管理后台 | 学生 | 仅自己的学业/简历/规划/面试 |
| 微信小程序 | 学生 | 微信快捷登录 → 绑定学号 → 学业与求职信息 |
| 校方大屏 | 校领导 | 数据可视化大屏 |

---

## 技术架构

| 层级 | 技术栈 |
|------|--------|
| 后端 | Java 21 · Spring Boot 3.2.5 · MyBatis Plus 3.5.6 · Spring Security · JWT (jjwt 0.12.5) · WebSocket |
| 前端管理后台 | Vue 3.4 · Vite 5 · TypeScript · Element Plus 2.x · Pinia · ECharts 5 |
| 微信小程序 | 原生小程序框架 (基础库 3.16) · RecorderManager |
| AI 能力 | 金蝶苍穹 AI 平台（LLM 大模型 + Whisper 语音识别 + TTS 语音合成） |
| 数据层 | MySQL 8.0 + Redis（可选） |
| 工具库 | Hutool 5.8.27 · Lombok · Knife4j 4.4 (API 文档) |
| 部署 | Docker Compose + Nginx + GitHub Actions CI/CD |

---

## 项目结构

```
├── backend/                               # Spring Boot 后端
│   └── src/main/java/com/soyorim/acaj/
│       ├── common/                        # 统一响应(Result)、分页、异常处理(BusinessException)
│       ├── config/security/               # SecurityConfig、JwtUtil、JwtAuthFilter
│       └── module/
│           ├── system/                    # 用户/角色/菜单 + 认证授权 + 微信登录绑定
│           ├── academic/                  # 学生画像、预警、学习规划
│           ├── employment/                # 简历、岗位、人岗匹配、模拟面试
│           └── ai/                        # AI 对话、知识库、语音识别/合成(VoiceController)
├── frontend-admin/                        # Vue 3 管理后台
│   └── src/
│       ├── api/                           # API 封装（auth/academic/employment/ai/system）
│       ├── router/                        # 路由 + 角色守卫
│       ├── stores/                        # Pinia 状态管理
│       ├── layout/                        # 侧边栏 + 顶部导航（角色菜单过滤）
│       ├── components/                    # 通用组件（page-shell、empty-state、skeleton-table、stat-card）
│       ├── styles/                        # 设计令牌(tokens.css) + Element Plus 覆盖 + 过渡动画
│       └── views/
│           ├── login/                     # 登录页
│           ├── dashboard/                 # 校方大屏（默认首页）+ 仪表盘
│           ├── academic/                  # 学生管理、预警、学习规划
│           ├── employment/                # 简历、岗位、AI模拟面试、面试记录
│           ├── ai/                        # 知识库管理
│           └── system/                    # 用户管理、角色管理
├── frontend-miniapp/                      # 微信小程序（9 个页面）
│   ├── app.js                            # 全局：认证管理 · request 封装 · 统一导航
│   ├── app.wxss                          # 统一设计系统：40+ CSS 令牌（色板/间距/排版/阴影）
│   └── pages/
│       ├── index/                        # 首页（真实 API 岗位 + 预警 + 快捷入口）
│       ├── login/                        # 微信快捷登录（wx.login → code → 后端）
│       ├── bind/                         # 绑定学号（临时 token + 学号密码验证）
│       ├── jobs-tab/                     # 岗位市场 Tab（搜索 + 分页 + 真实 API）
│       ├── jobs/
│       │   ├── index/                    # → 重定向至 jobs-tab
│       │   └── detail/                   # 岗位详情 + 投递
│       ├── study/                        # 学业仪表盘（GPA 环 + 学分进度 + LV 等级 + 预警）
│       ├── resume/                       # 简历（查看/编辑/AI 优化三态切换）
│       ├── interview/                    # AI 模拟面试（文字/语音双模式）
│       ├── profile/                      # 个人中心（JWT 认证 + API 统计 + 功能菜单）
│       ├── academic/                     # → 重定向至 study
│       └── level-detail/                 # LV 等级详情（16 级称号表）
├── docs/                                  # API / 架构 / 部署文档
├── scripts/init-db.sql                    # MySQL 初始化脚本
├── docker-compose.yml                     # Docker 一键部署
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

> 开发环境已统一使用 MySQL。请确保本地 MySQL 服务已启动。

### 1. 准备数据库

```bash
# 创建数据库
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS acaj DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 执行初始化脚本（16 张表 + 种子数据）
mysql -uroot -p acaj < scripts/init-db.sql

# 追加 openid 字段（微信小程序绑定学号功能）
mysql -uroot -p acaj -e "ALTER TABLE sys_user ADD COLUMN openid VARCHAR(64) NULL COMMENT '微信openid' AFTER avatar; ALTER TABLE sys_user ADD UNIQUE INDEX idx_openid (openid);"
```

### 2. 后端配置

编辑 `backend/src/main/resources/application-dev.yml`，修改 MySQL 连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/acaj?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 你的密码
```

### 3. 后端启动

```bash
cd backend
mvn clean package -DskipTests
java -jar target/acaj-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
```

启动后：
- API 文档 (Knife4j)：http://localhost:8080/doc.html
- 后端运行在 http://localhost:8080

### 4. 管理后台启动

```bash
cd frontend-admin
npm install
npm run dev
```

访问 http://localhost:3000（Vite 代理 `/api` → `localhost:8080`）

### 5. 微信小程序

1. 使用微信开发者工具打开 `frontend-miniapp` 目录
2. 配置 AppID（`wx98caabde4bdd6522`）
3. 设置环境变量 `WX_APP_SECRET` 为真实的微信小程序 AppSecret
4. 小程序启动后自动检测登录状态，未登录跳转微信快捷登录 → 绑定学号

### 微信小程序技术要点

- 所有页面共用 `app.wxss` 中的 CSS 自定义属性，主色 `#9DB4C0`，与 PC 管理端设计令牌同源
- 所有 API 请求通过 `app.request()` 统一封装（自动携带 JWT、401 跳转登录页、非 200 自动 toast）
- 所有页面导航通过 `app.navigateTo()` 统一分发（自动判断 tabBar / navigateTo）
- 简历页支持三态切换：空状态 → 编辑（动态增删教育/实习/技能）→ 查看（AI 优化）
- AI 面试支持文字/语音双模式（RecorderManager 录音 → 后端 Cosmos 语音识别/合成）
- 学业仪表盘包含 GPA 环形进度（Canvas）、学分进度条、LV 综合等级（学分×0.6 + GPA×0.4）
- 个人中心接入真实 JWT 认证流程，徽章计数来自后端 API 实时查询

### Docker 部署

```bash
docker-compose up -d
```

---

## 微信小程序认证流程

```
小程序启动
  │
  ├─ 有 token → 获取用户信息 → 进入首页
  │
  └─ 无 token → 登录页
                  │
                  └─ 点击「微信快捷登录」
                       │
                       └─ wx.login() 获取 code
                            │
                            └─ POST /api/auth/wx-login { code }
                                 │
                                 ├─ openid 已绑定 → 返回正式 JWT → 进入首页
                                 │
                                 └─ openid 未绑定 → 返回临时 token (5min)
                                                      │
                                                      └─ 跳转绑定页
                                                           │
                                                           └─ 输入学号 + 密码
                                                                │
                                                                └─ POST /api/auth/bind { studentId, password }
                                                                     │
                                                                     └─ 验证通过 → 写入 openid → 返回正式 JWT → 进入首页
```

---

## API 接口

所有接口前缀 `/api`，需认证的接口 Header 携带 `Authorization: Bearer {token}`。

### 认证
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | /api/auth/login | 公开 | 用户名+密码登录，返回 JWT |
| POST | /api/auth/wx-login | 公开 | 微信 code 登录（已绑定→JWT，未绑定→临时token） |
| POST | /api/auth/bind | 临时token | 绑定学号+密码，返回正式 JWT |
| GET | /api/auth/info | 登录用户 | 当前用户信息 |

### 学业管理
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/academic/students | 登录用户 | 学生列表（分页，支持学号搜索） |
| GET | /api/academic/student/{id} | 登录用户（归属校验） | 学生详情 |
| GET | /api/academic/warnings | 登录用户 | 预警列表（分页） |
| PUT | /api/academic/warning/{id}/read | 登录用户（归属校验） | 标记已读 |
| PUT | /api/academic/warning/{id}/handle | 登录用户（归属校验） | 处理预警 |
| GET/POST/PUT/DELETE | /api/academic/study-plan* | 登录用户（归属校验） | 学习规划 CRUD |

### 就业服务
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/employ/resumes | 登录用户 | 简历列表 |
| GET | /api/employ/resume/{studentId} | 登录用户（归属校验） | 查询简历 |
| POST | /api/employ/resume | 登录用户（归属校验） | 保存/更新简历 |
| POST | /api/employ/resume/generate | 登录用户（归属校验） | AI 生成简历 |
| GET | /api/employ/jobs | 公开 | 岗位列表 |
| POST/PUT/DELETE | /api/employ/job/* | 管理员/教师 | 岗位管理 CUD |
| POST | /api/employ/match | 登录用户（归属校验） | 人岗匹配 |
| POST | /api/employ/interview/start | 登录用户 | 开始模拟面试（AI 出题） |
| POST | /api/employ/interview/submit | 登录用户 | 提交面试答案（AI 评分） |
| GET | /api/employ/interviews | 登录用户 | 面试记录列表 |

### AI 智能体
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | /api/ai/chat | 登录用户 | AI 对话 |
| GET | /api/ai/conversations/{userId} | 登录用户（归属校验） | 会话列表 |
| GET | /api/ai/conversation/{sessionId} | 登录用户（归属校验） | 对话详情 |
| GET/POST/PUT/DELETE | /api/ai/knowledge* | 管理员/教师 | 知识库 CRUD |
| POST | /api/ai/speech-to-text | 登录用户 | 语音转文本（Cosmos Whisper） |
| POST | /api/ai/text-to-speech | 登录用户 | 文本转语音（Cosmos TTS） |

### 系统管理（仅管理员）
| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST/PUT/DELETE | /api/system/user* | 用户 CRUD |
| GET/POST/PUT/DELETE | /api/system/role* | 角色管理 |
| GET | /api/system/menu/tree | 菜单树 |

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

## 数据权限矩阵

| 角色 | 学生列表 | 预警 | 学习规划 | 简历 | 面试记录 | 岗位管理 | 知识库管理 | 系统管理 |
|------|----------|------|----------|------|----------|----------|------------|----------|
| 管理员 | 全部 | 全部 | 全部 | 全部 | 全部 | 全部 | 全部 | 全部 |
| 教师 | 管辖学生 | 管辖学生 | 管辖学生 | 管辖学生 | 管辖学生 | 全部 | 全部 | 禁止 |
| 学生 | 仅自己 | 仅自己 | 仅自己 | 仅自己 | 仅自己 | 只读 | 只读 | 禁止 |

---

## 安全机制

- **JWT 认证**：正式 token 24h 有效，临时 token（微信未绑定）5min 有效
- **方法级权限**：`@PreAuthorize` 注解控制角色访问
- **数据隔离**：教师仅见管辖学生数据，学生仅见自身数据
- **AI 防冒充**：`userId` 从 JWT 提取，不接受请求体传参
- **密码加密**：BCrypt 存储

---

## 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `DB_URL` | `jdbc:mysql://localhost:3306/acaj` | 数据库连接 |
| `DB_USERNAME` | `root` | 数据库用户名 |
| `DB_PASSWORD` | — | 数据库密码 |
| `JWT_SECRET` | 内置 dev key | 生产环境必须替换 |
| `AI_API_KEY` | — | 金蝶苍穹 AI 平台 API Key |
| `WX_APP_ID` | `wx98caabde4bdd6522` | 微信小程序 AppID |
| `WX_APP_SECRET` | — | 微信小程序 AppSecret（生产必填） |
| `REDIS_HOST` | `localhost` | Redis 地址（可选） |

---

## 许可证

MIT License
