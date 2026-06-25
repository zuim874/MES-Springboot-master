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

**设计思路**：一个库位可以存放多种物料，通过关联表管理。使用物理删除而非逻辑删除来避免 `(location_id, materiel_id)` 唯一索引冲突——当用户删除某物料后重新添加同名物料时，逻辑删除会导致旧记录残留，新记录插入失败。

**核心代码**：

```java
@Service
public class WarehouseLocationMaterielServiceImpl extends ServiceImpl<WarehouseLocationMaterielMapper, WarehouseLocationMateriel> implements IWarehouseLocationMaterielService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBindMateriels(String locationId, List<WarehouseLocationMateriel> materiels) {
        // 查询当前有效的绑定关系
        Map<String, WarehouseLocationMateriel> existingMap = new HashMap<>();
        for (WarehouseLocationMateriel e : baseMapper.selectList(
                new QueryWrapper<WarehouseLocationMateriel>().eq("location_id", locationId))) {
            existingMap.put(e.getMaterielId(), e);
        }

        Set<String> newMaterielIds = new HashSet<>();
        for (WarehouseLocationMateriel item : materiels) {
            String mid = item.getMaterielId().trim();
            Integer qty = item.getQuantity() != null ? item.getQuantity() : 1;

            if (qty <= 0) {
                // 物理删除，避免唯一索引残留
                baseMapper.physicalDeleteById(existingMap.get(mid).getId());
                continue;
            }

            newMaterielIds.add(mid);
            if (existingMap.containsKey(mid)) {
                existingMap.get(mid).setQuantity(qty);
                baseMapper.updateById(existingMap.get(mid));  // 更新
            } else {
                baseMapper.physicalDeleteByLocationIdAndMaterielId(locationId, mid);  // 清理旧记录
                baseMapper.insert(item);  // 新增
            }
        }

        // 物理删除旧列表中有但新列表中没有的物料
        for (WarehouseLocationMateriel existing : existingList) {
            if (!newMaterielIds.contains(existing.getMaterielId())) {
                baseMapper.physicalDeleteById(existing.getId());
            }
        }

        syncLocationMaterielId(locationId);  // 同步库位主物料ID
    }
}
```

**Mapper 新增物理删除方法**：

```java
public interface WarehouseLocationMaterielMapper extends BaseMapper<WarehouseLocationMateriel> {
    @Delete("DELETE FROM sp_warehouse_location_materiel WHERE id = #{id}")
    int physicalDeleteById(@Param("id") String id);

    @Delete("DELETE FROM sp_warehouse_location_materiel WHERE location_id = #{locationId} AND materiel_id = #{materielId}")
    int physicalDeleteByLocationIdAndMaterielId(@Param("locationId") String locationId, @Param("materielId") String materielId);
}
```

**关键设计决策**：关联表不使用 `@TableLogic` 逻辑删除，因为 `(location_id, materiel_id)` 有唯一索引。逻辑删除会将 `is_deleted` 设为 `1`，但记录仍然存在，导致后续重新添加相同物料时报唯一键冲突。因此对该表的所有删除操作均使用自定义物理删除 SQL。

**数据模型**：
- `sp_warehouse`：库房表
- `sp_warehouse_location`：库位表（含 group_num, row_num, layer_num, column_num）
- `sp_warehouse_location_materiel`：库位-物料关联表（含 quantity 存储量，唯一索引 `(location_id, materiel_id)`）

### 5. 工艺管理模块

#### 5.1 工艺流程管理（ProcessPlanController）

**设计思路**：基于产品 BOM 树结构，为每个 BOM 节点绑定一个工序流程定义（ProcessFlow），形成完整的工艺路线。工序流程定义内已包含所有零散工序，因此工艺规划中不再需要单独选择工序，直接选择工序流程定义即可。

**核心数据模型**：
- `ProductBom`（产品BOM）：定义产品结构树
- `ProductBomNode`（BOM节点）：BOM 树的每个节点，通过 `parentId` 形成父子关系
- `ProcessPlan`（工艺规划）：BOM 节点与工序流程定义的绑定关系
- `ProcessFlow`（工序流程定义）：包含多个工序的有序流程

**上级工序流程绑定**：通过 BOM 节点的父子关系自动确定——当前节点的父节点所绑定的工艺规划中的工序流程定义，即为当前节点的"上级工序流程"。

**核心代码**：

```java
@Controller
@RequestMapping("/admin/processPlan")
public class ProcessPlanController extends BaseController {

    @PostMapping("/node-list")
    @ResponseBody
    public Result nodeList(@RequestParam String bomId) {
        // 1. 查询BOM节点树
        List<ProductBomNode> nodes = nodeService.list(wrapper);

        // 2. 查询所有工艺规划，按 bomNodeId 建立索引
        Map<String, ProcessPlan> planMap = plans.stream()
                .collect(Collectors.toMap(ProcessPlan::getBomNodeId, p -> p));

        // 3. 组装数据
        for (ProductBomNode node : nodes) {
            ProcessPlan plan = planMap.get(node.getId());
            if (plan != null) {
                item.put("flowId", plan.getFlowId());
                // 工序流程定义内已包含所有零散工序，展示流程名称即可
                if (StringUtils.isNotEmpty(plan.getFlowId())) {
                    ProcessFlow flow = processFlowService.getById(plan.getFlowId());
                    item.put("flowName", flow.getName());
                }
            }

            // 4. 上级工序流程：通过父节点BOM关系确定
            if (StringUtils.isNotEmpty(node.getParentId())) {
                ProcessPlan parentPlan = planMap.get(node.getParentId());
                if (parentPlan != null && StringUtils.isNotEmpty(parentPlan.getFlowId())) {
                    ProcessFlow parentFlow = processFlowService.getById(parentPlan.getFlowId());
                    item.put("parentFlowName", parentFlow.getName());
                }
            }
        }
        return Result.success(result);
    }
}
```

**关键设计决策**：
- 工艺规划不再保存单独的 `processId`，而是通过 `flowId` 关联工序流程定义
- 工序流程定义（`sp_process_flow` + `sp_process_flow_detail`）已经包含完整的工序列表，无需在工艺规划中重复选择
- 上级工序流程通过 BOM 节点的 `parentId` 关系自动推导，而非手动指定单个工序

#### 5.2 工艺内容编制（ProcessContentController）

**设计思路**：为已绑定了工艺规划的 BOM 节点编制详细的工艺内容（如加工参数、检验标准等）。使用 `flowId` 而非 `processId` 来关联工序流程，确保数据一致性——当工序被删除后，编制内容仍能正确关联到工序流程。

**核心代码**：

```java
@Controller
@RequestMapping("/admin/processContent")
public class ProcessContentController extends BaseController {

    @PostMapping("/node-list")
    @ResponseBody
    public Result nodeList(@RequestParam String bomId) {
        // 查询工艺规划，通过 flowId 获取流程名称
        ProcessPlan plan = planMap.get(node.getId());
        if (plan != null && StringUtils.isNotEmpty(plan.getFlowId())) {
            ProcessFlow flow = processFlowService.getById(plan.getFlowId());
            if (flow != null) {
                item.put("flowName", flow.getName());  // 展示流程名称
            }
        }

        // 查询工艺内容编制状态
        ProcessContent content = contentMap.get(node.getId());
        if (content != null) {
            item.put("contentId", content.getId());
            item.put("contentStatus", content.getStatus());  // 编制状态
        }
        return Result.success(result);
    }
}
```

**关键设计决策**：
- 使用 `flowId` 代替 `processId` 关联工序流程，避免工序被删除后编制内容无法编辑
- 工艺内容编制依赖工艺规划中已绑定的工序流程定义，确保数据一致性

#### 5.3 工艺路线管理

**设计思路**：定义产品的加工流程，支持工序排序和参数配置。

**核心表结构**：
- `sp_process_flow`：工艺路线表
- `sp_process_flow_detail`：工艺路线详情（工序列表）
- `sp_process`：工序定义表
- `sp_process_content`：工序内容表
- `sp_process_plan`：工艺规划表

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

### 9. 绑定唯一性约束

#### 9.1 用户-班组一对一绑定

**设计思路**：一个用户只能绑定到一个员工班组。当用户被添加到新班组时，自动从旧班组中移除。

**核心代码**：

```java
@Service
public class WorkTeamUserServiceImpl extends ServiceImpl<WorkTeamUserMapper, WorkTeamUser> implements IWorkTeamUserService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBindUsers(String teamId, List<String> userIds) {
        // 1. 先删除该班组已有的绑定关系
        QueryWrapper<WorkTeamUser> wrapper = new QueryWrapper<>();
        wrapper.eq("team_id", teamId);
        baseMapper.delete(wrapper);

        // 2. 插入新的绑定关系
        if (userIds != null && !userIds.isEmpty()) {
            // 约束：一个用户只能绑定到一个班组，先清除这些用户在所有其他班组的绑定
            for (String userId : userIds) {
                QueryWrapper<WorkTeamUser> userWrapper = new QueryWrapper<>();
                userWrapper.eq("user_id", userId);
                baseMapper.delete(userWrapper);
            }
            // 插入当前班组的新绑定
            for (String userId : userIds) {
                WorkTeamUser tu = new WorkTeamUser();
                tu.setId(UUID.randomUUID().toString().replace("-", ""));
                tu.setTeamId(teamId);
                tu.setUserId(userId);
                tu.setIsDeleted("0");
                baseMapper.insert(tu);
            }
        }
    }
}
```

#### 9.2 设备-编组一对一绑定

**设计思路**：一台设备只能绑定到一个设备编组。当设备被分配到新编组时，自动从旧编组中移除。

**核心代码**：

```java
@Service
public class EquipmentGroupItemServiceImpl extends ServiceImpl<EquipmentGroupItemMapper, EquipmentGroupItem> implements IEquipmentGroupItemService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBindEquipments(String groupId, List<String> equipmentIds) {
        // 1. 先删除该编组已有的绑定关系
        QueryWrapper<EquipmentGroupItem> wrapper = new QueryWrapper<>();
        wrapper.eq("group_id", groupId);
        baseMapper.delete(wrapper);

        // 2. 插入新的绑定关系
        if (equipmentIds != null && !equipmentIds.isEmpty()) {
            // 约束：一台设备只能绑定到一个编组，先清除这些设备在所有其他编组的绑定
            for (String equipmentId : equipmentIds) {
                QueryWrapper<EquipmentGroupItem> eqWrapper = new QueryWrapper<>();
                eqWrapper.eq("equipment_id", equipmentId);
                baseMapper.delete(eqWrapper);
            }
            // 插入当前编组的新绑定
            for (String equipmentId : equipmentIds) {
                EquipmentGroupItem item = new EquipmentGroupItem();
                item.setId(UUID.randomUUID().toString().replace("-", ""));
                item.setGroupId(groupId);
                item.setEquipmentId(equipmentId);
                item.setIsDeleted("0");
                baseMapper.insert(item);
            }
        }
    }
}
```

**关键设计决策**：在每次保存绑定关系时，先清除选中用户/设备在所有班组/编组中的历史绑定，再插入新绑定。整个过程在 `@Transactional` 事务中执行，保证数据一致性。

## 数据库初始化

### 存储过程：安全添加列

**设计思路**：SQL 初始化脚本中的 `AddColumnIfNotExists` 存储过程，在添加列前先检查列是否存在，避免重复执行脚本时报 `Duplicate column` 错误。

**核心代码**：

```sql
DELIMITER $$
DROP PROCEDURE IF EXISTS AddColumnIfNotExists$$
CREATE PROCEDURE AddColumnIfNotExists(
    IN tableName VARCHAR(128),
    IN columnName VARCHAR(128),
    IN columnDefinition VARCHAR(1024)
)
BEGIN
    DECLARE colCount INT;
    -- 去除字段名中的反引号后再查询
    SET columnName = REPLACE(columnName, '`', '');
    SELECT COUNT(*) INTO colCount
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = tableName
      AND COLUMN_NAME = columnName;
    IF colCount = 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', tableName, '` ADD COLUMN `', columnName, '` ', columnDefinition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;
```

**关键修复**：调用该方法时字段名可能带有反引号（如 `` `source` ``），导致 `INFORMATION_SCHEMA.COLUMNS` 匹配失败。通过 `REPLACE(columnName, '`', '')` 去除反引号后再查询，确保列存在性检查正确。

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
用户(SysUser) --绑定--> 班组(WorkTeam)
    |
    +-- 角色(SysRole) <-- 菜单(SysMenu)
    +-- 部门(SysDepartment)

物料(SpMaterile) <-- 库位物料(WarehouseLocationMateriel) --> 库位(WarehouseLocation) --> 库房(Warehouse)

工单(SpOrder) <-- 工艺路线(ProcessFlow) <-- 工序(Process)

设备(Equipment) --绑定--> 设备编组(EquipmentGroup)

产品BOM(ProductBom) <-- BOM节点(ProductBomNode) --工艺规划--> 工艺规划(ProcessPlan) --关联--> 工序流程定义(ProcessFlow)
```

### 核心表清单

| 表名 | 说明 | 核心字段 |
| :--- | :--- | :--- |
| `sp_sys_user` | 用户表 | id, username, password, dept_id |
| `sp_sys_role` | 角色表 | id, name, authority |
| `sp_sys_menu` | 菜单表 | id, parent_id, menu_url, authority |
| `sp_work_team` | 员工班组表 | id, code, name |
| `sp_work_team_user` | 班组-用户关联表 | id, team_id, user_id |
| `sp_materile` | 物料表 | id, materiel, stock |
| `sp_warehouse` | 库房表 | id, name, group_count, row_count |
| `sp_warehouse_location` | 库位表 | id, warehouse_id, materiel_id |
| `sp_warehouse_location_materiel` | 库位物料关联表 | id, location_id, materiel_id, quantity |
| `sp_order` | 工单表 | id, order_no, status |
| `sp_process_flow` | 工序流程定义表 | id, name, product_id |
| `sp_process_flow_detail` | 工序流程详情表 | id, flow_id, process_id, sort_num |
| `sp_process` | 工序定义表 | id, code, name |
| `sp_process_plan` | 工艺规划表 | id, bom_id, bom_node_id, flow_id |
| `sp_process_content` | 工艺内容编制表 | id, bom_id, bom_node_id, flow_id, status |
| `sp_equipment` | 设备表 | id, code, name |
| `sp_equipment_group` | 设备编组表 | id, code, name |
| `sp_equipment_group_item` | 设备编组-设备关联表 | id, group_id, equipment_id |
| `sp_product_bom` | 产品BOM表 | id, name |
| `sp_product_bom_node` | BOM节点表 | id, bom_id, parent_id, node_code |

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