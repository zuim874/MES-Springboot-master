package com.wangziyang.mes.system.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangziyang.mes.common.BaseController;
import com.wangziyang.mes.common.Result;
import com.wangziyang.mes.system.entity.ProductBom;
import com.wangziyang.mes.system.entity.ProductBomNode;
import com.wangziyang.mes.system.service.IProductBomNodeService;
import com.wangziyang.mes.system.service.IProductBomService;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 产品BOM管理控制器
 */
@Controller
@RequestMapping("/admin/productBom")
public class ProductBomController extends BaseController {

    @Autowired
    private IProductBomService bomService;

    @Autowired
    private IProductBomNodeService nodeService;

    // ==================== BOM头管理 ====================

    @ApiOperation("产品BOM列表UI")
    @GetMapping("/list-ui")
    public String listUI() {
        return "admin/productBom/list";
    }

    @ApiOperation("产品BOM分页查询")
    @PostMapping("/page")
    @ResponseBody
    public Result page(@RequestParam(required = false) String name,
                       @RequestParam(defaultValue = "1") long current,
                       @RequestParam(defaultValue = "10") long size) {
        QueryWrapper<ProductBom> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(name)) {
            wrapper.like("name", name);
        }
        wrapper.eq("is_deleted", "0");
        wrapper.orderByDesc("create_time");
        IPage<ProductBom> page = bomService.page(new Page<>(current, size), wrapper);
        return Result.success(page);
    }

    @ApiOperation("产品BOM新增/编辑UI")
    @GetMapping("/add-or-update-ui")
    public String addOrUpdateUI(Model model, @RequestParam(required = false) String id) {
        if (StringUtils.isNotEmpty(id)) {
            ProductBom record = bomService.getById(id);
            model.addAttribute("result", record);
        }
        return "admin/productBom/addOrUpdate";
    }

    @ApiOperation("产品BOM保存")
    @PostMapping("/add-or-update")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result addOrUpdate(ProductBom record) {
        if (StringUtils.isEmpty(record.getIsDeleted())) {
            record.setIsDeleted("0");
        }
        if (StringUtils.isEmpty(record.getIsValid())) {
            record.setIsValid("1");
        }
        if (StringUtils.isEmpty(record.getIsFrozen())) {
            record.setIsFrozen("0");
        }
        if (StringUtils.isEmpty(record.getVersion())) {
            record.setVersion("V1.0");
        }

        boolean isNew = StringUtils.isEmpty(record.getId());
        if (isNew) {
            String code = generateBomCode();
            record.setCode(code);
        }

        if (StringUtils.isEmpty(record.getName())) {
            return Result.failure("产品物料名称不能为空");
        }

        // 校验编码+版本唯一性
        QueryWrapper<ProductBom> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("code", record.getCode());
        checkWrapper.eq("version", record.getVersion());
        checkWrapper.eq("is_deleted", "0");
        if (StringUtils.isNotEmpty(record.getId())) {
            checkWrapper.ne("id", record.getId());
        }
        long count = bomService.count(checkWrapper);
        if (count > 0) {
            return Result.failure("该产品物料编码与版本组合已存在");
        }

        bomService.saveOrUpdate(record);

        if (isNew) {
            // 自动创建层级0根节点（产品）
            ProductBomNode rootNode = new ProductBomNode();
            rootNode.setBomId(record.getId());
            rootNode.setParentId(null);
            rootNode.setNodeCode("0");
            rootNode.setNodeName(record.getName());
            rootNode.setNodeLevel(0);
            rootNode.setNodeType("0");
            rootNode.setQty(BigDecimal.ONE);
            rootNode.setSortNum(0);
            rootNode.setIsDeleted("0");
            nodeService.save(rootNode);
        } else {
            // 同步更新根节点名称
            QueryWrapper<ProductBomNode> nodeWrapper = new QueryWrapper<>();
            nodeWrapper.eq("bom_id", record.getId());
            nodeWrapper.eq("is_deleted", "0");
            nodeWrapper.and(w -> w.isNull("parent_id").or().eq("parent_id", ""));
            ProductBomNode root = nodeService.getOne(nodeWrapper);
            if (root != null && !record.getName().equals(root.getNodeName())) {
                root.setNodeName(record.getName());
                nodeService.updateById(root);
            }
        }

        return Result.success(record.getId());
    }

    /**
     * 自动生成产品物料编码 CP + 6位序号
     */
    private String generateBomCode() {
        String prefix = "CP";
        QueryWrapper<ProductBom> wrapper = new QueryWrapper<>();
        wrapper.apply("code LIKE {0}", prefix + "%");
        wrapper.orderByDesc("code");
        wrapper.last("LIMIT 1");
        ProductBom max = bomService.getOne(wrapper);
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

    @ApiOperation("删除产品BOM")
    @PostMapping("/delete")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result delete(@RequestParam String id) {
        ProductBom record = bomService.getById(id);
        if (record == null) {
            return Result.failure("记录不存在");
        }
        if ("1".equals(record.getIsFrozen())) {
            return Result.failure("已定版的BOM不能删除");
        }
        record.setIsDeleted("1");
        bomService.updateById(record);

        // 级联删除节点
        QueryWrapper<ProductBomNode> wrapper = new QueryWrapper<>();
        wrapper.eq("bom_id", id);
        List<ProductBomNode> nodes = nodeService.list(wrapper);
        for (ProductBomNode node : nodes) {
            node.setIsDeleted("1");
        }
        if (!nodes.isEmpty()) {
            nodeService.updateBatchById(nodes);
        }

        return Result.success();
    }

    @ApiOperation("BOM定版")
    @PostMapping("/freeze")
    @ResponseBody
    public Result freeze(@RequestParam String id) {
        ProductBom record = bomService.getById(id);
        if (record == null) {
            return Result.failure("记录不存在");
        }
        record.setIsFrozen("1");
        bomService.updateById(record);
        return Result.success();
    }

    @ApiOperation("复制BOM创建新版本")
    @PostMapping("/copy")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result copy(@RequestParam String id) {
        ProductBom source = bomService.getById(id);
        if (source == null) {
            return Result.failure("源BOM不存在");
        }

        String newVersion = generateNextVersion(source.getVersion());

        ProductBom target = new ProductBom();
        target.setCode(source.getCode());
        target.setName(source.getName());
        target.setVersion(newVersion);
        target.setIsValid("1");
        target.setIsFrozen("0");
        target.setRemark(source.getRemark());
        target.setIsDeleted("0");
        bomService.save(target);

        // 复制节点
        QueryWrapper<ProductBomNode> wrapper = new QueryWrapper<>();
        wrapper.eq("bom_id", id);
        wrapper.eq("is_deleted", "0");
        wrapper.orderByAsc("node_level", "sort_num");
        List<ProductBomNode> sourceNodes = nodeService.list(wrapper);

        Map<String, String> idMapping = new HashMap<>();
        for (ProductBomNode node : sourceNodes) {
            String oldId = node.getId();
            ProductBomNode newNode = new ProductBomNode();
            newNode.setBomId(target.getId());
            newNode.setParentId(idMapping.get(node.getParentId()));
            newNode.setNodeCode(node.getNodeCode());
            newNode.setNodeName(node.getNodeName());
            newNode.setNodeLevel(node.getNodeLevel());
            newNode.setNodeType(node.getNodeType());
            newNode.setRefCode(node.getRefCode());
            newNode.setRefName(node.getRefName());
            newNode.setQty(node.getQty());
            newNode.setRemark(node.getRemark());
            newNode.setSortNum(node.getSortNum());
            newNode.setIsDeleted("0");
            nodeService.save(newNode);
            idMapping.put(oldId, newNode.getId());
        }

        return Result.success(target.getId());
    }

    private String generateNextVersion(String version) {
        if (StringUtils.isEmpty(version)) {
            return "V2.0";
        }
        try {
            String numPart = version.replaceAll("[^0-9.]", "");
            String[] parts = numPart.split("\\.");
            int major = Integer.parseInt(parts[0]);
            return "V" + (major + 1) + ".0";
        } catch (Exception e) {
            return "V2.0";
        }
    }

    // ==================== 节点管理 ====================

    @ApiOperation("BOM节点管理UI")
    @GetMapping("/node-ui")
    public String nodeUI(Model model, @RequestParam String bomId) {
        model.addAttribute("bomId", bomId);
        ProductBom bom = bomService.getById(bomId);
        if (bom != null) {
            model.addAttribute("bomName", bom.getName());
            model.addAttribute("isFrozen", bom.getIsFrozen() != null ? bom.getIsFrozen() : "0");
        } else {
            model.addAttribute("bomName", "未知");
            model.addAttribute("isFrozen", "0");
        }
        // 查询根节点，如果不存在则自动创建
        QueryWrapper<ProductBomNode> rootWrapper = new QueryWrapper<>();
        rootWrapper.eq("bom_id", bomId);
        rootWrapper.eq("is_deleted", "0");
        rootWrapper.and(w -> w.isNull("parent_id").or().eq("parent_id", ""));
        rootWrapper.last("LIMIT 1");
        ProductBomNode rootNode = nodeService.getOne(rootWrapper);
        if (rootNode == null && bom != null) {
            rootNode = new ProductBomNode();
            rootNode.setBomId(bomId);
            rootNode.setParentId(null);
            rootNode.setNodeCode("0");
            rootNode.setNodeName(bom.getName());
            rootNode.setNodeLevel(0);
            rootNode.setNodeType("0");
            rootNode.setQty(BigDecimal.ONE);
            rootNode.setSortNum(0);
            rootNode.setIsDeleted("0");
            nodeService.save(rootNode);
        }
        return "admin/productBom/node";
    }

    @ApiOperation("BOM节点列表")
    @PostMapping("/node/list")
    @ResponseBody
    public Result nodeList(@RequestParam String bomId) {
        QueryWrapper<ProductBomNode> wrapper = new QueryWrapper<>();
        wrapper.eq("bom_id", bomId);
        wrapper.eq("is_deleted", "0");
        wrapper.orderByAsc("node_level", "sort_num", "node_code");
        List<ProductBomNode> list = nodeService.list(wrapper);
        return Result.success(list);
    }

    @ApiOperation("保存BOM节点")
    @PostMapping("/node/save")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result nodeSave(ProductBomNode node) {
        if (StringUtils.isEmpty(node.getBomId())) {
            return Result.failure("BOM ID不能为空");
        }
        if (StringUtils.isEmpty(node.getNodeName())) {
            return Result.failure("节点名称不能为空");
        }
        if (StringUtils.isEmpty(node.getNodeType())) {
            return Result.failure("节点类型不能为空");
        }
        if (StringUtils.isEmpty(node.getIsDeleted())) {
            node.setIsDeleted("0");
        }
        if (node.getQty() == null) {
            node.setQty(BigDecimal.ONE);
        }

        boolean isNew = StringUtils.isEmpty(node.getId());

        // 新增时，如果parentId为空，自动查找或创建根节点并挂载到根节点下
        if (isNew && StringUtils.isEmpty(node.getParentId())) {
            QueryWrapper<ProductBomNode> rootWrapper = new QueryWrapper<>();
            rootWrapper.eq("bom_id", node.getBomId());
            rootWrapper.eq("is_deleted", "0");
            rootWrapper.and(w -> w.isNull("parent_id").or().eq("parent_id", ""));
            rootWrapper.last("LIMIT 1");
            ProductBomNode rootNode = nodeService.getOne(rootWrapper);
            if (rootNode == null) {
                ProductBom bom = bomService.getById(node.getBomId());
                if (bom == null) {
                    return Result.failure("BOM不存在");
                }
                rootNode = new ProductBomNode();
                rootNode.setBomId(node.getBomId());
                rootNode.setParentId(null);
                rootNode.setNodeCode("0");
                rootNode.setNodeName(bom.getName());
                rootNode.setNodeLevel(0);
                rootNode.setNodeType("0");
                rootNode.setQty(BigDecimal.ONE);
                rootNode.setSortNum(0);
                rootNode.setIsDeleted("0");
                nodeService.save(rootNode);
            }
            node.setParentId(rootNode.getId());
            node.setNodeLevel(1);
        }

        // 检查BOM是否已定版
        ProductBom bom = bomService.getById(node.getBomId());
        if (bom != null && "1".equals(bom.getIsFrozen())) {
            return Result.failure("已定版的BOM不能编辑节点");
        }

        if (node.getNodeLevel() == null) {
            node.setNodeLevel(1);
        }

        // 自动生成节点编号
        if (isNew) {
            String nextCode = generateNodeCode(node.getNodeType());
            node.setNodeCode(nextCode);
        }

        nodeService.saveOrUpdate(node);
        return Result.success(node.getId());
    }

    /**
     * 根据节点类型生成编号：零部件 BOM+6位序号，物料 M+6位序号
     */
    private String generateNodeCode(String nodeType) {
        String prefix;
        if ("1".equals(nodeType)) {
            prefix = "BOM";
        } else if ("2".equals(nodeType)) {
            prefix = "M";
        } else {
            prefix = "NODE";
        }
        QueryWrapper<ProductBomNode> wrapper = new QueryWrapper<>();
        wrapper.apply("node_code LIKE {0}", prefix + "%");
        wrapper.orderByDesc("node_code");
        wrapper.last("LIMIT 1");
        ProductBomNode max = nodeService.getOne(wrapper);
        int seq = 1;
        if (max != null && StringUtils.isNotEmpty(max.getNodeCode())) {
            String numStr = max.getNodeCode().substring(prefix.length());
            try {
                seq = Integer.parseInt(numStr) + 1;
            } catch (NumberFormatException e) {
                seq = 1;
            }
        }
        return prefix + String.format("%06d", seq);
    }

    @ApiOperation("获取单个BOM节点")
    @PostMapping("/node/get")
    @ResponseBody
    public Result nodeGet(@RequestParam String id) {
        ProductBomNode node = nodeService.getById(id);
        if (node == null) {
            return Result.failure("节点不存在");
        }
        return Result.success(node);
    }

    @ApiOperation("删除BOM节点")
    @PostMapping("/node/delete")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Result nodeDelete(@RequestParam String id) {
        ProductBomNode node = nodeService.getById(id);
        if (node == null) {
            return Result.failure("节点不存在");
        }
        // 检查BOM是否已定版
        ProductBom bom = bomService.getById(node.getBomId());
        if (bom != null && "1".equals(bom.getIsFrozen())) {
            return Result.failure("已定版的BOM不能删除节点");
        }
        // 不能删除根节点
        if (node.getNodeLevel() != null && node.getNodeLevel() == 0) {
            return Result.failure("不能删除根节点");
        }
        // 级联删除子节点
        deleteChildren(node.getId());
        node.setIsDeleted("1");
        nodeService.updateById(node);
        return Result.success();
    }

    private void deleteChildren(String parentId) {
        QueryWrapper<ProductBomNode> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", parentId);
        wrapper.eq("is_deleted", "0");
        List<ProductBomNode> children = nodeService.list(wrapper);
        for (ProductBomNode child : children) {
            deleteChildren(child.getId());
            child.setIsDeleted("1");
            nodeService.updateById(child);
        }
    }

    @ApiOperation("获取下一个节点编号")
    @PostMapping("/node/next-code")
    @ResponseBody
    public Result nextNodeCode(@RequestParam String nodeType) {
        String code = generateNodeCode(nodeType);
        return Result.success(code);
    }
}
