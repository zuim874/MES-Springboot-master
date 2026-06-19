package com.wangziyang.mes.system.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangziyang.mes.basedata.entity.SpMaterile;
import com.wangziyang.mes.basedata.service.ISpMaterileService;
import com.wangziyang.mes.common.BaseController;
import com.wangziyang.mes.common.Result;
import com.wangziyang.mes.system.entity.ProductBom;
import com.wangziyang.mes.system.entity.ProductBomItem;
import com.wangziyang.mes.system.service.IProductBomItemService;
import com.wangziyang.mes.system.service.IProductBomService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 产品BOM管理控制器
 */
@Controller
@RequestMapping("/admin/productBom")
public class ProductBomController extends BaseController {

    @Autowired
    private IProductBomService productBomService;

    @Autowired
    private IProductBomItemService productBomItemService;

    @Autowired
    private ISpMaterileService spMaterileService;

    @ApiOperation("产品BOM列表UI")
    @GetMapping("/list-ui")
    public String listUI(Model model) {
        return "admin/productBom/list";
    }

    @ApiOperation("产品BOM分页列表")
    @PostMapping("/page")
    @ResponseBody
    public Result page(@RequestParam(required = false) String bomCode,
                       @RequestParam(required = false) String productMaterielDesc,
                       @RequestParam(defaultValue = "1") long current,
                       @RequestParam(defaultValue = "10") long size) {
        QueryWrapper<ProductBom> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(bomCode)) {
            wrapper.like("bom_code", bomCode);
        }
        if (StringUtils.isNotEmpty(productMaterielDesc)) {
            wrapper.like("product_materiel_desc", productMaterielDesc);
        }
        wrapper.eq("is_deleted", "0");
        wrapper.orderByDesc("create_time");
        IPage<ProductBom> page = productBomService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size), wrapper);
        return Result.success(page);
    }

    @GetMapping("/add-or-update-ui")
    public String addOrUpdateUI(Model model, @RequestParam(required = false) String id) {
        if (StringUtils.isNotEmpty(id)) {
            ProductBom record = productBomService.getById(id);
            model.addAttribute("result", record);
        }
        return "admin/productBom/addOrUpdate";
    }

    @PostMapping("/add-or-update")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result addOrUpdate(ProductBom record) {
        if (StringUtils.isEmpty(record.getIsDeleted())) {
            record.setIsDeleted("0");
        }

        // 新增时自动生成BOM编码
        if (StringUtils.isEmpty(record.getId())) {
            String code = generateBomCode();
            record.setBomCode(code);
            record.setState("0"); // 草稿状态
            record.setVersion("V1.0");
        } else {
            // 编辑时校验定版状态
            ProductBom old = productBomService.getById(record.getId());
            if (old != null && "1".equals(old.getState())) {
                return Result.failure("BOM已定版，不能编辑");
            }
        }

        if (StringUtils.isEmpty(record.getProductMaterielCode())) {
            return Result.failure("产品物料不能为空");
        }

        productBomService.saveOrUpdate(record);
        return Result.success(record.getId());
    }

    /**
     * 自动生成BOM编码 BOM + 6位序号
     */
    private String generateBomCode() {
        String prefix = "BOM";
        QueryWrapper<ProductBom> wrapper = new QueryWrapper<>();
        wrapper.apply("bom_code LIKE {0}", prefix + "%");
        wrapper.orderByDesc("bom_code");
        wrapper.last("LIMIT 1");
        ProductBom max = productBomService.getOne(wrapper);
        int seq = 1;
        if (max != null && StringUtils.isNotEmpty(max.getBomCode())) {
            String numStr = max.getBomCode().substring(prefix.length());
            try {
                seq = Integer.parseInt(numStr) + 1;
            } catch (NumberFormatException e) {
                seq = 1;
            }
        }
        return prefix + String.format("%06d", seq);
    }

    @PostMapping("/delete")
    @ResponseBody
    public Result delete(@RequestParam String id) {
        ProductBom old = productBomService.getById(id);
        if (old != null && "1".equals(old.getState())) {
            return Result.failure("BOM已定版，不能删除");
        }
        ProductBom record = new ProductBom();
        record.setId(id);
        record.setIsDeleted("1");
        productBomService.updateById(record);
        return Result.success();
    }

    @PostMapping("/deleteBatch")
    @ResponseBody
    public Result deleteBatch(@RequestParam String ids) {
        if (StringUtils.isEmpty(ids)) {
            return Result.failure("请选择数据");
        }
        String[] idArr = ids.split(",");
        for (String id : idArr) {
            ProductBom old = productBomService.getById(id);
            if (old != null && "1".equals(old.getState())) {
                return Result.failure("存在已定版的BOM，不能删除");
            }
            ProductBom record = new ProductBom();
            record.setId(id);
            record.setIsDeleted("1");
            productBomService.updateById(record);
        }
        return Result.success();
    }

    @ApiOperation("BOM编制页面")
    @GetMapping("/edit-ui")
    public String editUI(Model model, @RequestParam String id) {
        ProductBom bom = productBomService.getById(id);
        model.addAttribute("bom", bom);
        return "admin/productBom/edit";
    }

    @ApiOperation("锁定BOM")
    @PostMapping("/lock")
    @ResponseBody
    public Result lock(@RequestParam String id) {
        ProductBom record = productBomService.getById(id);
        if (record == null) {
            return Result.failure("BOM不存在");
        }
        if ("1".equals(record.getState())) {
            return Result.failure("BOM已处于定版状态");
        }
        ProductBom update = new ProductBom();
        update.setId(id);
        update.setState("1");
        productBomService.updateById(update);
        return Result.success();
    }

    @ApiOperation("获取BOM子项树")
    @GetMapping("/item/tree")
    @ResponseBody
    public Result itemTree(@RequestParam String bomId) {
        QueryWrapper<ProductBomItem> wrapper = new QueryWrapper<>();
        wrapper.eq("bom_id", bomId);
        wrapper.orderByAsc("sort_num");
        List<ProductBomItem> list = productBomItemService.list(wrapper);
        List<Map<String, Object>> tree = buildTree(list, null);
        return Result.success(tree);
    }

    private List<Map<String, Object>> buildTree(List<ProductBomItem> list, String parentId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProductBomItem item : list) {
            String itemParentId = item.getParentId();
            boolean match = (parentId == null && itemParentId == null)
                    || (parentId != null && parentId.equals(itemParentId));
            if (match) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", item.getId());
                node.put("bomId", item.getBomId());
                node.put("parentId", item.getParentId());
                node.put("materielCode", item.getMaterielCode());
                node.put("materielDesc", item.getMaterielDesc());
                node.put("materielType", item.getMaterielType());
                node.put("itemNum", item.getItemNum());
                node.put("itemUnit", item.getItemUnit());
                node.put("remark", item.getRemark());
                node.put("sortNum", item.getSortNum());
                node.put("children", buildTree(list, item.getId()));
                result.add(node);
            }
        }
        return result;
    }

    @ApiOperation("保存BOM子项")
    @PostMapping("/item/save")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result itemSave(ProductBomItem item) {
        // 校验BOM状态
        if (StringUtils.isNotEmpty(item.getBomId())) {
            ProductBom bom = productBomService.getById(item.getBomId());
            if (bom != null && "1".equals(bom.getState())) {
                return Result.failure("BOM已定版，不能编辑");
            }
        }

        if (StringUtils.isEmpty(item.getMaterielCode())) {
            return Result.failure("物料编码不能为空");
        }

        if (StringUtils.isEmpty(item.getId())) {
            item.setId(UUID.randomUUID().toString().replace("-", ""));
        }
        if (item.getItemNum() == null) {
            item.setItemNum(new java.math.BigDecimal("1"));
        }
        if (item.getSortNum() == null) {
            item.setSortNum(0);
        }

        productBomItemService.saveOrUpdate(item);
        return Result.success(item.getId());
    }

    @ApiOperation("删除BOM子项")
    @PostMapping("/item/delete")
    @ResponseBody
    public Result itemDelete(@RequestParam String id) {
        ProductBomItem item = productBomItemService.getById(id);
        if (item != null && StringUtils.isNotEmpty(item.getBomId())) {
            ProductBom bom = productBomService.getById(item.getBomId());
            if (bom != null && "1".equals(bom.getState())) {
                return Result.failure("BOM已定版，不能删除");
            }
        }
        // 级联删除子节点
        deleteItemChildren(id);
        productBomItemService.removeById(id);
        return Result.success();
    }

    private void deleteItemChildren(String parentId) {
        QueryWrapper<ProductBomItem> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", parentId);
        List<ProductBomItem> children = productBomItemService.list(wrapper);
        for (ProductBomItem child : children) {
            deleteItemChildren(child.getId());
            productBomItemService.removeById(child.getId());
        }
    }

    @ApiOperation("查询物料列表（用于BOM编制选择物料）")
    @GetMapping("/materiel/list")
    @ResponseBody
    public Result materielList(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String matType) {
        QueryWrapper<SpMaterile> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", "0");
        if (StringUtils.isNotEmpty(keyword)) {
            wrapper.and(w -> w.like("materiel", keyword).or().like("materiel_desc", keyword));
        }
        if (StringUtils.isNotEmpty(matType)) {
            wrapper.eq("mat_type", matType);
        }
        wrapper.orderByAsc("materiel");
        List<SpMaterile> list = spMaterileService.list(wrapper);
        return Result.success(list);
    }
}
