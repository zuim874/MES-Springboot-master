<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>产品BOM管理</title>
    <#include "${request.contextPath}/common/common.ftl">
</head>
<body>
<div class="splayui-container">
    <div class="splayui-main">
        <form id="js-search-form" class="layui-form" lay-filter="js-q-form-filter">
            <div class="layui-form-item">
                <div class="layui-inline">
                    <label class="layui-form-label">产品物料名称</label>
                    <div class="layui-input-inline">
                        <input type="text" name="name" autocomplete="off" class="layui-input">
                    </div>
                </div>
                <div class="layui-inline">
                    <a class="layui-btn" lay-submit lay-filter="js-search-filter"><i class="layui-icon layui-icon-search layuiadmin-button-btn"></i></a>
                </div>
            </div>
        </form>

        <table class="layui-hide" id="js-bom-table" lay-filter="js-bom-table-filter"></table>
    </div>
</div>

<script type="text/html" id="js-bom-table-toolbar-top">
    <div class="layui-btn-container">
        <button class="layui-btn layui-btn-danger layui-btn-sm" lay-event="deleteBatch"><i class="layui-icon">&#xe640;</i>批量删除</button>
        <@shiro.hasPermission name="user:add">
            <button class="layui-btn layui-btn-sm" lay-event="add"><i class="layui-icon">&#xe61f;</i>添加</button>
        </@shiro.hasPermission>
    </div>
</script>

<script type="text/html" id="js-bom-table-toolbar-right">
    <a class="layui-btn layui-btn-xs" lay-event="node" style="padding: 0 6px;"><i class="layui-icon layui-icon-senior"></i>节点管理</a>
    <a class="layui-btn layui-btn-xs" lay-event="edit" style="padding: 0 6px;"><i class="layui-icon layui-icon-edit"></i>编辑</a>
    <a class="layui-btn layui-btn-xs layui-btn-warm" lay-event="freeze" style="padding: 0 6px;"><i class="layui-icon layui-icon-ok"></i>定版</a>
    <a class="layui-btn layui-btn-xs layui-btn-normal" lay-event="copy" style="padding: 0 6px;"><i class="layui-icon layui-icon-template-1"></i>复制</a>
    <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="delete" style="padding: 0 6px;"><i class="layui-icon layui-icon-delete"></i>删除</a>
</script>

<script type="text/html" id="js-tpl-valid">
    {{# if(d.isValid === '1'){ }}
    <span class="layui-btn layui-btn-xs layui-btn-normal">有效</span>
    {{# } else { }}
    <span class="layui-btn layui-btn-xs layui-btn-disabled">无效</span>
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

        var bomTableIns = spTable.render({
            url: '${request.contextPath}/admin/productBom/page',
            elem: '#js-bom-table',
            toolbar: '#js-bom-table-toolbar-top',
            cols: [[
                {type: 'checkbox', fixed: 'left'},
                {field: 'code', title: '产品物料编码', width: 150},
                {field: 'name', title: '产品物料名称', width: 180},
                {field: 'version', title: '版本', width: 100},
                {field: 'isValid', title: '有效性', templet: '#js-tpl-valid', width: 100},
                {field: 'isFrozen', title: '定版标识', templet: '#js-tpl-frozen', width: 100},
                {field: 'remark', title: '备注'},
                {field: 'updateTime', title: '更新时间', width: 160},
                {fixed: 'right', title: '操作', toolbar: '#js-bom-table-toolbar-right', width: 420}
            ]]
        });

        form.on('submit(js-search-filter)', function (data) {
            bomTableIns.reload({where: data.field, page: {curr: 1}});
            return false;
        });

        table.on('toolbar(js-bom-table-filter)', function (obj) {
            var checkStatus = table.checkStatus(obj.config.id);
            switch (obj.event) {
                case 'add':
                    spLayer.open({
                        title: '新增产品BOM',
                        area: ['600px', '400px'],
                        content: '${request.contextPath}/admin/productBom/add-or-update-ui'
                    }, function () {
                        bomTableIns.reload();
                    });
                    break;
                case 'deleteBatch':
                    var data = checkStatus.data;
                    if (data.length === 0) {
                        layer.msg('请选择要删除的记录', {icon: 2});
                        return;
                    }
                    var ids = data.map(function (item) { return item.id; });
                    layer.confirm('确定删除选中的记录吗？', function (index) {
                        // 简化：逐个删除
                        var count = 0;
                        ids.forEach(function(id) {
                            spUtil.ajax({
                                url: '${request.contextPath}/admin/productBom/delete',
                                type: 'POST',
                                data: {id: id},
                                async: false,
                                success: function (res) {
                                    if (res.code === 0) count++;
                                }
                            });
                        });
                        layer.close(index);
                        layer.msg('成功删除' + count + '条记录', {icon: 1});
                        bomTableIns.reload();
                    });
                    break;
            }
        });

        table.on('tool(js-bom-table-filter)', function (obj) {
            var data = obj.data;
            switch (obj.event) {
                case 'edit':
                    if (data.isFrozen === '1') {
                        layer.msg('已定版的BOM不能编辑', {icon: 2});
                        return;
                    }
                    spLayer.open({
                        title: '编辑产品BOM',
                        area: ['600px', '400px'],
                        content: '${request.contextPath}/admin/productBom/add-or-update-ui?id=' + data.id
                    }, function () {
                        bomTableIns.reload();
                    });
                    break;
                case 'delete':
                    if (data.isFrozen === '1') {
                        layer.msg('已定版的BOM不能删除', {icon: 2});
                        return;
                    }
                    layer.confirm('确定删除该产品BOM吗？', function (index) {
                        spUtil.ajax({
                            url: '${request.contextPath}/admin/productBom/delete',
                            type: 'POST',
                            data: {id: data.id},
                            success: function (res) {
                                layer.close(index);
                                if (res.code === 0) {
                                    layer.msg('删除成功', {icon: 1});
                                    bomTableIns.reload();
                                } else {
                                    layer.msg(res.msg, {icon: 2});
                                }
                            }
                        });
                    });
                    break;
                case 'freeze':
                    if (data.isFrozen === '1') {
                        layer.msg('该BOM已处于定版状态', {icon: 2});
                        return;
                    }
                    layer.confirm('确定定版吗？定版后将不能编辑和删除', function (index) {
                        spUtil.ajax({
                            url: '${request.contextPath}/admin/productBom/freeze',
                            type: 'POST',
                            data: {id: data.id},
                            success: function (res) {
                                layer.close(index);
                                if (res.code === 0) {
                                    layer.msg('定版成功', {icon: 1});
                                    bomTableIns.reload();
                                } else {
                                    layer.msg(res.msg, {icon: 2});
                                }
                            }
                        });
                    });
                    break;
                case 'copy':
                    layer.confirm('确定复制该产品BOM创建新版本吗？', function (index) {
                        spUtil.ajax({
                            url: '${request.contextPath}/admin/productBom/copy',
                            type: 'POST',
                            data: {id: data.id},
                            success: function (res) {
                                layer.close(index);
                                if (res.code === 0) {
                                    layer.msg('复制成功', {icon: 1});
                                    bomTableIns.reload();
                                } else {
                                    layer.msg(res.msg, {icon: 2});
                                }
                            }
                        });
                    });
                    break;
                case 'node':
                    layer.open({
                        type: 2,
                        title: 'BOM节点管理 - ' + data.name,
                        area: ['1300px', '700px'],
                        maxmin: true,
                        content: '${request.contextPath}/admin/productBom/node-ui?bomId=' + data.id,
                        end: function () {
                            bomTableIns.reload();
                        }
                    });
                    break;
            }
        });
    });
</script>
</body>
</html>
