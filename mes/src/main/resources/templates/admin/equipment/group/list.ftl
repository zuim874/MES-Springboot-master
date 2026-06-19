<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>设备编组列表</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <#include "${request.contextPath}/common/common.ftl">
</head>
<body>
<div class="splayui-container">
    <div class="splayui-main">
        <form id="js-search-form" class="layui-form" lay-filter="js-q-form-filter">
            <div class="layui-form-item">
                <div class="layui-inline">
                    <label class="layui-form-label">编组名称</label>
                    <div class="layui-input-inline">
                        <input type="text" name="name" autocomplete="off" class="layui-input">
                    </div>
                </div>
                <div class="layui-inline">
                    <label class="layui-form-label">编组代码</label>
                    <div class="layui-input-inline">
                        <input type="text" name="code" autocomplete="off" class="layui-input">
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
        <button class="layui-btn layui-btn-danger layui-btn-sm" lay-event="deleteBatch"><i class="layui-icon">&#xe640;</i>批量删除</button>
        <@shiro.hasPermission name="user:add">
            <button class="layui-btn layui-btn-sm" lay-event="add"><i class="layui-icon">&#xe61f;</i>添加</button>
        </@shiro.hasPermission>
    </div>
</script>

<script type="text/html" id="js-record-table-toolbar-right">
    <a class="layui-btn layui-btn-xs" lay-event="edit" style="padding: 0 6px;"><i class="layui-icon layui-icon-edit"></i>编辑</a>
    <a class="layui-btn layui-btn-warm layui-btn-xs" lay-event="bindEquipment" style="padding: 0 6px;"><i class="layui-icon layui-icon-component"></i>绑定设备</a>
    <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="delete" style="padding: 0 6px;"><i class="layui-icon layui-icon-delete"></i>删除</a>
</script>

<script>
    layui.use(['form', 'table', 'spLayer', 'spTable'], function () {
        var form = layui.form,
            table = layui.table,
            spLayer = layui.spLayer,
            spTable = layui.spTable;

        var tableIns = spTable.render({
            url: '${request.contextPath}/admin/equipment/group/page',
            cols: [
                [{
                    type: 'checkbox'
                }, {
                    field: 'code', title: '编组代码', width: 120
                }, {
                    field: 'name', title: '编组名称', width: 150
                }, {
                    field: 'descr', title: '描述', width: 200
                }, {
                    field: 'processUnits', title: '已绑定加工单元', width: 220
                }, {
                    field: 'createUsername', title: '创建人', width: 120
                }, {
                    field: 'createTime', title: '创建时间', width: 160
                }, {
                    fixed: 'right', field: 'operate', title: '操作', toolbar: '#js-record-table-toolbar-right', unresize: true, width: 260
                }]
            ]
        });

        $(function () {
            form.render();
        });

        form.on('submit(js-search-filter)', function (data) {
            tableIns.reload({
                where: data.field,
                page: {
                    curr: 1
                }
            });
            return false;
        });

        table.on('toolbar(js-record-table-filter)', function (obj) {
            if (obj.event === 'add') {
                spLayer.open({
                    title: '添加编组',
                    area: ['90%', '90%'],
                    content: '${request.contextPath}/admin/equipment/group/add-or-update-ui'
                });
            }
        });

        table.on('tool(js-record-table-filter)', function (obj) {
            var data = obj.data;

            if (obj.event === 'edit') {
                spLayer.open({
                    title: '编辑编组',
                    area: ['90%', '90%'],
                    spWhere: {id: data.id},
                    content: '${request.contextPath}/admin/equipment/group/add-or-update-ui'
                });
            }

            if (obj.event === 'bindEquipment') {
                spLayer.open({
                    title: '绑定设备 - ' + data.name,
                    area: ['600px', '500px'],
                    content: '${request.contextPath}/admin/equipment/group/bind-equipment-ui?groupId=' + data.id
                });
            }

            if (obj.event === 'delete') {
                layer.confirm('确认删除吗？', function (index) {
                    $.ajax({
                        url: '${request.contextPath}/admin/equipment/group/add-or-update',
                        type: 'POST',
                        data: {
                            id: data.id,
                            isDeleted: '1'
                        },
                        success: function (res) {
                            if (res.code === 0) {
                                layer.msg('删除成功');
                                tableIns.reload();
                            } else {
                                layer.msg('删除失败：' + (res.msg || '未知错误'));
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
