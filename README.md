# 智能校园失物招领平台

基于 **SpringBoot + Vue3 + YOLOv8 + DeepSeek** 的智能失物招领系统。
实现「拾物登记(AI自动分类) → 失主文字检索(AI语义匹配) → 确认匹配 → 线下领取」的完整业务闭环。

## 技术栈

| 层次 | 技术 |
|------|------|
| 前端 | Vue3 + Vite + Element Plus + ECharts + Axios |
| 后端 | Spring Boot 3.2 + MyBatis-Plus + Spring Security + JWT |
| AI | YOLOv8 (Flask微服务) + DeepSeek Embedding/Chat API |
| 数据库 | MySQL 8.0 + Redis 7.0 |
| 部署 | Docker Compose |

## 项目结构

```
campus-lost-found/
├── backend/          # Spring Boot 后端
├── frontend/         # Vue3 前端
├── ai-service/       # Flask YOLO 微服务
├── docker/           # Docker 部署编排 + SQL初始化
├── docs/             # 文档
└── README.md
```

## 快速开始

### 1. 环境要求
- JDK 17+
- Node.js 18+
- Python 3.11+
- Docker & Docker Compose (可选)

### 2. Docker 一键部署（推荐）

```bash
# 克隆项目
cd campus-lost-found

# 配置DeepSeek API Key（编辑 docker/mysql/init.sql 中的 deepseek_api_key）

# 启动所有服务
docker compose -f docker/docker-compose.yml up -d

# 访问
# 失主端: http://localhost
# 管理端: http://localhost/admin/login
```

### 3. 本地开发

**启动MySQL和Redis**（可使用Docker）：
```bash
docker run -d --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root mysql:8.0
docker run -d --name redis -p 6379:6379 redis:7-alpine
```

**启动Flask YOLO服务**：
```bash
cd ai-service
pip install -r requirements.txt
python app.py
```

**启动后端**：
```bash
cd backend
mvn spring-boot:run
```

**启动前端**：
```bash
cd frontend
npm install
npm run dev
```

### 4. 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 工作人员 | staff01 | staff123 |
| 失主 | 自行注册 | - |

## API文档

启动后端后访问：`http://localhost:8080/doc.html` (Knife4j)

## 核心功能

- 🔍 **智能检索**：自然语言描述丢失物品，DeepSeek语义匹配
- 📷 **AI识别**：上传物品图片，YOLO自动识别类别
- 📊 **数据看板**：统计拾物数据、热门类别、趋势图表
- 👥 **三角色管理**：失主、工作人员、管理员
- 🐳 **Docker部署**：一键启动全部服务
