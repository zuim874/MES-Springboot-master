<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>工序信息定义</title>
    <#include "${request.contextPath}/common/common.ftl">
</head>
<body>
<div class="layui-form" style="padding: 20px;">
    <input type="hidden" name="id" value="${(result.id)!}">

    <div class="layui-form-item">
        <label class="layui-form-label sp-required">工序编号</label>
        <div class="layui-input-block">
            <#if result?? && result.id??>
                <input type="text" class="layui-input" value="${result.code}" disabled>
                <input type="hidden" name="code" value="${result.code}">
            <#else>
                <input type="text" class="layui-input" value="系统自动生成" disabled>
            </#if>
        </div>
    </div>

    <div class="layui-form-item">
        <label class="layui-form-label sp-required">工序名称</label>
        <div class="layui-input-block">
            <input type="text" name="name" lay-verify="required" autocomplete="off" class="layui-input" value="${(result.name)!}">
        </div>
    </div>

    <div class="layui-form-item">
        <label class="layui-form-label sp-required">加工单元</label>
        <div class="layui-input-block">
            <select name="processUnitId" lay-verify="required" lay-search="">
                <option value="">请选择加工单元</option>
            </select>
        </div>
    </div>

    <div class="layui-form-item">
        <label class="layui-form-label sp-required">工序工时(h)</label>
        <div class="layui-input-block">
            <input type="number" name="laborHours" lay-verify="required|number" autocomplete="off" class="layui-input" value="${(result.laborHours)!'1'}" min="1">
        </div>
    </div>

    <div class="layui-form-item">
        <label class="layui-form-label sp-required">制造周期(h)</label>
        <div class="layui-input-block">
            <input type="number" name="manufacturingCycle" lay-verify="required|number" autocomplete="off" class="layui-input" value="${(result.manufacturingCycle)!'2'}" min="1">
            <div class="layui-form-mid layui-word-aux">制造周期必须大于工序工时</div>
        </div>
    </div>

    <div class="layui-form-item">
        <label class="layui-form-label sp-required">生成生产计划</label>
        <div class="layui-input-block">
            <select name="generateProductionPlan" lay-verify="required">
                <option value="">请选择</option>
                <option value="是" <#if (result.generateProductionPlan)?? && result.generateProductionPlan == '是'>selected</#if>>是</option>
                <option value="否" <#if (result.generateProductionPlan)?? && result.generateProductionPlan == '否'>selected</#if>>否</option>
            </select>
        </div>
    </div>

    <div class="layui-form-item">
        <label class="layui-form-label">备注</label>
        <div class="layui-input-block">
            <textarea name="remark" class="layui-textarea" rows="3">${(result.remark)!}</textarea>
        </div>
    </div>

    <div class="layui-form-item" style="text-align: center;">
        <button id="js-submit" lay-submit lay-filter="process-form-filter" style="display:none;"></button>
    </div>
</div>

<script>
    layui.use(['form', 'layer'], function () {
        var form = layui.form,
            layer = layui.layer;

        // 加载加工单元列表
        spUtil.ajax({
            url: '${request.contextPath}/admin/process/unit-list',
            type: 'GET',
            success: function (res) {
                if (res.code === 0 && res.data) {
                    var unitList = res.data;
                    var select = $('select[name="processUnitId"]');
                    for (var i = 0; i < unitList.length; i++) {
                        var selected = '';
                        <#if (result.processUnitId)??>
                        if (unitList[i].id === '${result.processUnitId}') {
                            selected = 'selected';
                        }
                        </#if>
                        select.append('<option value="' + unitList[i].id + '" ' + selected + '>' + unitList[i].name + '</option>');
                    }
                    form.render('select');
                }
            }
        });

        form.on('submit(process-form-filter)', function (data) {
            spUtil.ajax({
                url: '${request.contextPath}/admin/process/add-or-update',
                type: 'POST',
                data: data.field,
                success: function (res) {
                    if (res.code === 0) {
                        layer.msg('保存成功', {icon: 1, time: 1000}, function () {
                            var index = parent.layer.getFrameIndex(window.name);
                            parent.layer.close(index);
                        });
                    } else {
                        layer.msg(res.msg, {icon: 2});
                    }
                }
            });
            return false;
        });
    });
</script>
</body>
</html>