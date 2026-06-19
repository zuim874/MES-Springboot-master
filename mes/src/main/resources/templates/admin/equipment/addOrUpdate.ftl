<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>设备信息</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <#include "${request.contextPath}/common/common.ftl">
</head>
<body>
<div class="splayui-container">
    <div class="splayui-main">
        <form class="layui-form" action="">
            <input type="hidden" name="id" value="${(result.id)!}">

            <div class="layui-form-item">
                <div class="layui-inline">
                    <label class="layui-form-label">设备编号</label>
                    <div class="layui-input-inline">
                        <input type="text" name="code" lay-verify="required" autocomplete="off" class="layui-input" value="${(result.code)!}">
                    </div>
                </div>
                <div class="layui-inline">
                    <label class="layui-form-label">设备名称</label>
                    <div class="layui-input-inline">
                        <input type="text" name="name" lay-verify="required" autocomplete="off" class="layui-input" value="${(result.name)!}">
                    </div>
                </div>
            </div>

            <div class="layui-form-item">
                <div class="layui-inline">
                    <label class="layui-form-label">型号与规格</label>
                    <div class="layui-input-inline">
                        <input type="text" name="modelSpec" autocomplete="off" class="layui-input" value="${(result.modelSpec)!}">
                    </div>
                </div>
                <div class="layui-inline">
                    <label class="layui-form-label">连接方式</label>
                    <div class="layui-input-inline">
                        <input type="text" name="connectionType" autocomplete="off" class="layui-input" value="${(result.connectionType)!}">
                    </div>
                </div>
            </div>

            <div class="layui-form-item layui-hide">
                <div class="layui-input-block">
                    <input id="js-id" name="id" value="${(result.id)!}"/>
                    <button id="js-submit" class="layui-btn" lay-submit lay-filter="js-submit-filter">确定</button>
                </div>
            </div>
        </form>
    </div>
</div>

<script>
    layui.use(['form', 'util'], function () {
        var form = layui.form,
            util = layui.util;

        form.on('submit(js-submit-filter)', function (data) {
            spUtil.submitForm({
                url: "${request.contextPath}/admin/equipment/add-or-update",
                data: data.field
            });

            return false;
        });
    });
</script>
</body>
</html>
