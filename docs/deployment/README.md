# 部署指南

## 环境要求

| 软件 | 最低版本 |
|------|----------|
| JDK | 21+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |
| Docker | 20.10+ (可选) |
| Maven | 3.8+ (本地构建) |

## 本地开发部署

### 1. 初始化数据库

```bash
mysql -u root -p < scripts/init-db.sql
```

### 2. 启动后端

```bash
cd backend
./mvnw spring-boot:run
# 或: mvn spring-boot:run
```

后端启动后访问：
- API文档：http://localhost:8080/doc.html
- 健康检查：http://localhost:8080/actuator/health

### 3. 启动管理后台

```bash
cd frontend-admin
npm install
npm run dev
```

访问：http://localhost:3000

默认账号：admin / admin123

### 4. 启动微信小程序

1. 下载并安装[微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 导入 `frontend-miniapp` 目录
3. 配置 AppID（测试可用测试号）
4. 在开发工具中配置服务器域名（开发阶段可关闭域名校验）

## Docker 一键部署

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

服务端口：
- 管理后台：http://localhost (80)
- 后端 API：http://localhost:8080
- MySQL：localhost:3306
- Redis：localhost:6379

## 生产环境注意事项

1. **数据库密码**：修改 `docker-compose.yml` 和 `application.yml` 中的默认密码
2. **JWT 密钥**：修改 `application.yml` 中的 `jwt.secret`，使用强随机字符串
3. **AI API Key**：配置金蝶苍穹 AI 平台的真实 API Key
4. **HTTPS**：生产环境应配置 SSL 证书
5. **日志**：配置日志收集（ELK/Loki）
6. **监控**：接入 Prometheus + Grafana
7. **备份**：定期备份 MySQL 数据和上传文件
