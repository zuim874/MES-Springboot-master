package com.wangziyang.mes.system.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangziyang.mes.common.BaseController;
import com.wangziyang.mes.common.Result;
import com.wangziyang.mes.system.entity.Part;
import com.wangziyang.mes.system.service.IPartService;
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

/**
 * 零部件定义控制器
 */
@Controller
@RequestMapping("/admin/part")
public class PartController extends BaseController {

    @Autowired
    private IPartService partService;

    @ApiOperation("零部件定义列表UI")
    @GetMapping("/list-ui")
    public String listUI(Model model) {
        return "admin/part/list";
    }

    @ApiOperation("零部件分页列表")
    @PostMapping("/page")
    @ResponseBody
    public Result page(@RequestParam(required = false) String name,
                       @RequestParam(defaultValue = "1") long current,
                       @RequestParam(defaultValue = "10") long size) {
        QueryWrapper<Part> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(name)) {
            wrapper.like("name", name);
        }
        wrapper.eq("is_deleted", "0");
        wrapper.orderByAsc("code");
        IPage<Part> page = partService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size), wrapper);
        return Result.success(page);
    }

    @GetMapping("/add-or-update-ui")
    public String addOrUpdateUI(Model model, @RequestParam(required = false) String id) {
        if (StringUtils.isNotEmpty(id)) {
            Part record = partService.getById(id);
            model.addAttribute("result", record);
        }
        return "admin/part/addOrUpdate";
    }

    @PostMapping("/add-or-update")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result addOrUpdate(Part record) {
        if (StringUtils.isEmpty(record.getIsDeleted())) {
            record.setIsDeleted("0");
        }

        // 新增时自动生成零部件编号
        if (StringUtils.isEmpty(record.getId())) {
            String code = generatePartCode();
            record.setCode(code);
        } else {
            // 编辑时校验编码唯一性（排除自身）
            QueryWrapper<Part> checkWrapper = new QueryWrapper<>();
            checkWrapper.eq("code", record.getCode());
            checkWrapper.eq("is_deleted", "0");
            checkWrapper.ne("id", record.getId());
            long count = partService.count(checkWrapper);
            if (count > 0) {
                return Result.failure("零部件编号已存在");
            }
        }

        if (StringUtils.isEmpty(record.getName())) {
            return Result.failure("零部件名称不能为空");
        }

        partService.saveOrUpdate(record);
        return Result.success(record.getId());
    }

    @PostMapping("/delete")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result delete(@RequestParam String id) {
        Part part = partService.getById(id);
        if (part == null) {
            return Result.failure("零部件不存在");
        }
        part.setIsDeleted("1");
        partService.updateById(part);
        return Result.success();
    }

    /**
     * 自动生成零部件编号 BJ + 6位序号
     */
    private String generatePartCode() {
        String prefix = "BJ";
        QueryWrapper<Part> wrapper = new QueryWrapper<>();
        wrapper.apply("code LIKE {0}", prefix + "%");
        wrapper.orderByDesc("code");
        wrapper.last("LIMIT 1");
        Part max = partService.getOne(wrapper);
        int seq = 1;
        if (max != null && StringUtils.isNotEmpty(max.getCode())) {
            String numStr = max.getCode().substring(prefix.length());
            try {
                seq = Integer.parseInt(numStr) + 1;
            } catch (NumberFormatException e) {
                seq = 1;
            }
        }
        return prefix + String.format("%06d", seq);
    }
}
