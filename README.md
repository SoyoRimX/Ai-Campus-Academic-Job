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
| AI 简历生成与优化 | 根据学生信息 AI 生成专业简历，评分与优化建议 |
| 智能职位匹配 | 简历与岗位多维度匹配打分，输出匹配理由 |
| AI 模拟面试 | 文字/语音双模式，AI 提问 → 作答 → 评分反馈 + 问答回顾 |
| 面试记录管理 | 查看历史面试记录，展开查看详细问答 |

### 三端协同 + 角色权限
| 端 | 用户 | 可见范围 |
|------|------|----------|
| PC 管理后台 | 管理员 | 全部数据，系统管理 |
| PC 管理后台 | 教师 | 仅自己辅导的学生数据 |
| PC 管理后台 | 学生 | 仅自己的学业/简历/规划/面试 |
| 微信小程序 | 学生 | 轻量化查看学业与求职信息 |
| 校方大屏 | 校领导 | 7 屏数据可视化大屏 |

---

## 技术架构

| 层级 | 技术栈 |
|------|--------|
| 后端 | Spring Boot 3.2 + MyBatis Plus 3.5 + Spring Security + JWT + WebSocket |
| 前端管理后台 | Vue 3.4 + Vite 5 + Element Plus 2.x + Pinia + ECharts 5 |
| 微信小程序 | 原生小程序框架 + WebSocket |
| AI 能力 | 金蝶苍穹 AI 平台（LLM 大模型 + RAG 检索增强 + 提示词工程） |
| 数据层 | MySQL 8.0（生产）/ H2（开发）+ Redis + Elasticsearch |
| 部署 | Docker Compose + Nginx + GitHub Actions CI/CD |

---

## 项目结构

```
├── backend/                               # Spring Boot 后端
│   └── src/main/java/com/soyorim/acaj/
│       ├── common/                        # 统一响应、分页、异常处理
│       ├── config/                        # Security、JWT、MyBatis、WebSocket、MetaHandler
│       └── module/
│           ├── system/                    # 用户/角色/菜单 + 认证授权
│           ├── academic/                  # 学生画像、课程、成绩、预警、学习规划
│           ├── employment/                # 简历、岗位、人岗匹配、模拟面试
│           └── ai/                        # AI 对话、知识库、AI 服务层
├── frontend-admin/                        # Vue 3 管理后台（14 个页面）
│   └── src/
│       ├── api/                           # API 封装（auth/academic/employment/ai/system）
│       ├── router/                        # 路由 + 角色守卫
│       ├── stores/                        # Pinia 状态管理
│       ├── layout/                        # 侧边栏 + 顶部导航（角色菜单过滤）
│       └── views/
│           ├── login/                     # 登录页
│           ├── dashboard/                 # 仪表盘 + 校方大屏
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
├── scripts/init-db.sql                    # MySQL 初始化脚本
├── docker-compose.yml                     # 一键部署
└── .github/workflows/ci.yml               # CI/CD
```

---

## 快速开始

### 环境要求
| 软件 | 版本 |
|------|------|
| JDK | 17+ |
| Node.js | 18+ |
| MySQL | 8.0+（开发环境可选，内置 H2） |
| Redis | 6.0+（开发环境可选） |

### 后端启动
```bash
cd backend
mvn package -DskipTests
java -jar target/acaj-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
```

开发环境内置 H2 内存数据库 + 完整种子数据，无需 MySQL/Redis。启动后：
- API 文档：http://localhost:8080/doc.html
- H2 控制台：http://localhost:8080/h2-console（JDBC URL: `jdbc:h2:mem:acaj`）

### 管理后台启动
```bash
cd frontend-admin
npm install
npm run dev
```

访问 http://localhost:3000

### 微信小程序
使用微信开发者工具打开 `frontend-miniapp` 目录，配置 AppID。

### Docker 部署
```bash
docker-compose up -d
```

---

## 测试账号

| 用户名 | 密码 | 角色 | 可见数据 |
|--------|------|------|----------|
| admin | admin123 | 管理员 | 全部 |
| teacher1 | admin123 | 张老师 | 刘洋/孙悦/周杰（3人） |
| teacher2 | admin123 | 李辅导员 | 李明/王芳/张伟（3人） |
| teacher3 | admin123 | 王教授 | 陈静/赵磊（2人） |
| student1 | admin123 | 李明 | 自己的数据 |
| student2 | admin123 | 王芳 | 自己的数据 |
| student3~8 | admin123 | — | 各自自己的数据 |

---

## API 接口

所有接口前缀 `/api`，需认证的接口 Header 携带 `Authorization: Bearer {token}`。

### 认证
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 登录 |
| GET | /api/auth/info | 当前用户信息 |

### 学业管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/academic/students | 学生列表（分页，支持学号搜索） |
| GET | /api/academic/student/{id} | 学生详情 |
| GET | /api/academic/warnings | 预警列表（分页，支持学号搜索） |
| PUT | /api/academic/warning/{id}/read | 标记已读 |
| PUT | /api/academic/warning/{id}/handle | 处理预警 |
| GET/POST/PUT/DELETE | /api/academic/study-plans | 学习规划 CRUD |

### 就业服务
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/employ/resumes | 简历列表（分页） |
| GET | /api/employ/resume/{studentId} | 查询简历 |
| POST | /api/employ/resume/generate | AI 生成简历 |
| GET | /api/employ/jobs | 岗位列表（分页，关键字搜索） |
| POST/PUT/DELETE | /api/employ/job | 岗位 CRUD |
| POST | /api/employ/match | 人岗匹配 |
| POST | /api/employ/interview/start | 开始模拟面试 |
| POST | /api/employ/interview/submit | 提交面试答案 |
| GET | /api/employ/interviews | 面试列表（含岗位名称/学生姓名） |

### AI 智能体
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/ai/chat | AI 对话（支持真实 API + 规则引擎兜底） |
| GET | /api/ai/conversations/{userId} | 对话列表 |
| GET | /api/ai/conversation/{sessionId} | 对话详情 |
| GET/POST/PUT/DELETE | /api/ai/knowledge | 知识库 CRUD |

### 系统管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST/PUT/DELETE | /api/system/user | 用户 CRUD |
| GET | /api/system/role/all | 角色列表 |
| GET | /api/system/menu/tree | 菜单树 |

### WebSocket
| 路径 | 说明 |
|------|------|
| ws://localhost:8080/ws/notification/{userId} | 实时预警推送 |

---

## 数据权限

| 角色 | 学生列表 | 预警 | 学习规划 | 简历 | 面试记录 | 岗位管理 | 系统管理 |
|------|----------|------|----------|------|----------|----------|----------|
| 管理员 | 全部 | 全部 | 全部 | 全部 | 全部 | ✅ | ✅ |
| 教师 | 自己辅导的 | 自己辅导的 | 自己辅导的 | 自己辅导的 | 自己辅导的 | ✅ | ❌ |
| 学生 | 仅自己 | 仅自己 | 仅自己 | 仅自己 | 仅自己 | ❌ | ❌ |

---

## 许可证

MIT License
