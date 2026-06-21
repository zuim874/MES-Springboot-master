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
        /* 可视化二维表样式 */
        .matrix-container {
            margin-top: 10px;
            padding: 10px;
            background: #f8f8f8;
            border-radius: 4px;
        }
        .matrix-toolbar {
            margin-bottom: 10px;
        }
        .matrix-grid {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            justify-content: flex-start;
        }
        .matrix-cell {
            width: 120px;
            min-height: 80px;
            border: 1px solid #ddd;
            border-radius: 4px;
            padding: 6px;
            cursor: pointer;
            transition: all 0.2s;
            background: #fff;
            position: relative;
        }
        .matrix-cell:hover {
            box-shadow: 0 2px 8px rgba(0,0,0,0.15);
            transform: translateY(-2px);
        }
        .matrix-cell.occupied {
            background: #e6f7ff;
            border-color: #1890ff;
        }
        .matrix-cell .cell-code {
            font-size: 12px;
            color: #666;
            word-break: break-all;
        }
        .matrix-cell .cell-status {
            font-size: 11px;
            margin-top: 4px;
            padding: 2px 4px;
            border-radius: 2px;
            display: inline-block;
        }
        .matrix-cell .cell-status.empty {
            background: #f0f0f0;
            color: #999;
        }
        .matrix-cell .cell-status.has-materiel {
            background: #1890ff;
            color: #fff;
        }
        .matrix-cell .cell-materiel {
            font-size: 11px;
            color: #333;
            margin-top: 4px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        .matrix-legend {
            margin-top: 10px;
            font-size: 12px;
        }
        .matrix-legend span {
            margin-right: 15px;
        }
        .legend-dot {
            display: inline-block;
            width: 12px;
            height: 12px;
            border-radius: 2px;
            margin-right: 4px;
            vertical-align: middle;
            border: 1px solid #ddd;
        }
        .legend-dot.empty { background: #fff; }
        .legend-dot.occupied { background: #e6f7ff; border-color: #1890ff; }
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

        <!-- 可视化二维表 -->
        <div class="location-title">
            库位可视化看板
            <span class="location-info" id="js-matrix-title">请先选择库房</span>
        </div>
        <div class="matrix-container">
            <div class="matrix-toolbar layui-form">
                <div class="layui-inline">
                    <label class="layui-form-label" style="width:auto;">排号</label>
                    <div class="layui-input-inline" style="width:80px;">
                        <select id="js-matrix-row" lay-filter="matrix-row-filter"></select>
                    </div>
                </div>
                <div class="layui-inline">
                    <label class="layui-form-label" style="width:auto;">层号</label>
                    <div class="layui-input-inline" style="width:80px;">
                        <select id="js-matrix-layer" lay-filter="matrix-layer-filter"></select>
                    </div>
                </div>
                <div class="layui-inline">
                    <button class="layui-btn layui-btn-sm layui-btn-primary" id="js-refresh-matrix"><i class="layui-icon">&#xe669;</i>刷新</button>
                </div>
            </div>
            <div class="matrix-grid" id="js-matrix-grid"></div>
            <div class="matrix-legend">
                <span><i class="legend-dot empty"></i>空闲库位</span>
                <span><i class="legend-dot occupied"></i>已存放物料</span>
            </div>
        </div>
    </div>
</div>

<!-- 物料绑定弹窗 -->
<div id="js-bind-popup" style="display:none; padding:20px;">
    <form class="layui-form">
        <input type="hidden" id="bind-location-id">
        <div class="layui-form-item">
            <label class="layui-form-label">库位编码</label>
            <div class="layui-input-block">
                <input type="text" id="bind-location-code" class="layui-input" readonly>
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">存放物料</label>
            <div class="layui-input-block">
                <select id="bind-materiel-select" lay-search>
                    <option value="">请选择物料（空表示解绑）</option>
                </select>
            </div>
        </div>
        <div class="layui-form-item">
            <div class="layui-input-block">
                <button type="button" class="layui-btn" id="btn-save-bind">保存</button>
            </div>
        </div>
    </form>
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

        var g_currentWarehouse = null;
        var g_materielList = [];

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

        // 预加载物料列表（用于绑定弹窗）
        function loadMaterielList(callback) {
            spUtil.ajax({
                url: '${request.contextPath}/basedata/materile/page',
                type: 'POST',
                data: {size: 999, current: 1},
                success: function (res) {
                    if (res.code === 0 && res.data && res.data.records) {
                        g_materielList = res.data.records;
                    }
                    callback && callback();
                }
            });
        }
        loadMaterielList();

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

        // 库房行点击事件：加载库位和可视化
        table.on('row(js-warehouse-table-filter)', function(obj) {
            var data = obj.data;
            g_currentWarehouse = data;
            var total = data.groupCount * data.rowCount * data.layerCount * data.columnCount;
            $('#js-location-title').text('【' + data.name + '】 规格：' + data.groupCount + '组 x ' + data.rowCount + '排 x ' + data.layerCount + '层 x ' + data.columnCount + '列 = ' + total + '个库位');
            loadLocationTable(data.id);
            initMatrixSelectors(data);
            loadMatrix(data.id, 1, 1);
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
                    {field: 'columnNum', title: '列号', width: 80},
                    {field: 'materielId', title: '存放物料', width: 200, templet: function(d) {
                        if (d.materielId) {
                            var m = g_materielList.find(function(item) { return item.id === d.materielId; });
                            return m ? (m.materiel + ' - ' + m.materielDesc) : '未知物料';
                        }
                        return '<span style="color:#999">空闲</span>';
                    }}
                ]]
            });
        }

        // ==================== 可视化二维表 ====================

        // 初始化排号和层号选择器
        function initMatrixSelectors(warehouse) {
            var rowSelect = $('#js-matrix-row');
            var layerSelect = $('#js-matrix-layer');
            rowSelect.empty();
            layerSelect.empty();
            for (var r = 1; r <= warehouse.rowCount; r++) {
                rowSelect.append('<option value="' + r + '">第' + r + '排</option>');
            }
            for (var l = 1; l <= warehouse.layerCount; l++) {
                layerSelect.append('<option value="' + l + '">第' + l + '层</option>');
            }
            form.render('select');
            $('#js-matrix-title').text('【' + warehouse.name + '】 选择排号和层号查看库位分布');
        }

        // 监听排号/层号变化
        form.on('select(matrix-row-filter)', function() { refreshMatrix(); });
        form.on('select(matrix-layer-filter)', function() { refreshMatrix(); });
        $('#js-refresh-matrix').click(function() { refreshMatrix(); });

        function refreshMatrix() {
            if (!g_currentWarehouse) {
                layer.msg('请先选择库房', {icon: 0});
                return;
            }
            var row = $('#js-matrix-row').val() || 1;
            var layer = $('#js-matrix-layer').val() || 1;
            loadMatrix(g_currentWarehouse.id, parseInt(row), parseInt(layer));
        }

        // 加载二维矩阵
        function loadMatrix(warehouseId, rowNum, layerNum) {
            spUtil.ajax({
                url: '${request.contextPath}/admin/warehouse/location-matrix',
                data: {warehouseId: warehouseId, rowNum: rowNum, layerNum: layerNum},
                success: function (res) {
                    if (res.code === 0) {
                        renderMatrix(res.data, rowNum, layerNum);
                    }
                }
            });
        }

        // 渲染二维网格
        function renderMatrix(data, rowNum, layerNum) {
            var container = $('#js-matrix-grid');
            container.empty();
            if (!data || data.length === 0) {
                container.html('<div style="color:#999;padding:20px;">该排层暂无库位数据</div>');
                return;
            }
            for (var i = 0; i < data.length; i++) {
                var item = data[i];
                var occupied = item.materielId ? true : false;
                var cssClass = occupied ? 'occupied' : '';
                var statusText = occupied ? '已占用' : '空闲';
                var statusClass = occupied ? 'has-materiel' : 'empty';
                var materielText = occupied ? (item.materielCode + ' ' + (item.materielDesc || '')) : '';

                var html = '<div class="matrix-cell ' + cssClass + '" data-id="' + item.id + '" data-code="' + item.code + '" data-materiel="' + (item.materielId || '') + '">' +
                    '<div class="cell-code">' + item.code + '</div>' +
                    '<div class="cell-status ' + statusClass + '">' + statusText + '</div>' +
                    (occupied ? '<div class="cell-materiel" title="' + materielText + '">' + materielText + '</div>' : '') +
                    '</div>';
                container.append(html);
            }

            // 绑定点击事件
            $('.matrix-cell').click(function() {
                var locationId = $(this).data('id');
                var locationCode = $(this).data('code');
                var currentMaterielId = $(this).data('materiel');
                openBindPopup(locationId, locationCode, currentMaterielId);
            });
        }

        // 打开物料绑定弹窗
        function openBindPopup(locationId, locationCode, currentMaterielId) {
            $('#bind-location-id').val(locationId);
            $('#bind-location-code').val(locationCode);

            var select = $('#bind-materiel-select');
            select.empty();
            select.append('<option value="">请选择物料（空表示解绑）</option>');
            for (var i = 0; i < g_materielList.length; i++) {
                var m = g_materielList[i];
                var selected = (m.id === currentMaterielId) ? 'selected' : '';
                select.append('<option value="' + m.id + '" ' + selected + '>' + m.materiel + ' - ' + m.materielDesc + '</option>');
            }
            form.render('select');

            layer.open({
                type: 1,
                title: '库位物料绑定',
                area: ['500px', '300px'],
                content: $('#js-bind-popup'),
                success: function() {
                    form.render();
                }
            });
        }

        // 保存绑定
        $('#btn-save-bind').click(function() {
            var locationId = $('#bind-location-id').val();
            var materielId = $('#bind-materiel-select').val();
            spUtil.ajax({
                url: '${request.contextPath}/admin/warehouse/bind-materiel',
                type: 'POST',
                data: {locationId: locationId, materielId: materielId},
                success: function(res) {
                    if (res.code === 0) {
                        layer.msg('保存成功', {icon: 1});
                        layer.closeAll();
                        refreshMatrix();
                        if (g_currentWarehouse) {
                            loadLocationTable(g_currentWarehouse.id);
                        }
                    } else {
                        layer.msg(res.msg || '保存失败', {icon: 2});
                    }
                }
            });
        });
    });
</script>
</body>
</html>
