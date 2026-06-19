package com.wangziyang.mes.system.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangziyang.mes.common.BaseController;
import com.wangziyang.mes.common.Result;
import com.wangziyang.mes.order.entity.SpOrder;
import com.wangziyang.mes.order.service.ISpOrderService;
import com.wangziyang.mes.system.entity.Equipment;
import com.wangziyang.mes.system.request.EquipmentPageReq;
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

import java.util.List;

/**
 * <p>
 * 生产设备前端控制器
 * </p>
 */
@Controller
@RequestMapping("/admin/equipment")
public class EquipmentController extends BaseController {

    @Autowired
    private IEquipmentService equipmentService;

    @Autowired
    private ISpOrderService spOrderService;

    @ApiOperation("生产设备列表UI")
    @GetMapping("/list-ui")
    public String listUI(Model model) {
        return "admin/equipment/list";
    }

    @ApiOperation("生产设备分页列表")
    @PostMapping("/page")
    @ResponseBody
    public Result page(EquipmentPageReq req) {
        QueryWrapper<Equipment> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(req.getName())) {
            wrapper.like("name", req.getName());
        }
        if (StringUtils.isNotEmpty(req.getCode())) {
            wrapper.like("code", req.getCode());
        }
        wrapper.eq("is_deleted", "0");
        wrapper.orderByAsc("code");
        IPage result = equipmentService.page(req, wrapper);
        return Result.success(result);
    }

    @GetMapping("/add-or-update-ui")
    public String addOrUpdateUI(Model model, @RequestParam(required = false) String id) {
        if (StringUtils.isNotEmpty(id)) {
            Equipment record = equipmentService.getById(id);
            model.addAttribute("result", record);
        }
        return "admin/equipment/addOrUpdate";
    }

    @PostMapping("/add-or-update")
    @ResponseBody
    public Result addOrUpdate(Equipment record, String isDeleted) {
        if (StringUtils.isNotEmpty(isDeleted)) {
            record.setIsDeleted(isDeleted);
        }
        equipmentService.saveOrUpdate(record);
        return Result.success(record.getId());
    }

    @PostMapping("/delete")
    @ResponseBody
    public Result delete(@RequestParam String id) {
        QueryWrapper<SpOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("equipment_id", id);
        wrapper.in("statue", 1, 2);
        List<SpOrder> orders = spOrderService.list(wrapper);
        if (orders != null && !orders.isEmpty()) {
            return Result.failure("该设备存在下发的生产作业，不允许删除");
        }
        Equipment record = equipmentService.getById(id);
        if (record != null) {
            record.setIsDeleted("1");
            equipmentService.updateById(record);
        }
        return Result.success();
    }
}
