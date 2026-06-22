# MES-Springboot

基于 Spring Boot 构建的智能制造执行系统（MES），提供完整的生产流程管理解决方案。

## 技术栈

| 分类 | 技术 | 版本 |
| :--- | :--- | :--- |
| 后端框架 | Spring Boot | 2.1.7.RELEASE |
| ORM框架 | MyBatis-Plus | 3.1.2 |
| 数据库 | MySQL | 8.x |
| 缓存 | Redis | - |
| 安全框架 | Apache Shiro | - |
| 连接池 | Druid | 1.1.9 |
| 前端框架 | Layui | - |
| 模板引擎 | FreeMarker | - |
| API文档 | Swagger | - |
| 图表库 | ECharts | - |

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis（可选）

### 数据库配置

1. 创建数据库：

```sql
CREATE DATABASE dus CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

2. 初始化数据库：

执行 `scripts/sql/MySQL-20210225.sql` 脚本

3. 修改数据库连接配置：

编辑 `mes/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dus?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Hongkong&allowPublicKeyRetrieval=true
    username: your_username
    password: your_password
```

### 启动项目

```bash
cd mes
mvn spring-boot:run
```

启动成功后访问：`http://localhost:9090`

### 登录信息

- 用户名：admin
- 密码：123

## 项目结构

```
mes/
├── src/main/java/com/wangziyang/mes/
│   ├── basedata/          # 基础数据模块
│   │   ├── controller/    # 控制器
│   │   ├── entity/        # 实体类
│   │   ├── mapper/        # 数据访问层
│   │   ├── service/       # 业务逻辑层
│   │   └── request/       # 请求参数
│   ├── system/            # 系统管理模块
│   │   ├── controller/    # 控制器
│   │   ├── entity/        # 实体类
│   │   ├── mapper/        # 数据访问层
│   │   ├── service/       # 业务逻辑层
│   │   └── request/       # 请求参数
│   ├── order/             # 工单管理模块
│   ├── technology/        # 工艺管理模块
│   ├── digitization/      # 数字化平台模块
│   ├── common/            # 公共模块
│   │   ├── config/        # 配置类
│   │   ├── util/          # 工具类
│   │   └── advice/        # 全局异常处理
│   └── SparchetypeApplication.java  # 启动类
├── src/main/resources/
│   ├── templates/         # FreeMarker模板
│   ├── static/            # 静态资源
│   ├── mapper/            # MyBatis映射文件
│   ├── application.yml    # 主配置文件
│   └── application-dev.yml # 开发环境配置
└── pom.xml                # Maven配置
```

## 功能模块

### 系统管理

- 用户管理：系统用户配置、密码管理
- 组织管理：部门架构管理
- 菜单管理：系统菜单配置、权限标识
- 角色管理：角色权限分配、数据范围控制
- 字典管理：固定数据维护

### 工艺管理

- 工艺路线管理：生产流程定义
- BOM管理：产品物料清单
- 工艺文件管理：SOP文档管理

### 计划管理

- 工单下达：生产任务创建
- 工单分解：工序级任务拆分

### 物料管理

- 物料基础数据：物料信息维护
- 库存库位管理：库房库位定义与物料存放
- 库存同步：物料库存与库位存储量自动同步

### 设备管理

- 设备档案：设备信息录入
- 设备维保：维修保养记录

### 在制品管理

- 工序过站：生产过程记录

### 数字化平台

- 数据可视化：ECharts图表展示
- 数字孪生：3D仓库展示（Three.js）

## API文档

启动项目后访问：`http://localhost:9090/swagger-ui.html`

## 核心业务说明

### 库存同步机制

物料库存（`sp_materile.stock`）与库位存储量（`warehouse_location_materiel.quantity`）通过以下方式保持同步：

1. 进入库存库位界面时，系统自动检测并同步：
   - 如果物料实际库存 < 库位存储量，将库位存储量同步为物料实际库存
   - 如果物料实际库存 >= 库位存储量，保持不变（可能还未存入）

2. 删除物料时，自动清空关联库位上的物料绑定关系

### 逻辑删除

系统采用逻辑删除机制，删除操作不会物理删除数据，而是将 `is_deleted` 字段标记为 `1`。

## 开发规范

### 后端

- 使用 MyBatis-Plus 提供的通用 CRUD 方法
- 分页查询参数使用 `BasePageReq` 子类
- 枚举类统一管理在各模块的 `enums` 包下
- 使用 Hutool 工具类库简化开发

### 前端

- 使用 Layui 组件库构建界面
- 使用 FreeMarker 模板引擎渲染页面
- Ajax 请求统一使用 `spUtil.submitForm`
- 图表使用 ECharts

## 目录说明

| 目录 | 说明 |
| :--- | :--- |
| docs/ | 开发文档 |
| mes/src/main/java/ | Java源代码 |
| mes/src/main/resources/ | 配置文件和静态资源 |
| scripts/sql/ | 数据库初始化脚本 |

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request。