<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>库房库位定义</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <#include "${request.contextPath}/common/common.ftl">
    <style>
        .location-title {
            font-size: 16px;
            font-weight: bold;
            margin: 15px 0 10px;
            padding-left: 10px;
            border-left: 4px solid #1E9FFF;
        }
        .location-info {
            color: #666;
            font-size: 14px;
            margin-left: 10px;
        }
    </style>
</head>
<body>
<div class="splayui-container">
    <div class="splayui-main">
        <!-- 库房查询 -->
        <form id="js-search-form" class="layui-form" lay-filter="js-q-form-filter">
            <div class="layui-form-item">
                <div class="layui-inline">
                    <label class="layui-form-label">库房名称</label>
                    <div class="layui-input-inline">
                        <input type="text" name="name" autocomplete="off" class="layui-input">
                    </div>
                </div>
                <div class="layui-inline">
                    <label class="layui-form-label">库房编码</label>
                    <div class="layui-input-inline">
                        <input type="text" name="code" autocomplete="off" class="layui-input">
                    </div>
                </div>
                <div class="layui-inline">
                    <label class="layui-form-label">库房类型</label>
                    <div class="layui-input-inline">
                        <select name="type" id="js-search-type">
                            <option value="">全部</option>
                        </select>
                    </div>
                </div>
                <div class="layui-inline">
                    <a class="layui-btn" lay-submit lay-filter="js-search-filter"><i class="layui-icon layui-icon-search layuiadmin-button-btn"></i></a>
                </div>
            </div>
        </form>

        <!-- 库房表格 -->
        <table class="layui-hide" id="js-warehouse-table" lay-filter="js-warehouse-table-filter"></table>

        <!-- 库位信息 -->
        <div class="location-title">
            库位信息
            <span class="location-info" id="js-location-title"></span>
        </div>
        <table class="layui-hide" id="js-location-table" lay-filter="js-location-table-filter"></table>
    </div>
</div>

<script type="text/html" id="js-warehouse-table-toolbar-top">
    <div class="layui-btn-container">
        <button class="layui-btn layui-btn-danger layui-btn-sm" lay-event="deleteBatch"><i class="layui-icon">&#xe640;</i>批量删除</button>
        <@shiro.hasPermission name="user:add">
            <button class="layui-btn layui-btn-sm" lay-event="add"><i class="layui-icon">&#xe61f;</i>添加</button>
        </@shiro.hasPermission>
    </div>
</script>

<script type="text/html" id="js-warehouse-table-toolbar-right">
    <a class="layui-btn layui-btn-xs" lay-event="edit" style="padding: 0 6px;"><i class="layui-icon layui-icon-edit"></i>编辑</a>
    <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="delete" style="padding: 0 6px;"><i class="layui-icon layui-icon-delete"></i>删除</a>
</script>

<script>
    layui.use(['form', 'table', 'spLayer', 'spTable'], function () {
        var form = layui.form,
            table = layui.table,
            spLayer = layui.spLayer,
            spTable = layui.spTable;

        // 加载库房类型字典
        spUtil.ajax({
            url: '${request.contextPath}/basedata/dict/list/warehouse_type',
            success: function (res) {
                if (res.code === 0) {
                    var html = '<option value="">全部</option>';
                    for (var i = 0; i < res.data.length; i++) {
                        var item = res.data[i];
                        html += '<option value="' + item.value + '">' + item.name + '</option>';
                    }
                    $('#js-search-type').html(html);
                    form.render('select');
                }
            }
        });

        var warehouseTableIns = spTable.render({
            url: '${request.contextPath}/admin/warehouse/page',
            elem: '#js-warehouse-table',
            toolbar: '#js-warehouse-table-toolbar-top',
            cols: [[
                {type: 'checkbox', fixed: 'left'},
                {field: 'code', title: '库房编码', width: 120},
                {field: 'name', title: '库房名称', width: 150},
                {field: 'type', title: '库房类型', width: 100},
                {field: 'groupCount', title: '组数', width: 80},
                {field: 'rowCount', title: '排数', width: 80},
                {field: 'layerCount', title: '层数', width: 80},
                {field: 'columnCount', title: '列数', width: 80},
                {field: 'groupCount', title: '库位总数', width: 100, templet: function(d) {
                    return d.groupCount * d.rowCount * d.layerCount * d.columnCount;
                }},
                {fixed: 'right', title: '操作', toolbar: '#js-warehouse-table-toolbar-right', width: 150}
            ]]
        });

        // 库房行点击事件：加载库位
        table.on('row(js-warehouse-table-filter)', function(obj) {
            var data = obj.data;
            var total = data.groupCount * data.rowCount * data.layerCount * data.columnCount;
            $('#js-location-title').text('【' + data.name + '】 规格：' + data.groupCount + '组 x ' + data.rowCount + '排 x ' + data.layerCount + '层 x ' + data.columnCount + '列 = ' + total + '个库位');
            loadLocationTable(data.id);
            obj.tr.addClass('layui-table-click').siblings().removeClass('layui-table-click');
        });

        // 监听查询
        form.on('submit(js-search-filter)', function (data) {
            warehouseTableIns.reload({
                where: data.field
            });
            return false;
        });

        // 监听库房表格工具栏
        table.on('toolbar(js-warehouse-table-filter)', function (obj) {
            var checkStatus = table.checkStatus(obj.config.id);
            switch (obj.event) {
                case 'add':
                    spLayer.open({
                        title: '新增库房',
                        area: ['700px', '500px'],
                        content: '${request.contextPath}/admin/warehouse/add-or-update-ui'
                    }, function () {
                        warehouseTableIns.reload();
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
                    layer.confirm('确定删除选中的库房吗？删除库房将同步删除其所有库位。', function (index) {
                        var successCount = 0;
                        var failCount = 0;
                        for (var j = 0; j < ids.length; j++) {
                            spUtil.ajax({
                                url: '${request.contextPath}/admin/warehouse/delete',
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
                        warehouseTableIns.reload();
                    });
                    break;
            }
        });

        // 监听行工具事件
        table.on('tool(js-warehouse-table-filter)', function (obj) {
            var data = obj.data;
            switch (obj.event) {
                case 'edit':
                    spLayer.open({
                        title: '编辑库房',
                        area: ['700px', '500px'],
                        content: '${request.contextPath}/admin/warehouse/add-or-update-ui?id=' + data.id
                    }, function () {
                        warehouseTableIns.reload();
                    });
                    break;
                case 'delete':
                    layer.confirm('确定删除该库房吗？删除库房将同步删除其所有库位。', function (index) {
                        spUtil.deleteForm({
                            url: '${request.contextPath}/admin/warehouse/delete?id=' + data.id
                        }, function () {
                            warehouseTableIns.reload();
                            layer.close(index);
                        });
                    });
                    break;
            }
        });

        // 加载库位表格
        function loadLocationTable(warehouseId) {
            table.render({
                elem: '#js-location-table',
                url: '${request.contextPath}/admin/warehouse/location-list?warehouseId=' + warehouseId,
                page: false,
                cols: [[
                    {type: 'numbers', width: 60, title: '序号'},
                    {field: 'code', title: '库位编码', width: 180},
                    {field: 'groupNum', title: '组号', width: 80},
                    {field: 'rowNum', title: '排号', width: 80},
                    {field: 'layerNum', title: '层号', width: 80},
                    {field: 'columnNum', title: '列号', width: 80}
                ]]
            });
        }
    });
</script>
</body>
</html>
