<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>产品BOM</title>
    <#include "${request.contextPath}/common/common.ftl">
</head>
<body>
<div class="layui-form" style="padding: 20px;">
    <input type="hidden" name="id" value="${(result.id)!}">

    <div class="layui-form-item">
        <label class="layui-form-label sp-required">产品物料编码</label>
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
        <label class="layui-form-label sp-required">产品物料名称</label>
        <div class="layui-input-block">
            <input type="text" name="name" lay-verify="required" autocomplete="off" class="layui-input" value="${(result.name)!}">
        </div>
    </div>

    <div class="layui-form-item">
        <label class="layui-form-label">版本</label>
        <div class="layui-input-block">
            <input type="text" name="version" autocomplete="off" class="layui-input" value="${(result.version)!'V1.0'}">
        </div>
    </div>

    <div class="layui-form-item">
        <label class="layui-form-label">有效性</label>
        <div class="layui-input-block">
            <select name="isValid">
                <option value="1" <#if (result.isValid)?? && result.isValid == '1'>selected</#if>>有效</option>
                <option value="0" <#if (result.isValid)?? && result.isValid == '0'>selected</#if>>无效</option>
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
        <button id="js-submit" lay-submit lay-filter="bom-form-filter" style="display:none;"></button>
    </div>
</div>

<script>
    layui.use(['form', 'layer'], function () {
        var form = layui.form;

        form.on('submit(bom-form-filter)', function (data) {
            spUtil.ajax({
                url: '${request.contextPath}/admin/productBom/add-or-update',
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
