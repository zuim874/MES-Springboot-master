<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>零部件定义</title>
    <#include "${request.contextPath}/common/common.ftl">
</head>
<body>
<div class="layui-form" style="padding: 20px;">
    <input type="hidden" name="id" value="${(result.id)!}">

    <div class="layui-form-item">
        <label class="layui-form-label sp-required">零部件编号</label>
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
        <label class="layui-form-label sp-required">零部件名称</label>
        <div class="layui-input-block">
            <input type="text" name="name" lay-verify="required" autocomplete="off" class="layui-input" value="${(result.name)!}">
        </div>
    </div>

    <div class="layui-form-item">
        <label class="layui-form-label">备注</label>
        <div class="layui-input-block">
            <textarea name="remark" class="layui-textarea" rows="4">${(result.remark)!}</textarea>
        </div>
    </div>

    <div class="layui-form-item" style="text-align: center;">
        <button id="js-submit" lay-submit lay-filter="part-form-filter" style="display:none;"></button>
    </div>
</div>

<script>
    layui.use(['form', 'layer'], function () {
        var form = layui.form;

        form.on('submit(part-form-filter)', function (data) {
            spUtil.ajax({
                url: '${request.contextPath}/admin/part/add-or-update',
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
