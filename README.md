# Ai-Campus-Academic-Job —— AI 原生校园学业就业智能服务平台

> 🏆 第十五届中国软件杯 A6 赛题参赛项目
> 
> 基于金蝶 AI 苍穹平台，以 AI 智能体为核心，打造覆盖学生全校园周期的学业管理与就业服务一体化智能平台。

## ✨ 核心功能

### 🎓 学业管理
- 📊 **学业智能预警**：实时监测学业数据，主动推送预警通知
- 📖 **个性化学习规划**：结合培养方案与学业画像，生成定制化学习路径

### 💼 就业服务
- 📄 **AI 简历生成与优化**：一键生成专业简历，智能优化内容匹配岗位
- 🎯 **智能职位匹配**：基于学生画像与招聘信息库的精准双向匹配
- 🎤 **AI 模拟面试**：支持文字对话与语音通话双模式，语音模式通过微信小程序实时交互

### 📊 三端协同
- **学生端**：微信小程序，轻量化获取学业与求职服务
- **教师/管理员端**：PC 管理后台，实时掌握学情与就业进展，开展精准帮扶
- **校方大屏**：数据可视化，全局监测、态势分析与决策支撑

## 🏗️ 技术架构

| 层级 | 技术栈 |
|------|--------|
| **后端** | Spring Boot 3.x + MyBatis Plus + JWT + WebSocket |
| **前端管理后台** | Vue 3 + Vite + Element Plus + Pinia |
| **微信小程序** | 原生小程序框架 + 语音识别 / 语音合成插件 |
| **AI 能力** | 金蝶苍穹 AI 平台（大模型 + RAG 检索增强生成） |
| **数据层** | MySQL 8.0 + Redis + Elasticsearch |
| **部署** | Docker + Nginx（支持私有化部署与 SaaS 多租户） |

## 📦 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+

### 后端启动
```bash
cd backend
./mvnw spring-boot:run
```
### 管理后台启动
```bash
cd frontend-admin
npm install
npm run dev
```

### 微信小程序启动
使用微信开发者工具打开 frontend-miniapp 目录

配置 AppID 与服务器域名

### 📁 项目结构

```bash
Ai-Campus-Academic-Job/
├── backend/                         # 后端 Spring Boot 服务
│   ├── src/                         # 源代码
│   └── pom.xml                      # Maven 配置
├── frontend-admin/                  # 教师/管理员 PC 管理后台（Vue3）
│   ├── src/                         # 源代码
│   └── package.json
├── frontend-miniapp/                # 微信小程序端
│   ├── pages/                       # 页面
│   └── app.json                     # 小程序配置
├── ai-agent/                        # AI 智能体调度中心
│   ├── rag/                         # RAG 检索增强模块
│   ├── llm/                         # 大模型调用封装
│   └── knowledge-base/              # 知识库管理
├── docs/                            # 项目文档
│   ├── api/                         # API 接口文档
│   ├── architecture/                # 架构设计文档
│   └── deployment/                  # 部署指南
├── scripts/                         # 构建/部署脚本
├── .github/                         # GitHub 工作流配置
│   ├── workflows/                   # CI/CD Actions
│   └── ISSUE_TEMPLATE/              # Issue 模板
├── README.md                        # 项目主文档
├── LICENSE                          # MIT License
└── CHANGELOG.md                     # 版本变更记录
```

## 📄 许可证
本项目采用 MIT License，详见 LICENSE 文件。