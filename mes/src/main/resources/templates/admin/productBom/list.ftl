<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>产品BOM管理</title>
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
                    <label class="layui-form-label">BOM编码</label>
                    <div class="layui-input-inline">
                        <input type="text" name="bomCode" autocomplete="off" class="layui-input">
                    </div>
                </div>
                <div class="layui-inline">
                    <label class="layui-form-label">产品物料</label>
                    <div class="layui-input-inline">
                        <input type="text" name="productMaterielDesc" autocomplete="off" class="layui-input">
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
    <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="delete" style="padding: 0 6px;"><i class="layui-icon layui-icon-delete"></i>删除</a>
</script>

<script type="text/html" id="state-tpl">
    {{# if(d.state === '1'){ }}
    <span style="color: green;">已定版</span>
    {{# } else { }}
    <span style="color: orange;">草稿</span>
    {{# } }}
</script>

<script>
    layui.use(['form', 'table', 'spLayer', 'spTable'], function () {
        var form = layui.form,
            table = layui.table,
            spLayer = layui.spLayer,
            spTable = layui.spTable;

        var tableIns = spTable.render({
            url: '${request.contextPath}/admin/productBom/page',
            elem: '#js-record-table',
            toolbar: '#js-record-table-toolbar-top',
            cols: [[
                {type: 'checkbox', fixed: 'left'},
                {field: 'bomCode', title: 'BOM编码', width: 150, templet: function(d){
                    return '<a href="${request.contextPath}/admin/productBom/edit-ui?id=' + d.id + '" style="color: #01AAED; text-decoration: underline;">' + d.bomCode + '</a>';
                }},
                {field: 'productMaterielCode', title: '产品物料编码', width: 140},
                {field: 'productMaterielDesc', title: '产品物料名称', width: 180},
                {field: 'version', title: '版本号', width: 100},
                {field: 'state', title: '状态', width: 100, templet: '#state-tpl'},
                {field: 'remark', title: '备注'},
                {fixed: 'right', title: '操作', toolbar: '#js-record-table-toolbar-right', width: 130}
            ]]
        });

        form.on('submit(js-search-filter)', function (data) {
            tableIns.reload({
                where: data.field,
                page: { curr: 1 }
            });
            return false;
        });

        table.on('toolbar(js-record-table-filter)', function (obj) {
            if (obj.event === 'add') {
                spLayer.open({
                    title: '新增产品BOM',
                    area: ['600px', '400px'],
                    content: '${request.contextPath}/admin/productBom/add-or-update-ui'
                });
            }
            if (obj.event === 'deleteBatch') {
                var checkStatus = table.checkStatus(obj.config.id);
                var data = checkStatus.data;
                if (data.length === 0) {
                    layer.msg('请先选择数据');
                    return;
                }
                layer.confirm('确认删除选中数据吗？', function(index){
                    var ids = data.map(function(item){ return item.id; }).join(',');
                    spUtil.ajax({
                        url: '${request.contextPath}/admin/productBom/deleteBatch',
                        type: 'POST',
                        data: {ids: ids},
                        success: function(res){
                            if(res.code === 0){
                                layer.msg('删除成功');
                                tableIns.reload();
                            } else {
                                layer.msg(res.msg);
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
                if (data.state === '1') {
                    layer.msg('BOM已定版，不能编辑');
                    return;
                }
                spLayer.open({
                    title: '编辑产品BOM',
                    area: ['600px', '400px'],
                    spWhere: {id: data.id},
                    content: '${request.contextPath}/admin/productBom/add-or-update-ui'
                });
            }
            if (obj.event === 'delete') {
                if (data.state === '1') {
                    layer.msg('BOM已定版，不能删除');
                    return;
                }
                layer.confirm('确认删除吗？', function (index) {
                    spUtil.ajax({
                        url: '${request.contextPath}/admin/productBom/delete',
                        type: 'POST',
                        data: {id: data.id},
                        success: function (res) {
                            if (res.code === 0) {
                                layer.msg('删除成功');
                                tableIns.reload();
                            } else {
                                layer.msg(res.msg);
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
