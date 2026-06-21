<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>工艺流程管理</title>
    <#include "${request.contextPath}/common/common.ftl">
</head>
<body>
<div class="splayui-container">
    <div class="splayui-main">
        <form id="js-search-form" class="layui-form" lay-filter="js-q-form-filter">
            <div class="layui-form-item">
                <div class="layui-inline">
                    <label class="layui-form-label">产品名称</label>
                    <div class="layui-input-inline">
                        <input type="text" name="name" autocomplete="off" class="layui-input">
                    </div>
                </div>
                <div class="layui-inline">
                    <a class="layui-btn" lay-submit lay-filter="js-search-filter"><i class="layui-icon layui-icon-search layuiadmin-button-btn"></i></a>
                </div>
            </div>
        </form>

        <table class="layui-hide" id="js-plan-table" lay-filter="js-plan-table-filter"></table>
    </div>
</div>

<script type="text/html" id="js-record-table-toolbar-top">
    <div class="layui-btn-container">
        <@shiro.hasPermission name="user:add">
        </@shiro.hasPermission>
    </div>
</script>

<script type="text/html" id="js-plan-table-toolbar-right">
    <a class="layui-btn layui-btn-xs layui-btn-normal" lay-event="detail"><i class="layui-icon layui-icon-senior"></i>工艺规划</a>
</script>

<script type="text/html" id="js-tpl-locked">
    {{# if(d.processPlanLocked === '1'){ }}
    <span class="layui-btn layui-btn-xs layui-btn-danger">已锁定</span>
    {{# } else { }}
    <span class="layui-btn layui-btn-xs">未锁定</span>
    {{# } }}
</script>

<script type="text/html" id="js-tpl-frozen">
    {{# if(d.isFrozen === '1'){ }}
    <span class="layui-btn layui-btn-xs layui-btn-danger">已定版</span>
    {{# } else { }}
    <span class="layui-btn layui-btn-xs">未定版</span>
    {{# } }}
</script>

<script>
    layui.use(['form', 'table', 'spLayer', 'spTable'], function () {
        var form = layui.form,
            table = layui.table,
            spLayer = layui.spLayer,
            spTable = layui.spTable;

        var planTableIns = spTable.render({
            url: '${request.contextPath}/admin/processPlan/page',
            elem: '#js-plan-table',
            cols: [[
                {type: 'checkbox', fixed: 'left'},
                {field: 'code', title: '产品物料编码', width: 150},
                {field: 'name', title: '产品名称', width: 180},
                {field: 'version', title: '版本', width: 100},
                {field: 'isFrozen', title: '定版标识', templet: '#js-tpl-frozen', width: 100},
                {field: 'processPlanLocked', title: '工艺锁定', templet: '#js-tpl-locked', width: 100},
                {field: 'remark', title: '备注'},
                {fixed: 'right', title: '操作', toolbar: '#js-plan-table-toolbar-right', width: 120}
            ]]
        });

        form.on('submit(js-search-filter)', function (data) {
            planTableIns.reload({where: data.field});
            return false;
        });

        table.on('tool(js-plan-table-filter)', function (obj) {
            var data = obj.data;
            if (obj.event === 'detail') {
                spLayer.open({
                    title: '工艺规划 - ' + data.name,
                    area: ['900px', '580px'],
                    content: '${request.contextPath}/admin/processPlan/detail-ui',
                    spWhere: {bomId: data.id},
                    btn: []
                });
            }
        });
    });
</script>
</body>
</html>