<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>零部件定义</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <#include "${request.contextPath}/common/common.ftl">
</head>
<body>
<div class="splayui-container">
    <div class="splayui-main">
        <!-- 查询 -->
        <form id="js-search-form" class="layui-form" lay-filter="js-q-form-filter">
            <div class="layui-form-item">
                <div class="layui-inline">
                    <label class="layui-form-label">零部件名称</label>
                    <div class="layui-input-inline">
                        <input type="text" name="name" autocomplete="off" class="layui-input">
                    </div>
                </div>
                <div class="layui-inline">
                    <a class="layui-btn" lay-submit lay-filter="js-search-filter"><i class="layui-icon layui-icon-search layuiadmin-button-btn"></i></a>
                </div>
            </div>
        </form>

        <!-- 表格 -->
        <table class="layui-hide" id="js-part-table" lay-filter="js-part-table-filter"></table>
    </div>
</div>

<script type="text/html" id="js-part-table-toolbar-top">
    <div class="layui-btn-container">
        <button class="layui-btn layui-btn-danger layui-btn-sm" lay-event="deleteBatch"><i class="layui-icon">&#xe640;</i>批量删除</button>
        <@shiro.hasPermission name="user:add">
            <button class="layui-btn layui-btn-sm" lay-event="add"><i class="layui-icon">&#xe61f;</i>添加</button>
        </@shiro.hasPermission>
    </div>
</script>

<script type="text/html" id="js-part-table-toolbar-right">
    <a class="layui-btn layui-btn-xs" lay-event="edit" style="padding: 0 6px;"><i class="layui-icon layui-icon-edit"></i>编辑</a>
    <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="delete" style="padding: 0 6px;"><i class="layui-icon layui-icon-delete"></i>删除</a>
</script>

<script>
    layui.use(['form', 'table', 'spLayer', 'spTable'], function () {
        var form = layui.form,
            table = layui.table,
            spLayer = layui.spLayer,
            spTable = layui.spTable;

        var partTableIns = spTable.render({
            url: '${request.contextPath}/admin/part/page',
            elem: '#js-part-table',
            toolbar: '#js-part-table-toolbar-top',
            cols: [[
                {type: 'checkbox', fixed: 'left'},
                {field: 'code', title: '零部件编号', width: 150},
                {field: 'name', title: '零部件名称', width: 180},
                {field: 'remark', title: '备注'},
                {fixed: 'right', title: '操作', toolbar: '#js-part-table-toolbar-right', width: 150}
            ]]
        });

        // 监听查询
        form.on('submit(js-search-filter)', function (data) {
            partTableIns.reload({
                where: data.field
            });
            return false;
        });

        // 监听工具栏
        table.on('toolbar(js-part-table-filter)', function (obj) {
            var checkStatus = table.checkStatus(obj.config.id);
            switch (obj.event) {
                case 'add':
                    spLayer.open({
                        title: '新增零部件',
                        area: ['600px', '400px'],
                        content: '${request.contextPath}/admin/part/add-or-update-ui'
                    }, function () {
                        partTableIns.reload();
                    });
                    break;
                case 'deleteBatch':
                    var data = checkStatus.data;
                    if (data.length === 0) {
                        layer.msg('请至少选择一条记录', {icon: 2});
                        return;
                    }
                    var ids = [];
                    for (var i = 0; i < data.length; i++) {
                        ids.push(data[i].id);
                    }
                    layer.confirm('确定删除选中的零部件吗？', function (index) {
                        var successCount = 0;
                        var failCount = 0;
                        for (var j = 0; j < ids.length; j++) {
                            spUtil.ajax({
                                url: '${request.contextPath}/admin/part/delete',
                                data: {id: ids[j]},
                                async: false,
                                success: function (res) {
                                    if (res.code === 0) successCount++;
                                    else failCount++;
                                }
                            });
                        }
                        layer.close(index);
                        layer.msg('成功：' + successCount + '，失败：' + failCount, {icon: 1});
                        partTableIns.reload();
                    });
                    break;
            }
        });

        // 监听行工具事件
        table.on('tool(js-part-table-filter)', function (obj) {
            var data = obj.data;
            switch (obj.event) {
                case 'edit':
                    spLayer.open({
                        title: '编辑零部件',
                        area: ['600px', '400px'],
                        content: '${request.contextPath}/admin/part/add-or-update-ui?id=' + data.id
                    }, function () {
                        partTableIns.reload();
                    });
                    break;
                case 'delete':
                    layer.confirm('确定删除该零部件吗？', function (index) {
                        spUtil.ajax({
                            url: '${request.contextPath}/admin/part/delete',
                            data: {id: data.id},
                            success: function (res) {
                                if (res.code === 0) {
                                    layer.msg('删除成功', {icon: 1});
                                    partTableIns.reload();
                                } else {
                                    layer.msg(res.msg, {icon: 2});
                                }
                            }
                        });
                        layer.close(index);
                    });
                    break;
            }
        });
    });
</script>
</body>
</html>
