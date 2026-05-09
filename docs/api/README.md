# API 接口文档

后端启动后访问 Knife4j 文档页面：`http://localhost:8080/doc.html`

## 接口概览

### 认证接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/auth/login` | 用户登录 | 否 |
| GET | `/api/auth/info` | 获取当前用户信息 | 是 |

### 学业管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/academic/students` | 学生列表（分页） |
| GET | `/api/academic/student/{id}` | 学生详情 |
| GET | `/api/academic/warnings` | 预警列表（分页） |
| PUT | `/api/academic/warning/{id}/read` | 标记已读 |
| PUT | `/api/academic/warning/{id}/handle` | 处理预警 |

### 就业服务

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/employ/resume/{studentId}` | 查询简历 |
| POST | `/api/employ/resume` | 保存简历 |
| POST | `/api/employ/resume/generate` | AI生成简历 |
| GET | `/api/employ/jobs` | 岗位列表 |
| POST | `/api/employ/job` | 新增岗位 |
| PUT | `/api/employ/job` | 更新岗位 |
| DELETE | `/api/employ/job/{id}` | 删除岗位 |
| POST | `/api/employ/match` | 人岗匹配 |
| POST | `/api/employ/interview/start` | 开始面试 |
| POST | `/api/employ/interview/submit` | 提交面试 |

### AI 智能体

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/ai/chat` | AI对话 |
| GET | `/api/ai/conversations/{userId}` | 对话历史 |
| GET | `/api/ai/knowledge` | 知识库列表 |

### 系统管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST/PUT/DELETE | `/api/system/user` | 用户CRUD |
| GET | `/api/system/role/all` | 角色列表 |
| GET | `/api/system/menu/tree` | 菜单树 |

## 认证方式

所有需认证接口在 Header 中携带：`Authorization: Bearer {token}`

## 统一返回格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

分页返回：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "page": 1,
    "size": 10,
    "records": []
  }
}
```
