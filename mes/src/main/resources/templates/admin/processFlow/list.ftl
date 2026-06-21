<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>工序流程定义</title>
    <#include "${request.contextPath}/common/common.ftl">
</head>
<body>
<div class="splayui-container">
    <div class="splayui-main">
        <form id="js-search-form" class="layui-form" lay-filter="js-q-form-filter">
            <div class="layui-form-item">
                <div class="layui-inline">
                    <label class="layui-form-label">编码/名称</label>
                    <div class="layui-input-inline">
                        <input type="text" name="name" autocomplete="off" class="layui-input">
                    </div>
                </div>
                <div class="layui-inline">
                    <a class="layui-btn" lay-submit lay-filter="js-search-filter"><i class="layui-icon layui-icon-search layuiadmin-button-btn"></i></a>
                </div>
            </div>
        </form>

        <table class="layui-hide" id="js-record-table" lay-filter="js-record-table-filter"></table>
    </div>
</div>

<script type="text/html" id="js-record-table-toolbar-top">
    <div class="layui-btn-container">
        <@shiro.hasPermission name="user:add">
            <button class="layui-btn layui-btn-sm" lay-event="add"><i class="layui-icon">&#xe61f;</i>新增</button>
        </@shiro.hasPermission>
        <button class="layui-btn layui-btn-danger layui-btn-sm" lay-event="deleteBatch"><i class="layui-icon">&#xe640;</i>批量删除</button>
    </div>
</script>

<script type="text/html" id="js-record-table-toolbar-right">
    <a class="layui-btn layui-btn-xs" lay-event="edit"><i class="layui-icon layui-icon-edit"></i>编辑</a>
    <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="delete"><i class="layui-icon layui-icon-delete"></i>删除</a>
</script>

<script>
    layui.use(['form', 'table', 'spLayer', 'spTable'], function () {
        var form = layui.form,
            table = layui.table,
            spLayer = layui.spLayer,
            spTable = layui.spTable;

        var tableIns = spTable.render({
            url: '${request.contextPath}/admin/processFlow/page',
            cols: [
                [{type: 'checkbox'},
                {field: 'code', title: '流程编码', width: 150},
                {field: 'name', title: '流程名称', width: 180},
                {field: 'processChain', title: '时序流程'},
                {field: 'remark', title: '备注'},
                {fixed: 'right', field: 'operate', title: '操作', toolbar: '#js-record-table-toolbar-right', unresize: true, width: 150}]
            ]
        });

        form.on('submit(js-search-filter)', function (data) {
            tableIns.reload({where: data.field});
            return false;
        });

        table.on('toolbar(js-record-table-filter)', function (obj) {
            var checkStatus = table.checkStatus(obj.config.id);
            if (obj.event === 'add') {
                spLayer.open({
                    title: '新增工序流程',
                    area: ['720px', '580px'],
                    content: '${request.contextPath}/admin/processFlow/add-or-update-ui',
                    end: function () {
                        tableIns.reload();
                    }
                });
            } else if (obj.event === 'deleteBatch') {
                var data = checkStatus.data;
                if (data.length === 0) {
                    layer.msg('请选择要删除的记录', {icon: 0});
                    return;
                }
                layer.confirm('确定删除选中的' + data.length + '条记录吗？', function (index) {
                    var ids = data.map(function (item) { return item.id; }).join(',');
                    spUtil.ajax({
                        url: '${request.contextPath}/admin/processFlow/delete',
                        type: 'POST',
                        data: {id: ids},
                        success: function (res) {
                            if (res.code === 0) {
                                layer.msg('删除成功', {icon: 1});
                                tableIns.reload();
                            } else {
                                layer.msg(res.msg, {icon: 2});
                            }
                        }
                    });
                    layer.close(index);
                });
            }
        });

        table.on('tool(js-record-table-filter)', function (obj) {
            var data = obj.data;
            if (obj.event === 'edit') {
                spLayer.open({
                    title: '编辑工序流程',
                    area: ['720px', '580px'],
                    content: '${request.contextPath}/admin/processFlow/add-or-update-ui',
                    spWhere: {id: data.id},
                    end: function () {
                        tableIns.reload();
                    }
                });
            } else if (obj.event === 'delete') {
                layer.confirm('确定删除该记录吗？', function (index) {
                    spUtil.ajax({
                        url: '${request.contextPath}/admin/processFlow/delete',
                        type: 'POST',
                        data: {id: data.id},
                        success: function (res) {
                            if (res.code === 0) {
                                layer.msg('删除成功', {icon: 1});
                                tableIns.reload();
                            } else {
                                layer.msg(res.msg, {icon: 2});
                            }
                        }
                    });
                    layer.close(index);
                });
            }
        });
    });
</script>
</body>
</html>
