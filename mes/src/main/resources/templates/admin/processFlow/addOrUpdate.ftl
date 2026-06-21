<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>工序流程定义</title>
    <#include "${request.contextPath}/common/common.ftl">
</head>
<body>
<div class="splayui-container">
    <div class="splayui-main">
        <form class="layui-form splayui-form" lay-filter="js-form-filter">
            <div class="layui-form-item">
                <div class="layui-inline">
                    <label class="layui-form-label sp-required">流程编码</label>
                    <div class="layui-input-inline">
                        <input type="text" name="code" lay-verify="required" autocomplete="off" class="layui-input" value="${(flow.code)!}">
                    </div>
                </div>
                <div class="layui-inline">
                    <label class="layui-form-label sp-required">流程名称</label>
                    <div class="layui-input-inline">
                        <input type="text" name="name" lay-verify="required" autocomplete="off" class="layui-input" value="${(flow.name)!}">
                    </div>
                </div>
            </div>
            <div class="layui-form-item">
                <label class="layui-form-label">时序流程</label>
                <div class="layui-input-inline" style="width: 514px">
                    <input type="text" id="js-process-chain" readonly autocomplete="off" class="layui-input" value="${(flow.processChain)!}">
                </div>
            </div>
            <div class="layui-form-item">
                <label class="layui-form-label">备注</label>
                <div class="layui-input-inline" style="width: 514px">
                    <input type="text" name="remark" autocomplete="off" class="layui-input" value="${(flow.remark)!}">
                </div>
            </div>

            <div class="layui-form-item">
                <fieldset class="layui-elem-field layui-field-title">
                    <legend>工序时序编排（左侧选择工序，右侧调整顺序）</legend>
                </fieldset>
                <div id="js-shuttle" class="demo-transfer"></div>
            </div>

            <div class="layui-form-item layui-hide">
                <div class="layui-input-block">
                    <input type="hidden" id="js-id" name="id" value="${(flow.id)!}"/>
                    <button id="js-submit" class="layui-btn" lay-demotransferactive="getData" lay-submit lay-filter="js-submit-filter">确定</button>
                </div>
            </div>
        </form>
    </div>
</div>

<script>
    layui.use(['form', 'util', 'transfer'], function () {
        var form = layui.form,
            util = layui.util,
            transfer = layui.transfer;

        var requestParmaArr = [];
        var allProcesses = [];
        var currentProcessIds = [];

        <#list allProcess as p>
        allProcesses.push({value: '${p.value}', title: '${p.title}'});
        </#list>

        <#if currentProcessIds??>
            <#list currentProcessIds as pid>
            currentProcessIds.push('${pid}');
            </#list>
        </#if>

        transfer.render({
            elem: '#js-shuttle',
            title: ['全部工序', '已选工序（按顺序）'],
            data: allProcesses,
            value: currentProcessIds,
            id: 'keyProcess'
        });

        util.event('lay-demoTransferActive', {
            getData: function (othis) {
                requestParmaArr = transfer.getData('keyProcess');
            }
        });

        form.on('submit(js-submit-filter)', function (data) {
            var processIds = requestParmaArr.map(function (item) {
                return item.value;
            });

            var submitData = {
                id: data.field.id || null,
                code: data.field.code,
                name: data.field.name,
                remark: data.field.remark,
                processIds: processIds
            };

            $.ajax({
                url: '${request.contextPath}/admin/processFlow/add-or-update',
                type: 'POST',
                contentType: 'application/json;charset=UTF-8',
                data: JSON.stringify(submitData),
                dataType: 'json',
                success: function (res) {
                    if (res.code === 0) {
                        layer.msg('保存成功', {icon: 1, time: 1000}, function () {
                            var index = parent.layer.getFrameIndex(window.name);
                            parent.layer.close(index);
                        });
                    } else {
                        layer.msg(res.msg || '保存失败', {icon: 2});
                    }
                },
                error: function () {
                    layer.msg('保存失败', {icon: 2});
                }
            });
            return false;
        });
    });
</script>
</body>
</html>
