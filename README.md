# MES-Springboot

基于 Spring Boot 构建的智能制造执行系统（MES），提供完整的生产流程管理解决方案。

## 技术栈

| 分类 | 技术 | 版本 | 说明 |
| :--- | :--- | :--- | :--- |
| 后端框架 | Spring Boot | 2.1.7.RELEASE | 核心框架 |
| ORM框架 | MyBatis-Plus | 3.1.2 | 简化CRUD操作 |
| 数据库 | MySQL | 8.x | 关系型数据库 |
| 缓存 | Redis | - | 缓存用户会话等 |
| 安全框架 | Apache Shiro | - | 权限控制 |
| 连接池 | Druid | 1.1.9 | 数据库连接池 |
| 前端框架 | Layui | - | UI组件库 |
| 模板引擎 | FreeMarker | - | 页面渲染 |
| API文档 | Swagger | - | 接口文档 |
| 图表库 | ECharts | - | 数据可视化 |
| 3D渲染 | Three.js | - | 数字孪生 |

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
│   ├── basedata/          # 基础数据模块（物料、数据表管理）
│   │   ├── controller/    # 控制器层
│   │   ├── entity/        # 实体类
│   │   ├── mapper/        # 数据访问层（MyBatis）
│   │   ├── service/       # 业务逻辑层（接口+实现）
│   │   └── request/       # 请求参数对象
│   ├── system/            # 系统管理模块（用户、角色、菜单、库房等）
│   │   ├── controller/    # 控制器层
│   │   ├── entity/        # 实体类
│   │   ├── mapper/        # 数据访问层
│   │   ├── service/       # 业务逻辑层
│   │   ├── request/       # 请求参数对象
│   │   ├── dto/           # 数据传输对象
│   │   └── enums/         # 枚举类
│   ├── order/             # 工单管理模块
│   ├── technology/        # 工艺管理模块
│   ├── digitization/      # 数字化平台模块（可视化）
│   ├── common/            # 公共模块（通用类）
│   │   ├── config/        # 配置类
│   │   ├── util/          # 工具类
│   │   └── advice/        # 全局异常处理
│   └── SparchetypeApplication.java  # Spring Boot启动类
├── src/main/resources/
│   ├── templates/         # FreeMarker模板文件
│   ├── static/            # 静态资源（CSS、JS、图片）
│   ├── mapper/            # MyBatis XML映射文件
│   ├── application.yml    # 主配置文件
│   └── application-dev.yml # 开发环境配置
└── pom.xml                # Maven依赖配置
```

## 核心架构设计

### 1. 通用模块设计

#### 1.1 BaseController - 基础控制器

**设计思路**：所有业务控制器继承此类，提供获取当前登录用户的能力。

**核心代码**：

```java
public class BaseController {
    public SysUser getSysUser() {
        return (SysUser) SecurityUtils.getSubject().getPrincipal();
    }
}
```

**说明**：通过 Shiro 的 `SecurityUtils.getSubject()` 获取当前登录用户信息，用于权限校验、操作日志记录等场景。

#### 1.2 Result - 通用返回结果

**设计思路**：统一 API 返回格式，简化前端处理。

**核心代码**：

```java
public class Result<T> extends HashMap<String, Object> {
    public static <T> Result<T> success(T data) {
        return restResult(data, 0, "操作成功");
    }
    
    public static <T> Result<T> failure(String msg) {
        return restResult(null, 1, msg);
    }
    
    private static <T> Result<T> restResult(T data, int code, String msg) {
        Result<T> apiResult = new Result<>();
        apiResult.put("code", code);
        apiResult.put("data", data);
        apiResult.put("msg", msg);
        return apiResult;
    }
}
```

**说明**：
- `code=0` 表示成功，`code=1` 表示失败
- `data` 存放业务数据
- `msg` 存放提示信息

#### 1.3 BaseEntity - 基础实体类

**设计思路**：所有实体类继承此类，提供通用字段和逻辑删除支持。

**核心字段**：
- `id`：主键（UUID）
- `createTime`：创建时间
- `createUsername`：创建人
- `updateTime`：更新时间
- `updateUsername`：更新人
- `deleted`：逻辑删除标记（0正常，1删除）

### 2. 系统管理模块

#### 2.1 用户管理

**设计思路**：基于 Shiro 实现用户认证和权限管理。

**核心代码**：

```java
@Controller("adminSysUserController")
@RequestMapping("/admin/sys/user")
public class SysUserController extends BaseController {
    @Autowired
    private ISysUserService sysUserService;
    
    @PostMapping("/page")
    @ResponseBody
    @RequiresPermissions("sys:user:view")
    public Result page(SysUserPageReq req) throws Exception {
        // 分页查询 + 补充部门名称和角色名称
        IPage page = sysUserService.page(req, qw);
        // ...
    }
}
```

**权限控制**：通过 `@RequiresPermissions` 注解控制接口访问权限。

#### 2.2 角色管理

**设计思路**：角色与权限的多对多关系，角色与用户的多对多关系。

**核心表结构**：
- `sp_sys_role`：角色表
- `sp_sys_role_menu`：角色-菜单权限关联表
- `sp_sys_user_role`：用户-角色关联表

#### 2.3 菜单管理

**设计思路**：菜单树结构，支持多级菜单和权限标识。

**核心字段**：
- `parentId`：父菜单ID
- `menuUrl`：菜单URL
- `authority`：权限标识（如 `user:edit`）
- `isMenu`：是否为菜单（0菜单，1按钮权限）

#### 2.4 权限控制流程

```
用户登录 → Shiro验证 → 获取角色 → 获取权限列表 → 前端根据权限显示按钮 → 后端接口权限校验
```

### 3. 物料管理模块

#### 3.1 物料基础数据

**设计思路**：物料档案管理，包含物料编码自动生成、属性定义等。

**核心代码**：

```java
@Controller
@RequestMapping("/basedata/materile")
public class SpMaterileController extends BaseController {
    @Autowired
    private ISpMaterileService iSpMaterileService;
    
    @PostMapping("/add-or-update")
    @ResponseBody
    public Result addOrUpdate(SpMaterile record) {
        // 默认值处理
        if (record.getLeadTime() == null) record.setLeadTime(1);
        if (record.getStock() == null) record.setStock(0);
        
        // 新增时自动生成物料编码
        if (StringUtils.isEmpty(record.getId())) {
            String code = generateMaterielCode(record.getMatType());
            record.setMateriel(code);
        }
        
        iSpMaterileService.saveOrUpdate(record);
        return Result.success(record.getId(), "操作成功");
    }
}
```

**物料编码规则**：根据物料类型自动生成唯一编码。

#### 3.2 库存同步机制

**设计思路**：物料库存与库位存储量保持数据一致性。

**核心代码** - 删除物料时同步清理库位：

```java
@Service
public class SpMaterileServiceImpl extends ServiceImpl<SpMaterileMapper, SpMaterile> implements ISpMaterileService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        // 1. 清空库位上的当前存放物料ID（使用 wrapper.set 强制更新 null）
        UpdateWrapper<WarehouseLocation> locationWrapper = new UpdateWrapper<>();
        locationWrapper.eq("materiel_id", id).set("materiel_id", null);
        warehouseLocationMapper.update(null, locationWrapper);

        // 2. 逻辑删除库位物料关联记录
        WarehouseLocationMateriel materielUpdate = new WarehouseLocationMateriel();
        materielUpdate.setIsDeleted("1");
        UpdateWrapper<WarehouseLocationMateriel> materielWrapper = new UpdateWrapper<>();
        materielWrapper.eq("materiel_id", id);
        warehouseLocationMaterielMapper.update(materielUpdate, materielWrapper);

        // 3. 逻辑删除物料
        return super.removeById(id);
    }
}
```

**进入库位界面时自动同步**：

```java
// WarehouseController.locationList() 方法中
List<WarehouseLocationMateriel> bindList = locationMaterielService.listByLocationId(loc.getId());
syncLocationStockToMaterielStock(bindList);  // 同步检测

// 同步方法逻辑
private void syncLocationStockToMaterielStock(List<WarehouseLocationMateriel> bindList) {
    for (WarehouseLocationMateriel bind : bindList) {
        SpMaterile materiel = materileService.getById(bind.getMaterielId());
        if (materiel != null) {
            Integer materielStock = materiel.getStock() != null ? materiel.getStock() : 0;
            if (materielStock < bind.getQuantity()) {
                bind.setQuantity(materielStock);  // 库位存储量不能超过物料实际库存
                locationMaterielService.updateById(bind);
            }
        }
    }
}
```

**同步规则**：
- 进入库存库位界面时自动检测
- 如果物料实际库存 < 库位存储量 → 将库位存储量同步为物料实际库存
- 如果物料实际库存 >= 库位存储量 → 保持不变（可能还未存入）

### 4. 库房库位管理模块

#### 4.1 库房管理

**设计思路**：库房作为库位的容器，支持按组/排/层/列自动生成库位。

**核心代码**：

```java
@PostMapping("/add-or-update")
@ResponseBody
@Transactional(rollbackFor = Exception.class)
public Result addOrUpdate(Warehouse record) {
    // 校验规格参数
    if (record.getGroupCount() == null || record.getGroupCount() < 1) {
        return Result.failure("组数至少为1");
    }
    // 保存库房并自动生成库位
    warehouseService.saveOrUpdate(record);
    return Result.success(record.getId(), "操作成功");
}
```

#### 4.2 库位物料绑定

**设计思路**：一个库位可以存放多种物料，通过关联表管理。

**核心代码**：

```java
@Service
public class WarehouseLocationMaterielServiceImpl extends ServiceImpl<WarehouseLocationMaterielMapper, WarehouseLocationMateriel> implements IWarehouseLocationMaterielService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBindMateriels(String locationId, List<WarehouseLocationMateriel> materiels) {
        // 查询当前有效的绑定关系
        List<WarehouseLocationMateriel> existingList = baseMapper.selectList(
                new QueryWrapper<WarehouseLocationMateriel>().eq("location_id", locationId)
        );
        
        // 处理新增/更新/删除
        for (WarehouseLocationMateriel item : materiels) {
            Integer qty = item.getQuantity() != null ? item.getQuantity() : 1;
            if (qty <= 0) {
                baseMapper.deleteById(existingMap.get(mid).getId());  // 删除
                continue;
            }
            if (existingMap.containsKey(mid)) {
                existing.setQuantity(qty);
                baseMapper.updateById(existing);  // 更新
            } else {
                baseMapper.insert(item);  // 新增
            }
        }
        
        // 同步更新库位的 materiel_id
        syncLocationMaterielId(locationId);
    }
}
```

**数据模型**：
- `sp_warehouse`：库房表
- `sp_warehouse_location`：库位表（含 group_num, row_num, layer_num, column_num）
- `sp_warehouse_location_materiel`：库位-物料关联表（含 quantity 存储量）

### 5. 工艺管理模块

#### 5.1 工艺路线管理

**设计思路**：定义产品的加工流程，支持工序排序和参数配置。

**核心表结构**：
- `sp_process_flow`：工艺路线表
- `sp_process_flow_detail`：工艺路线详情（工序列表）
- `sp_process`：工序定义表
- `sp_process_content`：工序内容表

### 6. 工单管理模块

#### 6.1 工单下达

**设计思路**：生产任务的创建和分解，支持工序级任务拆分。

**核心代码**：

```java
@Controller
@RequestMapping("/order/spOrder")
public class SpOrderController extends BaseController {
    @Autowired
    private ISpOrderService iSpOrderService;
    
    @PostMapping("/add-or-update")
    @ResponseBody
    public Result addOrUpdate(SpOrder record) {
        // 工单编号自动生成
        if (StringUtils.isEmpty(record.getId())) {
            record.setOrderNo(generateOrderNo());
        }
        iSpOrderService.saveOrUpdate(record);
        return Result.success(record.getId(), "操作成功");
    }
}
```

### 7. 数字化平台模块

#### 7.1 数据可视化

**设计思路**：使用 ECharts 实现生产数据的可视化展示。

**前端代码示例**：

```javascript
// 加载ECharts
layui.use(['echarts'], function() {
    var echarts = layui.echarts;
    var chart = echarts.init(document.getElementById('chart-container'));
    
    // 请求数据并渲染图表
    $.ajax({
        url: '/admin/digitization/data',
        success: function(res) {
            chart.setOption({
                title: { text: '产量统计' },
                tooltip: {},
                xAxis: { data: res.data.labels },
                yAxis: {},
                series: [{ type: 'bar', data: res.data.values }]
            });
        }
    });
});
```

#### 7.2 数字孪生仓库

**设计思路**：使用 Three.js 实现3D仓库可视化展示。

**技术要点**：
- 加载3D模型（OBJ格式）
- 实现第一人称视角控制
- 实时显示库位状态
- 支持交互操作

### 8. 设备管理模块

#### 8.1 设备档案

**设计思路**：设备信息的录入和管理，支持设备分组。

**核心表结构**：
- `sp_equipment`：设备表
- `sp_equipment_group`：设备组表
- `sp_equipment_group_item`：设备组-设备关联表

## API文档

启动项目后访问：`http://localhost:9090/swagger-ui.html`

## 关键技术实现

### 1. MyBatis-Plus 逻辑删除

**设计思路**：使用 MyBatis-Plus 的 `@TableLogic` 注解实现逻辑删除。

**核心代码**：

```java
@TableField(value = "is_deleted")
@TableLogic(value = "0", delval = "1")
private String deleted;
```

**说明**：删除操作自动将 `is_deleted` 改为 `1`，查询时自动过滤已删除记录。

### 2. MyBatis-Plus 分页插件

**设计思路**：配置分页拦截器，支持自动分页。

**配置代码**：

```java
@Configuration
public class MybatisPlusConfig {
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }
}
```

**使用示例**：

```java
IPage page = sysUserService.page(req, qw);
```

### 3. Shiro 权限注解

**设计思路**：使用 `@RequiresPermissions` 注解控制接口访问权限。

**使用示例**：

```java
@PostMapping("/delete")
@RequiresPermissions("user:delete")
public Result delete(String id) {
    sysUserService.removeById(id);
    return Result.success();
}
```

### 4. FreeMarker 模板引擎

**设计思路**：使用 FreeMarker 渲染页面，支持模板继承和自定义标签。

**Shiro权限标签**：

```ftl
<@shiro.hasPermission name="user:delete">
    <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="delete">删除</a>
</@shiro.hasPermission>
```

## 数据库设计

### ER图关系

```
用户(SysUser) <-- 角色(SysRole) <-- 菜单(SysMenu)
                    |
                    +-- 部门(SysDepartment)

物料(SpMaterile) <-- 库位物料(WarehouseLocationMateriel) --> 库位(WarehouseLocation) --> 库房(Warehouse)

工单(SpOrder) <-- 工艺路线(ProcessFlow) <-- 工序(Process)

设备(Equipment) <-- 设备组(EquipmentGroup)

产品BOM(ProductBom) <-- BOM节点(ProductBomNode)
```

### 核心表清单

| 表名 | 说明 | 核心字段 |
| :--- | :--- | :--- |
| `sp_sys_user` | 用户表 | id, username, password, dept_id |
| `sp_sys_role` | 角色表 | id, name, authority |
| `sp_sys_menu` | 菜单表 | id, parent_id, menu_url, authority |
| `sp_materile` | 物料表 | id, materiel, stock, location_id |
| `sp_warehouse` | 库房表 | id, name, group_count, row_count |
| `sp_warehouse_location` | 库位表 | id, warehouse_id, materiel_id |
| `sp_warehouse_location_materiel` | 库位物料关联表 | id, location_id, materiel_id, quantity |
| `sp_order` | 工表单 | id, order_no, status |
| `sp_process_flow` | 工艺路线表 | id, name, product_id |

## 开发规范

### 后端规范

1. **分层架构**：Controller → Service → Mapper
2. **命名约定**：
   - Controller：`XxxController`
   - Service接口：`IXxxService`
   - Service实现：`XxxServiceImpl`
   - Mapper：`XxxMapper`
   - Entity：`Xxx`
   - 请求参数：`XxxReq`
3. **分页查询**：使用 `BasePageReq` 子类
4. **枚举管理**：统一放在各模块的 `enums` 包下
5. **工具类**：使用 Hutool 工具库

### 前端规范

1. **组件库**：使用 Layui
2. **模板引擎**：使用 FreeMarker
3. **Ajax请求**：统一使用 `spUtil.submitForm` 或 `spUtil.ajax`
4. **表格组件**：使用 `spTable` 封装
5. **弹窗组件**：使用 `spLayer` 封装
6. **图表**：使用 ECharts

## 部署说明

### 开发环境

```bash
cd mes
mvn spring-boot:run
```

### 生产环境

```bash
cd mes
mvn clean package
java -jar target/mes-1.0.0.jar --spring.profiles.active=pro
```

### Docker部署

```bash
docker build -t mes .
docker run -p 9090:9090 mes
```

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request。