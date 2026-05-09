# Ai-Campus-Academic-Job —— AI 原生校园学业就业智能服务平台

> 第十五届中国软件杯 A6 赛题参赛项目
> 基于金蝶 AI 苍穹平台，打造覆盖学生全校园周期的学业管理与就业服务一体化智能平台。

## 核心功能

### 学业管理
- **学业智能预警**：实时监测学业数据，主动推送预警通知
- **个性化学习规划**：结合培养方案与学业画像，生成定制化学习路径

### 就业服务
- **AI 简历生成与优化**：一键生成专业简历，智能优化内容匹配岗位
- **智能职位匹配**：基于学生画像与招聘信息库的精准双向匹配
- **AI 模拟面试**：支持文字对话与语音通话双模式

### 三端协同
- **学生端**：微信小程序，轻量化获取学业与求职服务
- **教师/管理员端**：PC 管理后台，实时掌握学情与就业进展
- **校方大屏**：数据可视化，全局监测与决策支撑

## 技术架构

| 层级 | 技术栈 |
|------|--------|
| 后端 | Spring Boot 3.2 + MyBatis Plus 3.5 + JWT + WebSocket |
| 前端管理后台 | Vue 3 + Vite + Element Plus + Pinia + ECharts |
| 微信小程序 | 原生小程序框架 + 语音识别插件 |
| AI 能力 | 金蝶苍穹 AI 平台（大模型 + RAG 检索增强生成） |
| 数据层 | MySQL 8.0 + Redis + Elasticsearch |
| 部署 | Docker + Nginx |

## 快速开始

### 环境要求

| 软件 | 版本 |
|------|------|
| JDK | 21+ |
| Node.js | 18+ |
| MySQL | 8.0+（开发环境可选，内置 H2） |
| Redis | 6.0+（开发环境可选） |

### 后端启动

```bash
cd backend
./mvnw spring-boot:run
# 或手动：mvn package -DskipTests && java -jar target/acaj-1.0.0-SNAPSHOT.jar
```

开发环境内置 H2 内存数据库，无需安装 MySQL。启动后访问：
- API 文档：http://localhost:8080/doc.html
- H2 控制台：http://localhost:8080/h2-console（JDBC URL: `jdbc:h2:mem:acaj`）

### 管理后台启动

```bash
cd frontend-admin
npm install
npm run dev
```

访问 http://localhost:3000，默认账号 `admin`，密码 `admin123`。

### 微信小程序启动

使用微信开发者工具打开 `frontend-miniapp` 目录，配置 AppID 与服务器域名。

### Docker 部署

```bash
docker-compose up -d
```

## 项目结构

```
├── backend/                    # Spring Boot 后端
│   └── src/main/java/com/soyorim/acaj/
│       ├── common/             # 统一返回、分页、异常处理
│       ├── config/             # 安全、MyBatis、WebSocket 配置
│       └── module/
│           ├── system/         # 用户/角色/菜单 + 认证
│           ├── academic/       # 学生画像、成绩、预警、学习规划
│           ├── employment/     # 简历、岗位、人岗匹配、模拟面试
│           └── ai/             # AI 对话、知识库
├── frontend-admin/             # Vue 3 管理后台
├── frontend-miniapp/           # 微信小程序
├── ai-agent/                   # AI 智能体（Python）
│   ├── llm/                    # 大模型调用封装 + 提示词模板
│   ├── rag/                    # 文档加载/切分/检索
│   └── knowledge-base/         # 知识库文档
├── docs/                       # API / 架构 / 部署文档
├── scripts/init-db.sql         # MySQL 数据库初始化
└── docker-compose.yml          # 容器化部署
```

## 默认测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 超级管理员 |
| teacher1 | admin123 | 教师 |
| student1 | admin123 | 学生 |

## 许可证

MIT License
