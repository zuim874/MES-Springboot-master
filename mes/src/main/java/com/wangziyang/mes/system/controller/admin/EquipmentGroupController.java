package com.wangziyang.mes.system.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangziyang.mes.common.BaseController;
import com.wangziyang.mes.common.Result;
import com.wangziyang.mes.system.entity.Equipment;
import com.wangziyang.mes.system.entity.EquipmentGroup;
import com.wangziyang.mes.system.entity.EquipmentGroupItem;
import com.wangziyang.mes.system.mapper.EquipmentGroupItemMapper;
import com.wangziyang.mes.system.request.EquipmentGroupPageReq;
import com.wangziyang.mes.system.service.IEquipmentGroupItemService;
import com.wangziyang.mes.system.service.IEquipmentGroupService;
import com.wangziyang.mes.system.service.IEquipmentService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
 * <p>
 * 设备编组前端控制器
 * </p>
 */
@Controller
@RequestMapping("/admin/equipment/group")
public class EquipmentGroupController extends BaseController {

    @Autowired
    private IEquipmentGroupService equipmentGroupService;

    @Autowired
    private IEquipmentGroupItemService equipmentGroupItemService;

    @Autowired
    private EquipmentGroupItemMapper equipmentGroupItemMapper;

    @Autowired
    private IEquipmentService equipmentService;

    @ApiOperation("设备编组列表UI")
    @GetMapping("/list-ui")
    public String listUI(Model model) {
        return "admin/equipment/group/list";
    }

    @ApiOperation("设备编组分页列表")
    @PostMapping("/page")
    @ResponseBody
    public Result page(EquipmentGroupPageReq req) {
        QueryWrapper<EquipmentGroup> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(req.getName())) {
            wrapper.like("name", req.getName());
        }
        if (StringUtils.isNotEmpty(req.getCode())) {
            wrapper.like("code", req.getCode());
        }
        wrapper.eq("is_deleted", "0");
        wrapper.orderByAsc("code");
        IPage result = equipmentGroupService.page(req, wrapper);
        return Result.success(result);
    }

    @GetMapping("/add-or-update-ui")
    public String addOrUpdateUI(Model model, @RequestParam(required = false) String id) {
        if (StringUtils.isNotEmpty(id)) {
            EquipmentGroup record = equipmentGroupService.getById(id);
            model.addAttribute("result", record);
        }
        return "admin/equipment/group/addOrUpdate";
    }

    @PostMapping("/add-or-update")
    @ResponseBody
    public Result addOrUpdate(EquipmentGroup record, String isDeleted) {
        if (StringUtils.isNotEmpty(isDeleted)) {
            record.setIsDeleted(isDeleted);
        } else {
            record.setIsDeleted("0");
        }
        equipmentGroupService.saveOrUpdate(record);
        return Result.success(record.getId());
    }

    @GetMapping("/bind-equipment-ui")
    public String bindEquipmentUI(Model model, @RequestParam(required = false) String groupId) {
        model.addAttribute("groupId", groupId);
        return "admin/equipment/group/bindEquipment";
    }

    @GetMapping("/equipment-list")
    @ResponseBody
    public Result equipmentList() {
        QueryWrapper<Equipment> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", "0");
        List<Equipment> list = equipmentService.list(wrapper);
        return Result.success(list);
    }

    @GetMapping("/bind-equipment-ids")
    @ResponseBody
    public Result bindEquipmentIds(@RequestParam String groupId) {
        List<String> equipmentIds = equipmentGroupItemMapper.selectEquipmentIdsByGroupId(groupId);
        return Result.success(equipmentIds);
    }

    @PostMapping("/save-bind-equipments")
    @ResponseBody
    public Result saveBindEquipments(@RequestParam String groupId, @RequestParam(value = "equipmentIds", required = false) List<String> equipmentIds) {
        if (StringUtils.isEmpty(groupId)) {
            return Result.failure("编组ID不能为空");
        }
        equipmentGroupItemService.saveBindEquipments(groupId, equipmentIds);
        return Result.success();
    }
}
