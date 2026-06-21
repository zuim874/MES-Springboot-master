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
        wrapper.eq("is_deleted", "0");
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
        if (StringUtils.isEmpty(record.getIsDeleted())) {
            record.setIsDeleted("0");
        }

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
            checkWrapper.eq("is_deleted", "0");
            long count = warehouseService.count(checkWrapper);
            if (count > 0) {
                return Result.failure("库房编码已存在，请更换编码");
            }
        } else {
            // 编辑时校验编码唯一性（排除自身）
            QueryWrapper<Warehouse> checkWrapper = new QueryWrapper<>();
            checkWrapper.eq("code", record.getCode());
            checkWrapper.eq("is_deleted", "0");
            checkWrapper.ne("id", record.getId());
            long count = warehouseService.count(checkWrapper);
            if (count > 0) {
                return Result.failure("库房编码已存在，请更换编码");
            }
            // 编辑时不重新生成库位，避免数据丢失；若需要修改规格，建议删除重建
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
                        location.setIsDeleted("0");
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
        warehouse.setIsDeleted("1");
        warehouseService.updateById(warehouse);

        // 同步删除库位
        QueryWrapper<WarehouseLocation> wrapper = new QueryWrapper<>();
        wrapper.eq("warehouse_id", id);
        WarehouseLocation location = new WarehouseLocation();
        location.setIsDeleted("1");
        warehouseLocationService.update(location, wrapper);

        return Result.success();
    }

    @ApiOperation("根据库房ID查询库位列表")
    @GetMapping("/location-list")
    @ResponseBody
    public Result locationList(@RequestParam String warehouseId) {
        List<WarehouseLocation> list = warehouseLocationMapper.selectByWarehouseId(warehouseId);
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

            List<WarehouseLocationMateriel> bindList = locationMaterielService.listByLocationId(loc.getId());
            List<Map<String, Object>> materiels = new ArrayList<>();
            int totalQuantity = 0;
            for (WarehouseLocationMateriel bind : bindList) {
                Map<String, Object> m = new HashMap<>();
                m.put("materielId", bind.getMaterielId());
                m.put("quantity", bind.getQuantity());
                SpMaterile materiel = materileService.getById(bind.getMaterielId());
                if (materiel != null) {
                    m.put("materielCode", materiel.getMateriel());
                    m.put("materielDesc", materiel.getMaterielDesc());
                }
                materiels.add(m);
                totalQuantity += bind.getQuantity();
            }
            item.put("materiels", materiels);
            item.put("totalQuantity", totalQuantity);
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

        // 查询多物料列表
        List<WarehouseLocationMateriel> bindList = locationMaterielService.listByLocationId(locationId);
        List<Map<String, Object>> materielList = new ArrayList<>();
        for (WarehouseLocationMateriel bind : bindList) {
            Map<String, Object> item = new HashMap<>();
            item.put("materielId", bind.getMaterielId());
            item.put("quantity", bind.getQuantity());
            SpMaterile materiel = materileService.getById(bind.getMaterielId());
            if (materiel != null) {
                item.put("materielCode", materiel.getMateriel());
                item.put("materielDesc", materiel.getMaterielDesc());
            }
            materielList.add(item);
        }
        result.put("materiels", materielList);
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

        // 如果该库位已绑定其他物料，先解绑原物料
        String oldMaterielId = location.getMaterielId();
        if (StringUtils.isNotEmpty(oldMaterielId) && !oldMaterielId.equals(materielId)) {
            SpMaterile oldMateriel = materileService.getById(oldMaterielId);
            if (oldMateriel != null) {
                oldMateriel.setLocationId(null);
                oldMateriel.setWarehouseId(null);
                materileService.updateById(oldMateriel);
            }
        }

        // 更新库位绑定的物料
        location.setMaterielId(materielId);
        warehouseLocationService.updateById(location);

        // 更新物料的默认库位
        if (StringUtils.isNotEmpty(materielId)) {
            SpMaterile materiel = materileService.getById(materielId);
            if (materiel != null) {
                materiel.setLocationId(locationId);
                materiel.setWarehouseId(location.getWarehouseId());
                materileService.updateById(materiel);
            }
        }

        return Result.success();
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
        wrapper.eq("is_deleted", "0");
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

            // 查询多物料列表
            List<WarehouseLocationMateriel> bindList = locationMaterielService.listByLocationId(loc.getId());
            List<Map<String, Object>> materiels = new ArrayList<>();
            int totalQuantity = 0;
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
                }
                materiels.add(m);
            }
            item.put("materiels", materiels);
            item.put("totalQuantity", totalQuantity);
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
        warehouseLocationService.updateById(location);

        // 处理多物料绑定
        if (materielsJson != null) {
            List<WarehouseLocationMateriel> materielList = parseMaterielsJson(materielsJson);
            locationMaterielService.saveBindMateriels(locationId, materielList);

            // 更新location的materiel_id为主物料（兼容旧逻辑）
            if (!materielList.isEmpty()) {
                location.setMaterielId(materielList.get(0).getMaterielId());
            } else {
                location.setMaterielId(null);
            }
            warehouseLocationService.updateById(location);
        }
        return Result.success();
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
