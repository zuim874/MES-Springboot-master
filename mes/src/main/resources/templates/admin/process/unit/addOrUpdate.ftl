<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>加工单元信息</title>
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
                    <label class="layui-form-label">单元代码</label>
                    <div class="layui-input-inline">
                        <input type="text" name="code" lay-verify="required" autocomplete="off" class="layui-input" value="${(result.code)!}">
                    </div>
                </div>
                <div class="layui-inline">
                    <label class="layui-form-label">单元名称</label>
                    <div class="layui-input-inline">
                        <input type="text" name="name" lay-verify="required" autocomplete="off" class="layui-input" value="${(result.name)!}">
                    </div>
                </div>
            </div>

            <div class="layui-form-item">
                <div class="layui-inline">
                    <label class="layui-form-label">单元类型</label>
                    <div class="layui-input-inline">
                        <select name="type" lay-verify="required">
                            <option value="">请选择</option>
                            <option value="1" <#if (result.type)?? && result.type == '1'>selected</#if>>人员作业单元</option>
                            <option value="2" <#if (result.type)?? && result.type == '2'>selected</#if>>设备作业单元</option>
                        </select>
                    </div>
                </div>
                <div class="layui-inline">
                    <label class="layui-form-label">是否有线边库</label>
                    <div class="layui-input-inline">
                        <select name="hasLineSideWarehouse" lay-verify="required">
                            <option value="">请选择</option>
                            <option value="0" <#if !(result.hasLineSideWarehouse??) || result.hasLineSideWarehouse == '0'>selected</#if>>否</option>
                            <option value="1" <#if (result.hasLineSideWarehouse)?? && result.hasLineSideWarehouse == '1'>selected</#if>>是</option>
                        </select>
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
                url: "${request.contextPath}/admin/process/unit/add-or-update",
                data: data.field
            });

            return false;
        });
    });
</script>
</body>
</html>
