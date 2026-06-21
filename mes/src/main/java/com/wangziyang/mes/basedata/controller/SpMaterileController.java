package com.wangziyang.mes.basedata.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangziyang.mes.basedata.entity.SpMaterile;
import com.wangziyang.mes.basedata.request.spMaterileReq;
import com.wangziyang.mes.basedata.service.ISpMaterileService;
import com.wangziyang.mes.common.BaseController;
import com.wangziyang.mes.common.Result;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;



/**
 * <p>
 * 物料控制器
 * </p>
 *
 * @author WangZiYang
 * @since 2020-03-19
 */
@Controller
@RequestMapping("/basedata/materile")
public class SpMaterileController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SpMaterileController.class);

    /**
     * 物料服务
     *
     * @date 2020-07-07
     */
    @Autowired
    private ISpMaterileService iSpMaterileService;
    /**
     * 物料管理界面
     *
     * @param model 模型
     * @return 物料管理界面
     */
    @ApiOperation("物料管理界面UI")
    @ApiImplicitParams({@ApiImplicitParam(name = "model", value = "模型", defaultValue = "模型")})
    @GetMapping("/list-ui")
    public String listUI(Model model) {
        return "basedata/materile/list";
    }


    /**
     * 物料管理修改界面
     *
     * @param model  模型
     * @param record 平台表对象
     * @return 更改界面
     */
    @ApiOperation("物料管理修改界面")
    @GetMapping("/add-or-update-ui")
    public String addOrUpdateUI(Model model, SpMaterile record) {
        if (StringUtils.isNotEmpty(record.getId())) {
            SpMaterile spMaterile = iSpMaterileService.getById(record.getId());
            model.addAttribute("result", spMaterile);
        }
        return "basedata/materile/addOrUpdate";
    }


    /**
     * 物料管理界面分页查询
     *
     * @param req 请求参数
     * @return Result 执行结果
     */
    @ApiOperation("物料管理界面分页查询")
    @ApiImplicitParams({@ApiImplicitParam(name = "req", value = "请求参数", defaultValue = "请求参数")})
    @PostMapping("/page")
    @ResponseBody
    public Result page(spMaterileReq req) {
        try {
            QueryWrapper<SpMaterile> queryWrapper = new QueryWrapper<>();
            if (StringUtils.isNotEmpty(req.getMaterielLike())) {
                queryWrapper.like("materiel", req.getMaterielLike());
            }
            if (StringUtils.isNotEmpty(req.getMaterielDescLike())) {
                queryWrapper.like("materiel_desc", req.getMaterielDescLike());
            }
            if (StringUtils.isNotEmpty(req.getMatType())) {
                queryWrapper.eq("mat_type", req.getMatType());
            }
            if (StringUtils.isNotEmpty(req.getSource())) {
                queryWrapper.eq("source", req.getSource());
            }
            queryWrapper.orderByDesc("update_time");
            Page<SpMaterile> page = new Page<>(req.getCurrent(), req.getSize());
            logger.info("物料分页查询参数: current={}, size={}, materielLike={}, materielDescLike={}, matType={}, source={}",
                    req.getCurrent(), req.getSize(), req.getMaterielLike(), req.getMaterielDescLike(), req.getMatType(), req.getSource());
            IPage result = iSpMaterileService.page(page, queryWrapper);
            logger.info("物料分页查询结果: total={}, pages={}, current={}", result.getTotal(), result.getPages(), result.getCurrent());
            return Result.success(result);
        } catch (Exception e) {
            logger.error("物料分页查询异常", e);
            return Result.failure("查询物料数据异常: " + e.getMessage());
        }
    }

    /**
     * 物料管理修改、新增
     *
     * @param record 物料实体类
     * @return 执行结果
     */
    @ApiOperation("物料管理修改、新增")
    @PostMapping("/add-or-update")
    @ResponseBody
    public Result addOrUpdate(SpMaterile record) {
        // 默认值处理
        if (record.getLeadTime() == null) {
            record.setLeadTime(1);
        }
        if (record.getSafetyStock() == null) {
            record.setSafetyStock(0);
        }

        // 校验：需求提前期至少为1天
        if (record.getLeadTime() < 1) {
            return Result.failure("物料需求提前期不可为0，至少为1天");
        }
        // 校验：安全库存不可为负数
        if (record.getSafetyStock() < 0) {
            return Result.failure("安全库存不能为负数");
        }

        // 新增时自动生成物料编码
        if (StringUtils.isEmpty(record.getId())) {
            if (StringUtils.isEmpty(record.getMatType())) {
                return Result.failure("物料类型不能为空");
            }
            String code = generateMaterielCode(record.getMatType());
            record.setMateriel(code);
        }

        // 产品类型唯一性校验：同一种产品描述只能有一个产品类型的物料
        if ("产品".equals(record.getMatType()) && StringUtils.isNotEmpty(record.getMaterielDesc())) {
            QueryWrapper<SpMaterile> checkWrapper = new QueryWrapper<>();
            checkWrapper.eq("mat_type", "产品");
            checkWrapper.eq("materiel_desc", record.getMaterielDesc());
            if (StringUtils.isNotEmpty(record.getId())) {
                checkWrapper.ne("id", record.getId());
            }
            long count = iSpMaterileService.count(checkWrapper);
            if (count > 0) {
                return Result.failure("已存在相同描述的产品类型物料，一种成品只能具有唯一的产品属性物料编码");
            }
        }

        iSpMaterileService.saveOrUpdate(record);
        return Result.success(record.getId(), StringUtils.isEmpty(record.getId()) ? "新增成功" : "修改成功");
    }

    /**
     * 根据物料类型自动生成编码
     */
    private String generateMaterielCode(String matType) {
        String prefix;
        if ("产品".equals(matType)) {
            prefix = "CP";
        } else if ("零件".equals(matType)) {
            prefix = "LJ";
        } else if ("标准件".equals(matType)) {
            prefix = "BZJ";
        } else {
            prefix = "QT";
        }
        QueryWrapper<SpMaterile> wrapper = new QueryWrapper<>();
        wrapper.apply("materiel LIKE {0}", prefix + "%");
        wrapper.orderByDesc("materiel");
        wrapper.last("LIMIT 1");
        SpMaterile max = iSpMaterileService.getOne(wrapper);
        int seq = 1;
        if (max != null && StringUtils.isNotEmpty(max.getMateriel())) {
            String numStr = max.getMateriel().substring(prefix.length());
            try {
                seq = Integer.parseInt(numStr) + 1;
            } catch (NumberFormatException e) {
                seq = 1;
            }
        }
        return prefix + String.format("%06d", seq);
    }


    /**
     * 删除物料信息
     *
     * @param req 请求参数
     * @return Result 执行结果
     */
    @ApiOperation("删除物料信息（逻辑删除）")
    @ApiImplicitParams({@ApiImplicitParam(name = "req", value = "物料实体", defaultValue = "物料实体")})
    @PostMapping("/delete")
    @ResponseBody
    public Result deleteByTableNameId(SpMaterile req) throws Exception {
        logger.info("逻辑删除物料, id={}", req.getId());
        boolean result = iSpMaterileService.removeById(req.getId());
        if (result) {
            return Result.success("删除成功");
        } else {
            return Result.failure("删除失败，物料不存在或已删除");
        }
    }
}
