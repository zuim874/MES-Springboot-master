package com.wangziyang.mes.system.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangziyang.mes.basedata.entity.SpMaterile;
import com.wangziyang.mes.basedata.service.ISpMaterileService;
import com.wangziyang.mes.common.BaseController;
import com.wangziyang.mes.common.Result;
import com.wangziyang.mes.system.entity.Warehouse;
import com.wangziyang.mes.system.entity.WarehouseLocation;
import com.wangziyang.mes.system.mapper.WarehouseLocationMapper;
import com.wangziyang.mes.system.request.WarehousePageReq;
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
        return Result.success(list);
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
        if (StringUtils.isNotEmpty(location.getMaterielId())) {
            SpMaterile materiel = materileService.getById(location.getMaterielId());
            result.put("materiel", materiel);
        }
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
                                  @RequestParam(defaultValue = "1") Integer layerNum) {
        QueryWrapper<WarehouseLocation> wrapper = new QueryWrapper<>();
        wrapper.eq("warehouse_id", warehouseId);
        wrapper.eq("row_num", rowNum);
        wrapper.eq("layer_num", layerNum);
        wrapper.eq("is_deleted", "0");
        wrapper.orderByAsc("column_num");
        List<WarehouseLocation> list = warehouseLocationService.list(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (WarehouseLocation loc : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", loc.getId());
            item.put("code", loc.getCode());
            item.put("columnNum", loc.getColumnNum());
            item.put("materielId", loc.getMaterielId());
            if (StringUtils.isNotEmpty(loc.getMaterielId())) {
                SpMaterile materiel = materileService.getById(loc.getMaterielId());
                if (materiel != null) {
                    item.put("materielCode", materiel.getMateriel());
                    item.put("materielDesc", materiel.getMaterielDesc());
                }
            }
            result.add(item);
        }
        return Result.success(result);
    }
}
