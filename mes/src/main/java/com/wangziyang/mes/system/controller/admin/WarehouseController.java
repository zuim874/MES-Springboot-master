package com.wangziyang.mes.system.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangziyang.mes.basedata.entity.SpMaterile;
import com.wangziyang.mes.basedata.service.ISpMaterileService;
import com.wangziyang.mes.common.BaseController;
import com.wangziyang.mes.common.Result;
import com.wangziyang.mes.system.entity.Warehouse;
import com.wangziyang.mes.system.entity.WarehouseLocation;
import com.wangziyang.mes.system.entity.WarehouseLocationMateriel;
import com.wangziyang.mes.system.mapper.WarehouseLocationMapper;
import com.wangziyang.mes.system.request.WarehousePageReq;
import com.wangziyang.mes.system.service.IWarehouseLocationMaterielService;
import com.wangziyang.mes.system.service.IWarehouseLocationService;
import com.wangziyang.mes.system.service.IWarehouseService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库房库位定义控制器
 */
@Controller
@RequestMapping("/admin/warehouse")
public class WarehouseController extends BaseController {

    @Autowired
    private IWarehouseService warehouseService;

    @Autowired
    private IWarehouseLocationService warehouseLocationService;

    @Autowired
    private WarehouseLocationMapper warehouseLocationMapper;

    @Autowired
    private ISpMaterileService materileService;

    @Autowired
    private IWarehouseLocationMaterielService locationMaterielService;

    @ApiOperation("库房库位定义列表UI")
    @GetMapping("/list-ui")
    public String listUI(Model model) {
        return "admin/warehouse/list";
    }

    @ApiOperation("库房分页列表")
    @PostMapping("/page")
    @ResponseBody
    public Result page(WarehousePageReq req) {
        QueryWrapper<Warehouse> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(req.getName())) {
            wrapper.like("name", req.getName());
        }
        if (StringUtils.isNotEmpty(req.getCode())) {
            wrapper.like("code", req.getCode());
        }
        if (StringUtils.isNotEmpty(req.getType())) {
            wrapper.eq("type", req.getType());
        }
        wrapper.orderByAsc("code");
        IPage result = warehouseService.page(req, wrapper);
        return Result.success(result);
    }

    @GetMapping("/add-or-update-ui")
    public String addOrUpdateUI(Model model, @RequestParam(required = false) String id) {
        if (StringUtils.isNotEmpty(id)) {
            Warehouse record = warehouseService.getById(id);
            model.addAttribute("result", record);
        }
        return "admin/warehouse/addOrUpdate";
    }

    @PostMapping("/add-or-update")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result addOrUpdate(Warehouse record) {
        // 校验规格参数
        if (record.getGroupCount() == null || record.getGroupCount() < 1) {
            return Result.failure("组数至少为1");
        }
        if (record.getRowCount() == null || record.getRowCount() < 1) {
            return Result.failure("排数至少为1");
        }
        if (record.getLayerCount() == null || record.getLayerCount() < 1) {
            return Result.failure("层数至少为1");
        }
        if (record.getColumnCount() == null || record.getColumnCount() < 1) {
            return Result.failure("列数至少为1");
        }

        // 新增时校验编码唯一性
        if (StringUtils.isEmpty(record.getId())) {
            QueryWrapper<Warehouse> checkWrapper = new QueryWrapper<>();
            checkWrapper.eq("code", record.getCode());
            long count = warehouseService.count(checkWrapper);
            if (count > 0) {
                return Result.failure("库房编码已存在，请更换编码");
            }
        } else {
            // 编辑时校验编码唯一性（排除自身）
            QueryWrapper<Warehouse> checkWrapper = new QueryWrapper<>();
            checkWrapper.eq("code", record.getCode());
            checkWrapper.ne("id", record.getId());
            long count = warehouseService.count(checkWrapper);
            if (count > 0) {
                return Result.failure("库房编码已存在，请更换编码");
            }
        }

        boolean isNew = StringUtils.isEmpty(record.getId());
        warehouseService.saveOrUpdate(record);

        // 新增时自动生成库位
        if (isNew) {
            generateLocations(record);
        }

        return Result.success(record.getId());
    }

    /**
     * 根据库房规格自动生成库位
     */
    private void generateLocations(Warehouse warehouse) {
        String warehouseCode = warehouse.getCode();
        for (int g = 1; g <= warehouse.getGroupCount(); g++) {
            for (int r = 1; r <= warehouse.getRowCount(); r++) {
                for (int l = 1; l <= warehouse.getLayerCount(); l++) {
                    for (int c = 1; c <= warehouse.getColumnCount(); c++) {
                        WarehouseLocation location = new WarehouseLocation();
                        location.setWarehouseId(warehouse.getId());
                        location.setCode(warehouseCode + "-" + g + "-" + r + "-" + l + "-" + c);
                        location.setGroupNum(g);
                        location.setRowNum(r);
                        location.setLayerNum(l);
                        location.setColumnNum(c);
                        location.setLength(warehouse.getDefaultLength() != null ? warehouse.getDefaultLength() : 50);
                        location.setWidth(warehouse.getDefaultWidth() != null ? warehouse.getDefaultWidth() : 50);
                        location.setHeight(warehouse.getDefaultHeight() != null ? warehouse.getDefaultHeight() : 50);
                        warehouseLocationService.save(location);
                    }
                }
            }
        }
    }

    @PostMapping("/delete")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result delete(@RequestParam String id) {
        Warehouse warehouse = warehouseService.getById(id);
        if (warehouse == null) {
            return Result.failure("库房不存在");
        }
        // 逻辑删除库房（@TableLogic会自动将is_deleted设为1）
        warehouseService.removeById(id);

        // 同步逻辑删除该库房下的所有库位
        QueryWrapper<WarehouseLocation> wrapper = new QueryWrapper<>();
        wrapper.eq("warehouse_id", id);
        List<WarehouseLocation> locations = warehouseLocationService.list(wrapper);
        for (WarehouseLocation loc : locations) {
            warehouseLocationService.removeById(loc.getId());
        }

        return Result.success("删除成功");
    }

    @ApiOperation("根据库房ID查询库位列表")
    @GetMapping("/location-list")
    @ResponseBody
    public Result locationList(@RequestParam String warehouseId) {
        QueryWrapper<WarehouseLocation> locationWrapper = new QueryWrapper<>();
        locationWrapper.eq("warehouse_id", warehouseId);
        locationWrapper.orderByAsc("group_num", "row_num", "layer_num", "column_num");
        List<WarehouseLocation> list = warehouseLocationService.list(locationWrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (WarehouseLocation loc : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", loc.getId());
            item.put("code", loc.getCode());
            item.put("groupNum", loc.getGroupNum());
            item.put("rowNum", loc.getRowNum());
            item.put("layerNum", loc.getLayerNum());
            item.put("columnNum", loc.getColumnNum());
            item.put("length", loc.getLength());
            item.put("width", loc.getWidth());
            item.put("height", loc.getHeight());
            item.put("materielId", loc.getMaterielId());

            // 计算库位容积
            long locationVolume = (long)(loc.getLength() != null ? loc.getLength() : 50)
                    * (loc.getWidth() != null ? loc.getWidth() : 50)
                    * (loc.getHeight() != null ? loc.getHeight() : 50);

            List<WarehouseLocationMateriel> bindList = locationMaterielService.listByLocationId(loc.getId());
            List<Map<String, Object>> materiels = new ArrayList<>();
            int totalQuantity = 0;
            long usedVolume = 0;
            for (WarehouseLocationMateriel bind : bindList) {
                Map<String, Object> m = new HashMap<>();
                m.put("materielId", bind.getMaterielId());
                m.put("quantity", bind.getQuantity());
                SpMaterile materiel = materileService.getById(bind.getMaterielId());
                if (materiel != null) {
                    m.put("materielCode", materiel.getMateriel());
                    m.put("materielDesc", materiel.getMaterielDesc());
                    long matVolume = (long)(materiel.getLength() != null ? materiel.getLength() : 0)
                            * (materiel.getWidth() != null ? materiel.getWidth() : 0)
                            * (materiel.getHeight() != null ? materiel.getHeight() : 0);
                    usedVolume += matVolume * bind.getQuantity();
                    m.put("materielLength", materiel.getLength());
                    m.put("materielWidth", materiel.getWidth());
                    m.put("materielHeight", materiel.getHeight());
                }
                materiels.add(m);
                totalQuantity += bind.getQuantity();
            }
            item.put("materiels", materiels);
            item.put("totalQuantity", totalQuantity);
            item.put("locationVolume", locationVolume);
            item.put("usedVolume", usedVolume);
            item.put("volumeUtilization", locationVolume > 0 ? Math.round(usedVolume * 100.0 / locationVolume) : 0);
            result.add(item);
        }
        return Result.success(result);
    }

    @ApiOperation("查询库位详情（含物料信息）")
    @GetMapping("/location-detail")
    @ResponseBody
    public Result locationDetail(@RequestParam String locationId) {
        WarehouseLocation location = warehouseLocationService.getById(locationId);
        if (location == null) {
            return Result.failure("库位不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("location", location);

        // 计算库位容积
        long locationVolume = (long)(location.getLength() != null ? location.getLength() : 50)
                * (location.getWidth() != null ? location.getWidth() : 50)
                * (location.getHeight() != null ? location.getHeight() : 50);
        result.put("locationVolume", locationVolume);

        // 查询多物料列表
        List<WarehouseLocationMateriel> bindList = locationMaterielService.listByLocationId(locationId);
        List<Map<String, Object>> materielList = new ArrayList<>();
        long usedVolume = 0;
        for (WarehouseLocationMateriel bind : bindList) {
            Map<String, Object> item = new HashMap<>();
            item.put("materielId", bind.getMaterielId());
            item.put("quantity", bind.getQuantity());
            SpMaterile materiel = materileService.getById(bind.getMaterielId());
            if (materiel != null) {
                item.put("materielCode", materiel.getMateriel());
                item.put("materielDesc", materiel.getMaterielDesc());
                item.put("materielLength", materiel.getLength());
                item.put("materielWidth", materiel.getWidth());
                item.put("materielHeight", materiel.getHeight());
                long matVolume = (long)(materiel.getLength() != null ? materiel.getLength() : 0)
                        * (materiel.getWidth() != null ? materiel.getWidth() : 0)
                        * (materiel.getHeight() != null ? materiel.getHeight() : 0);
                usedVolume += matVolume * bind.getQuantity();
                item.put("singleVolume", matVolume);
                item.put("totalVolume", matVolume * bind.getQuantity());
            }
            materielList.add(item);
        }
        result.put("materiels", materielList);
        result.put("usedVolume", usedVolume);
        result.put("remainingVolume", locationVolume - usedVolume);
        result.put("volumeUtilization", locationVolume > 0 ? Math.round(usedVolume * 100.0 / locationVolume) : 0);
        return Result.success(result);
    }

    @ApiOperation("将物料绑定到库位")
    @PostMapping("/bind-materiel")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result bindMateriel(@RequestParam String locationId, @RequestParam(required = false) String materielId) {
        WarehouseLocation location = warehouseLocationService.getById(locationId);
        if (location == null) {
            return Result.failure("库位不存在");
        }

        // 构建物料列表（兼容旧版单物料绑定）
        List<WarehouseLocationMateriel> materielList = new ArrayList<>();
        if (StringUtils.isNotEmpty(materielId)) {
            SpMaterile materiel = materileService.getById(materielId);
            if (materiel != null) {
                // 校验物料尺寸是否适合库位
                int matL = materiel.getLength() != null ? materiel.getLength() : 0;
                int matW = materiel.getWidth() != null ? materiel.getWidth() : 0;
                int matH = materiel.getHeight() != null ? materiel.getHeight() : 0;
                int locL = location.getLength() != null ? location.getLength() : 50;
                int locW = location.getWidth() != null ? location.getWidth() : 50;
                int locH = location.getHeight() != null ? location.getHeight() : 50;

                if (matL > 0 && (matL > locL || matW > locW || matH > locH)) {
                    return Result.failure("物料【" + materiel.getMateriel() + "】尺寸("
                            + matL + "×" + matW + "×" + matH + "cm)超出库位尺寸("
                            + locL + "×" + locW + "×" + locH + "cm)，无法存放");
                }

                // 校验体积是否超出库位容积
                long locationVolume = (long) locL * locW * locH;
                long matVolume = (long) matL * matW * matH;
                long usedVolume = 0;
                List<WarehouseLocationMateriel> existingBindList = locationMaterielService.listByLocationId(locationId);
                for (WarehouseLocationMateriel existing : existingBindList) {
                    SpMaterile existingMat = materileService.getById(existing.getMaterielId());
                    if (existingMat != null) {
                        long emv = (long)(existingMat.getLength() != null ? existingMat.getLength() : 0)
                                * (existingMat.getWidth() != null ? existingMat.getWidth() : 0)
                                * (existingMat.getHeight() != null ? existingMat.getHeight() : 0);
                        usedVolume += emv * existing.getQuantity();
                    }
                }
                if (locationVolume > 0 && (usedVolume + matVolume) > locationVolume) {
                    return Result.failure("物料体积(" + matVolume + "cm³)加入后总容积将超出库位容积(" + locationVolume + "cm³)，无法存放");
                }

                // 校验实际库存
                if (materiel.getStock() != null) {
                    int totalInOtherLocations = getTotalQuantityByMaterielIdExcludeLocation(materielId, locationId);
                    int newTotal = totalInOtherLocations + 1;
                    if (newTotal > materiel.getStock()) {
                        return Result.failure("物料【" + materiel.getMaterielDesc() + "】实际库存为" + materiel.getStock()
                                + "，所有库位总存放量将达到" + newTotal + "，超出实际库存");
                    }
                }

                WarehouseLocationMateriel item = new WarehouseLocationMateriel();
                item.setMaterielId(materielId);
                item.setQuantity(1);
                materielList.add(item);
            }
        }

        locationMaterielService.saveBindMateriels(locationId, materielList);
        // saveBindMateriels 内部已自动同步 materiel_id，无需重复处理

        return Result.success("保存成功");
    }

    @ApiOperation("获取库位二维矩阵数据（用于可视化展示）")
    @GetMapping("/location-matrix")
    @ResponseBody
    public Result locationMatrix(@RequestParam String warehouseId,
                                  @RequestParam(defaultValue = "1") Integer rowNum,
                                  @RequestParam(required = false) Integer layerNum) {
        QueryWrapper<WarehouseLocation> wrapper = new QueryWrapper<>();
        wrapper.eq("warehouse_id", warehouseId);
        wrapper.eq("row_num", rowNum);
        if (layerNum != null) {
            wrapper.eq("layer_num", layerNum);
        }
        wrapper.orderByAsc("layer_num");
        wrapper.orderByAsc("column_num");
        List<WarehouseLocation> list = warehouseLocationService.list(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (WarehouseLocation loc : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", loc.getId());
            item.put("code", loc.getCode());
            item.put("columnNum", loc.getColumnNum());
            item.put("layerNum", loc.getLayerNum());
            item.put("length", loc.getLength());
            item.put("width", loc.getWidth());
            item.put("height", loc.getHeight());
            item.put("materielId", loc.getMaterielId());

            // 计算库位容积
            long locationVolume = (long)(loc.getLength() != null ? loc.getLength() : 50)
                    * (loc.getWidth() != null ? loc.getWidth() : 50)
                    * (loc.getHeight() != null ? loc.getHeight() : 50);

            // 查询多物料列表
            List<WarehouseLocationMateriel> bindList = locationMaterielService.listByLocationId(loc.getId());
            List<Map<String, Object>> materiels = new ArrayList<>();
            int totalQuantity = 0;
            long usedVolume = 0;
            for (WarehouseLocationMateriel bind : bindList) {
                Map<String, Object> m = new HashMap<>();
                m.put("materielId", bind.getMaterielId());
                m.put("quantity", bind.getQuantity());
                totalQuantity += bind.getQuantity();
                SpMaterile materiel = materileService.getById(bind.getMaterielId());
                if (materiel != null) {
                    m.put("materielCode", materiel.getMateriel());
                    m.put("materielDesc", materiel.getMaterielDesc());
                    m.put("materielLength", materiel.getLength());
                    m.put("materielWidth", materiel.getWidth());
                    m.put("materielHeight", materiel.getHeight());
                    long matVolume = (long)(materiel.getLength() != null ? materiel.getLength() : 0)
                            * (materiel.getWidth() != null ? materiel.getWidth() : 0)
                            * (materiel.getHeight() != null ? materiel.getHeight() : 0);
                    usedVolume += matVolume * bind.getQuantity();
                    m.put("singleVolume", matVolume);
                    m.put("totalVolume", matVolume * bind.getQuantity());
                }
                materiels.add(m);
            }
            item.put("materiels", materiels);
            item.put("totalQuantity", totalQuantity);
            item.put("locationVolume", locationVolume);
            item.put("usedVolume", usedVolume);
            item.put("volumeUtilization", locationVolume > 0 ? Math.round(usedVolume * 100.0 / locationVolume) : 0);
            if (!materiels.isEmpty()) {
                item.put("materielCode", materiels.get(0).get("materielCode"));
                item.put("materielDesc", materiels.get(0).get("materielDesc"));
            }
            result.add(item);
        }
        return Result.success(result);
    }

    @ApiOperation("更新库位信息（尺寸、物料绑定等）")
    @PostMapping("/location-update")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result locationUpdate(@RequestParam String locationId,
                                  @RequestParam(required = false) Integer length,
                                  @RequestParam(required = false) Integer width,
                                  @RequestParam(required = false) Integer height,
                                  @RequestParam(required = false) String materielsJson) {
        WarehouseLocation location = warehouseLocationService.getById(locationId);
        if (location == null) {
            return Result.failure("库位不存在");
        }
        if (length != null) {
            location.setLength(length);
        }
        if (width != null) {
            location.setWidth(width);
        }
        if (height != null) {
            location.setHeight(height);
        }

        // 处理多物料绑定，并进行体积校验
        if (materielsJson != null) {
            List<WarehouseLocationMateriel> materielList = parseMaterielsJson(materielsJson);

            // 计算库位容积
            long locationVolume = (long)(location.getLength() != null ? location.getLength() : 50)
                    * (location.getWidth() != null ? location.getWidth() : 50)
                    * (location.getHeight() != null ? location.getHeight() : 50);

            // 校验：每个物料尺寸不能超过库位尺寸，且总体积不能超过库位容积
            long totalUsedVolume = 0;
            for (WarehouseLocationMateriel item : materielList) {
                if (item.getMaterielId() == null || item.getMaterielId().trim().isEmpty()) {
                    continue;
                }
                SpMaterile materiel = materileService.getById(item.getMaterielId());
                if (materiel == null) {
                    continue;
                }
                // 校验单物料尺寸是否适合库位
                int matL = materiel.getLength() != null ? materiel.getLength() : 0;
                int matW = materiel.getWidth() != null ? materiel.getWidth() : 0;
                int matH = materiel.getHeight() != null ? materiel.getHeight() : 0;
                int locL = location.getLength() != null ? location.getLength() : 50;
                int locW = location.getWidth() != null ? location.getWidth() : 50;
                int locH = location.getHeight() != null ? location.getHeight() : 50;

                if (matL > 0 && (matL > locL || matW > locW || matH > locH)) {
                    return Result.failure("物料【" + materiel.getMateriel() + "】尺寸("
                            + matL + "×" + matW + "×" + matH + "cm)超出库位尺寸("
                            + locL + "×" + locW + "×" + locH + "cm)，无法存放");
                }
                // 计算该物料占用的体积
                long matVolume = (long)matL * matW * matH;
                totalUsedVolume += matVolume * item.getQuantity();
            }

            if (locationVolume > 0 && totalUsedVolume > locationVolume) {
                return Result.failure("物料总体积(" + totalUsedVolume + "cm³)超出库位容积(" + locationVolume + "cm³)，容积利用率达"
                        + Math.round(totalUsedVolume * 100.0 / locationVolume) + "%，请减少物料数量或更换更大库位");
            }

            // 校验实际库存
            for (WarehouseLocationMateriel item : materielList) {
                if (item.getMaterielId() == null || item.getMaterielId().trim().isEmpty() || item.getQuantity() == null) {
                    continue;
                }
                SpMaterile materiel = materileService.getById(item.getMaterielId());
                if (materiel == null || materiel.getStock() == null) {
                    continue;
                }
                int totalInOtherLocations = getTotalQuantityByMaterielIdExcludeLocation(item.getMaterielId(), locationId);
                int newTotal = totalInOtherLocations + item.getQuantity();
                if (newTotal > materiel.getStock()) {
                    return Result.failure("物料【" + materiel.getMaterielDesc() + "】实际库存为" + materiel.getStock()
                            + "，所有库位总存放量将达到" + newTotal + "，超出实际库存");
                }
            }

            locationMaterielService.saveBindMateriels(locationId, materielList);
            // saveBindMateriels 内部已自动同步 materiel_id，无需重复处理
        }
        warehouseLocationService.updateById(location);
        return Result.success("保存成功");
    }

    /**
     * 查询某物料在所有库位中的总存放数量（排除指定库位）
     */
    private int getTotalQuantityByMaterielIdExcludeLocation(String materielId, String excludeLocationId) {
        QueryWrapper<WarehouseLocationMateriel> wrapper = new QueryWrapper<>();
        wrapper.eq("materiel_id", materielId);
        wrapper.eq("is_deleted", "0");
        if (excludeLocationId != null) {
            wrapper.ne("location_id", excludeLocationId);
        }
        List<WarehouseLocationMateriel> list = locationMaterielService.list(wrapper);
        int total = 0;
        for (WarehouseLocationMateriel item : list) {
            total += item.getQuantity() != null ? item.getQuantity() : 0;
        }
        return total;
    }

    /**
     * 解析前端传递的物料JSON字符串
     */
    private List<WarehouseLocationMateriel> parseMaterielsJson(String materielsJson) {
        List<WarehouseLocationMateriel> result = new ArrayList<>();
        if (StringUtils.isEmpty(materielsJson)) {
            return result;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            result = mapper.readValue(materielsJson, mapper.getTypeFactory().constructCollectionType(List.class, WarehouseLocationMateriel.class));
        } catch (Exception e) {
            // 如果解析失败，尝试简单解析
            String[] items = materielsJson.split(",");
            for (String item : items) {
                String[] parts = item.split(":");
                if (parts.length >= 2) {
                    WarehouseLocationMateriel m = new WarehouseLocationMateriel();
                    m.setMaterielId(parts[0]);
                    try {
                        m.setQuantity(Integer.parseInt(parts[1]));
                    } catch (NumberFormatException ex) {
                        m.setQuantity(1);
                    }
                    result.add(m);
                }
            }
        }
        return result;
    }
}
