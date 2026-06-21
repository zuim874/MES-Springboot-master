<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>物料维护</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0">
    <#include "${request.contextPath}/common/common.ftl">
    <link href="${request.contextPath}/css/effect.css" rel="stylesheet" type="text/css"/>
    <style>
        .flowProcss {
            font-size: 25px;
            margin-left: 310PX;
            display: flex;
            justify-content: flex-start;
            flex-direction: row;
        }
        .img-preview {
            width: 120px;
            height: 120px;
            border: 1px solid #e6e6e6;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-top: 10px;
        }
        .img-preview img {
            max-width: 100%;
            max-height: 100%;
        }
    </style>

</head>
<body>
<div class="splayui-container">
    <div class="splayui-main">
        <form class="layui-form splayui-form" lay-filter="formTest">
            <div class="layui-row">
                <div class="layui-col-xs6 layui-col-sm6 layui-col-md10">
                    <div class="layui-form-item">
                        <label class="layui-form-label">物料编号</label>
                        <div class="layui-input-inline">
                            <#if result.id??>
                                <input type="text" class="layui-input" value="${result.materiel}" readonly>
                                <input type="hidden" name="materiel" value="${result.materiel}">
                            <#else>
                                <input type="text" class="layui-input" value="系统自动生成" readonly>
                            </#if>
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-materielDesc" class="layui-form-label sp-required">物料描述
                        </label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-materielDesc" name="materielDesc" lay-verify="required"
                                   autocomplete="off" class="layui-input" value="${(result.materielDesc)!}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-matType" class="layui-form-label sp-required">物料类型
                        </label>
                        <div class="layui-input-inline">
                            <select id="js-matType" name="matType" lay-filter="matType-filter" lay-verify="required">
                            </select>
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-source" class="layui-form-label sp-required">物料来源
                        </label>
                        <div class="layui-input-inline">
                            <select id="js-source" name="source" lay-verify="required">
                            </select>
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-unit" class="layui-form-label sp-required">单位
                        </label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-unit" name="unit" lay-verify="required" autocomplete="off"
                                   class="layui-input" value="${(result.unit)!}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-productGroup" class="layui-form-label sp-required">产品组
                        </label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-productGroup" name="productGroup" lay-verify="required"
                                   autocomplete="off"
                                   class="layui-input" value="${(result.productGroup)!}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-model" class="layui-form-label sp-required">规格型号
                        </label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-model" name="model" lay-verify="required"
                                   autocomplete="off"
                                   class="layui-input" value="${(result.model)!}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-size" class="layui-form-label ">尺寸
                        </label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-size" name="size" autocomplete="off"
                                   class="layui-input" value="${(result.size)!}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-leadTime" class="layui-form-label sp-required">需求提前期(天)
                        </label>
                        <div class="layui-input-inline">
                            <input type="number" id="js-leadTime" name="leadTime" lay-verify="required|number"
                                   autocomplete="off" class="layui-input" value="${(result.leadTime)!1}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-safetyStock" class="layui-form-label">安全库存
                        </label>
                        <div class="layui-input-inline">
                            <input type="number" id="js-safetyStock" name="safetyStock" lay-verify="number"
                                   autocomplete="off" class="layui-input" value="${(result.safetyStock)!0}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label class="layui-form-label">物料图片</label>
                        <div class="layui-input-inline">
                            <button type="button" class="layui-btn" id="js-upload-btn">
                                <i class="layui-icon">&#xe67c;</i>上传图片
                            </button>
                            <input type="hidden" id="js-img-url" name="imgUrl" value="${(result.imgUrl)!}">
                            <div class="img-preview" id="js-img-preview">
                                <#if result.imgUrl??>
                                    <img src="${result.imgUrl}" id="js-preview-img">
                                <#else>
                                    <span>暂无图片</span>
                                </#if>
                            </div>
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-remark" class="layui-form-label">备注
                        </label>
                        <div class="layui-input-inline">
                            <textarea id="js-remark" name="remark" class="layui-textarea" style="width: 310px;">${(result.remark)!}</textarea>
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <div class="layui-inline">
                            <label class="layui-form-label">长(cm)</label>
                            <div class="layui-input-inline" style="width:80px;">
                                <input type="number" id="js-length" name="length" autocomplete="off" class="layui-input" value="${(result.length)!0}">
                            </div>
                        </div>
                        <div class="layui-inline">
                            <label class="layui-form-label">宽(cm)</label>
                            <div class="layui-input-inline" style="width:80px;">
                                <input type="number" id="js-width" name="width" autocomplete="off" class="layui-input" value="${(result.width)!0}">
                            </div>
                        </div>
                        <div class="layui-inline">
                            <label class="layui-form-label">高(cm)</label>
                            <div class="layui-input-inline" style="width:80px;">
                                <input type="number" id="js-height" name="height" autocomplete="off" class="layui-input" value="${(result.height)!0}">
                            </div>
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label class="layui-form-label">存放库房</label>
                        <div class="layui-input-inline">
                            <select id="js-warehouse" name="warehouseId" lay-filter="warehouse-filter">
                                <option value="">请选择库房</option>
                            </select>
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label class="layui-form-label">存放库位</label>
                        <div class="layui-input-inline">
                            <select id="js-location" name="locationId">
                                <option value="">请选择库位</option>
                            </select>
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-flowId" class="layui-form-label ">工艺流程
                        </label>
                        <div class="layui-input-inline">
                            <select id="js-flowId" name="flowId" lay-filter="flow-filter">
                            </select>
                        </div>
                        <div class=" text-effect flowProcss  " id="js-flowProcess" name="flowProcss">
                        </div>
                    </div>

                    <div class="layui-form-item">
                        <label for="js-is-deleted" class="layui-form-label sp-required">状态
                        </label>
                        <div class="layui-input-block" id="js-is-deleted" style="width: 310px;">
                            <input type="radio" name="deleted" value="0" title="正常"
                                   <#if (result.deleted)?? && result.deleted == "0">checked<#elseif !(result??)>checked</#if>>
                            <input type="radio" name="deleted" value="1" title="已删除"
                                   <#if (result.deleted)?? && result.deleted == "1">checked</#if>>
                            <input type="radio" name="deleted" value="2" title="已禁用"
                                   <#if (result.deleted)?? && result.deleted == "2">checked</#if>>
                        </div>
                    </div>
                </div>
                <div class="layui-form-item layui-hide">
                    <div class="layui-input-block">
                        <input id="js-id" name="id" value="${(result.id)!}"/>
                        <button id="js-submit" class="layui-btn" lay-submit lay-filter="js-submit-filter">确定
                        </button>
                    </div>
                </div>
            </div>
        </form>
    </div>
</div>
<script>
    layui.use(['form', 'util', 'upload'], function () {
        var form = layui.form,
            util = layui.util,
            upload = layui.upload;
        var flowRows = [];
        //流程添加下拉框
        getFlowData();
        //物料类型
        getMatTypeData();
        //物料来源
        getSourceData();
        //库房库位
        loadWarehouseData();
        form.on('select(warehouse-filter)', function (data) {
            loadLocationData(data.value);
        });

        // 图片上传
        upload.render({
            elem: '#js-upload-btn',
            url: '${request.contextPath}/upload/material-img',
            done: function (res) {
                if (res.code === 0) {
                    $('#js-img-url').val(res.data);
                    $('#js-img-preview').html('<img src="' + res.data + '" id="js-preview-img" style="max-width:100%;max-height:100%;">');
                    layer.msg('上传成功', {icon: 1});
                } else {
                    layer.msg('上传失败：' + (res.msg || '未知错误'));
                }
            },
            error: function () {
                layer.msg('上传失败，请检查网络');
            }
        });

        /**
         * 初始化物料类型数据
         */
        function getMatTypeData() {
            spUtil.ajax({
                url: '${request.contextPath}/basedata/dict/list/material_type',
                async: false,
                type: 'GET',
                serializable: false,
                data: {},
                success: function (data) {
                    $.each(data.data, function (index, item) {
                        $('#js-matType').append(new Option(item.name, item.value));
                    });
                }
            });
        }

        /**
         * 初始化物料来源数据
         */
        function getSourceData() {
            spUtil.ajax({
                url: '${request.contextPath}/basedata/dict/list/material_source',
                async: false,
                type: 'GET',
                serializable: false,
                data: {},
                success: function (data) {
                    $.each(data.data, function (index, item) {
                        $('#js-source').append(new Option(item.name, item.value));
                    });
                }
            });
        }

        /**
         * 初始化流程数据
         */
        function getFlowData() {
            spUtil.ajax({
                url: '${request.contextPath}/admin/processFlow/list',
                async: false,
                type: 'GET',
                showLoading: true,
                serializable: false,
                data: {},
                success: function (data) {
                    flowRows = data.data;
                },
                error: function () {
                    flowRows = [];
                }
            });

            $.each(flowRows, function (index, item) {
                $('#js-flowId').append(new Option(item.name, item.id));
            });
            //编辑时候根据回显的ID 绘制流程
            flowProssbyId("${(result.flowId)!}")
        }

        //下拉框选择 绘制流程时序图
        form.on('select(flow-filter)', function (data) {
            flowProssbyId(data.value)
        });

        //通过ID 获取流程时序 绘制
        function flowProssbyId(flowId) {
            var newArr = flowRows.filter(function (obj) {
                return obj.id == flowId;
            });
            if (newArr.length > 0) {
                procssArr = newArr[0].processChain.split("->")
                $("#js-flowProcess").empty();
                $.each(procssArr, function (i, val) {

                    if (i == procssArr.length - 1) {
                        $("#js-flowProcess").append("<span style='display: inline-flex;' >" + val + "</span>");
                    } else {
                        $("#js-flowProcess").append("<span style='display: inline-flex;' >" + val + '->' + "</span>");
                    }
                });
            }
        }

        //给表单赋值
        form.val("formTest", {
            "flowId": "${(result.flowId)!}",
            "matType": "${(result.matType)!}",
            "source": "${(result.source)!}",
            "warehouseId": "${(result.warehouseId)!}",
            "locationId": "${(result.locationId)!}"
        });

        // 编辑时若已有库房，联动加载库位
        var editWarehouseId = "${(result.warehouseId)!}";
        if (editWarehouseId) {
            loadLocationData(editWarehouseId, "${(result.locationId)!}");
        }

        /**
         * 加载库房列表
         */
        function loadWarehouseData() {
            spUtil.ajax({
                url: '${request.contextPath}/admin/warehouse/page',
                async: false,
                type: 'POST',
                showLoading: false,
                data: {current: 1, size: 999},
                success: function (res) {
                    if (res.code === 0 && res.data && res.data.records) {
                        var whSelect = $('#js-warehouse');
                        whSelect.empty();
                        whSelect.append('<option value="">请选择库房</option>');
                        $.each(res.data.records, function (index, item) {
                            whSelect.append(new Option(item.name + ' (' + item.code + ')', item.id));
                        });
                        form.render('select');
                    }
                },
                error: function () {
                    // 静默处理，避免弹窗影响用户体验
                }
            });
        }

        /**
         * 根据库房加载库位列表
         */
        function loadLocationData(warehouseId, selectedLocationId) {
            var locSelect = $('#js-location');
            locSelect.empty();
            locSelect.append('<option value="">请选择库位</option>');
            if (!warehouseId) {
                form.render('select');
                return;
            }
            spUtil.ajax({
                url: '${request.contextPath}/admin/warehouse/location-list',
                async: false,
                type: 'GET',
                showLoading: false,
                data: {warehouseId: warehouseId},
                success: function (res) {
                    if (res.code === 0 && res.data) {
                        $.each(res.data, function (index, item) {
                            var opt = new Option(item.code + ' (排' + item.rowNum + '-层' + item.layerNum + '-列' + item.columnNum + ')', item.id);
                            if (selectedLocationId && item.id === selectedLocationId) {
                                opt.selected = true;
                            }
                            locSelect.append(opt);
                        });
                    }
                    form.render('select');
                },
                error: function () {
                    form.render('select');
                }
            });
        }

        //监听提交
        form.on('submit(js-submit-filter)', function (data) {
            var leadTime = parseInt(data.field.leadTime);
            if (isNaN(leadTime) || leadTime < 1) {
                layer.msg('物料需求提前期不可为0，至少为1天');
                return false;
            }
            var safetyStock = parseInt(data.field.safetyStock);
            if (isNaN(safetyStock) || safetyStock < 0) {
                layer.msg('安全库存不能为负数');
                return false;
            }
            spUtil.submitForm({
                url: "${request.contextPath}/basedata/materile/add-or-update",
                data: data.field
            });
            return false;
        });

    });
</script>
</body>
</html>
