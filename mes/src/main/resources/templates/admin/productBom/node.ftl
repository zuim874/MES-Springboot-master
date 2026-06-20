<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>BOM节点管理</title>
    <#include "${request.contextPath}/common/common.ftl">
    <style>
        .bom-header { padding: 12px 15px; background: #f2f2f2; margin-bottom: 10px; display: flex; align-items: center; justify-content: space-between; }
        .bom-header .bom-title { font-size: 15px; }
        .bom-header .bom-title strong { margin-right: 10px; }
        .node-type-tag { display: inline-block; padding: 0 6px; border-radius: 2px; font-size: 12px; line-height: 20px; }
        .node-type-0 { background: #009688; color: #fff; }
        .node-type-1 { background: #5FB878; color: #fff; }
        .node-type-2 { background: #1E9FFF; color: #fff; }
        .node-action-btn { margin: 0 2px; }
    </style>
</head>
<body>
<div class="splayui-container">
    <div class="bom-header">
        <div class="bom-title">
            <strong>BOM名称：</strong>${bomName}
            <#if isFrozen?? && isFrozen == '1'>
                <span class="layui-btn layui-btn-xs layui-btn-danger">已定版</span>
            <#else>
                <span class="layui-btn layui-btn-xs">未定版</span>
            </#if>
        </div>
    </div>

    <table id="js-node-table" class="layui-table" lay-filter="js-node-table"></table>
</div>

<!-- 节点表单弹窗 -->
<div id="js-node-form-popup" style="display:none; padding: 20px;">
    <form class="layui-form" lay-filter="js-node-form-filter">
        <input type="hidden" name="id" id="node-id">
        <input type="hidden" name="bomId" value="${bomId}">
        <input type="hidden" name="parentId" id="node-parent-id">
        <input type="hidden" name="nodeLevel" id="node-level">
        <input type="hidden" name="nodeType" id="node-type">

        <div class="layui-form-item">
            <label class="layui-form-label">节点类型</label>
            <div class="layui-input-block">
                <input type="text" id="node-type-display" class="layui-input" disabled>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label sp-required">选择关联</label>
            <div class="layui-input-block">
                <div style="display: flex; gap: 5px;">
                    <input type="text" name="refCode" id="ref-code-input" autocomplete="off" class="layui-input" placeholder="请点击右侧按钮选择" style="flex: 1;" readonly>
                    <button type="button" class="layui-btn layui-btn-sm" id="btn-select-ref">选择</button>
                </div>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">节点名称</label>
            <div class="layui-input-block">
                <input type="text" name="nodeName" id="node-name-input" autocomplete="off" class="layui-input" placeholder="选择后自动填充" readonly>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">数量</label>
            <div class="layui-input-block">
                <input type="number" name="qty" value="1" min="0.01" step="0.01" class="layui-input">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">备注</label>
            <div class="layui-input-block">
                <textarea name="remark" class="layui-textarea" rows="2"></textarea>
            </div>
        </div>

        <div class="layui-form-item" style="text-align: center;">
            <button class="layui-btn" id="btn-save-form">保存</button>
            <button type="button" class="layui-btn layui-btn-primary" id="btn-cancel-form">取消</button>
        </div>
    </form>
</div>

<!-- 选择零部件/物料弹窗 -->
<div id="js-select-popup" style="display:none; padding: 10px;">
    <div class="layui-form" style="margin-bottom: 10px;">
        <div class="layui-inline">
            <input type="text" id="js-select-search" placeholder="输入名称搜索..." autocomplete="off" class="layui-input" style="width: 200px;">
        </div>
        <div class="layui-inline">
            <button class="layui-btn layui-btn-sm" id="js-select-search-btn"><i class="layui-icon">&#xe615;</i>搜索</button>
        </div>
    </div>
    <table id="js-select-table" lay-filter="js-select-table"></table>
</div>

<script>
    layui.use(['treeTable', 'table', 'form', 'layer'], function () {
        var treeTable = layui.treeTable,
            table = layui.table,
            form = layui.form,
            layer = layui.layer;

        var bomId = '${bomId}';
        var isFrozen = '${isFrozen!}' === '1';
        var g_selectedRef = null; // 全局变量，存储用户选择的零部件/物料数据

        // 渲染树形表格
        var insTb = treeTable.render({
            elem: '#js-node-table',
            tree: {
                iconIndex: 0,
                idName: 'id',
                pidName: 'parentId',
                isPidData: true,
                onlyIconControl: false,
                getIcon: function (d) {
                    if (d.nodeType === '0') {
                        return '<i class="layui-icon layui-icon-auz" style="color:#009688;"></i>';
                    } else if (d.nodeType === '1') {
                        return '<i class="layui-icon layui-icon-component" style="color:#5FB878;"></i>';
                    } else {
                        return '<i class="layui-icon layui-icon-file" style="color:#1E9FFF;"></i>';
                    }
                }
            },
            cols: [
                {field: 'nodeName', title: '节点名称', width: 220},
                {field: 'nodeLevel', title: '节点层级', width: 100},
                {field: 'nodeCode', title: '节点编号', width: 140},
                {
                    field: 'nodeType', title: '节点类型', width: 100, templet: function (d) {
                        var typeMap = {'0': '产品', '1': '零部件', '2': '物料'};
                        var clsMap = {'0': 'node-type-0', '1': 'node-type-1', '2': 'node-type-2'};
                        return '<span class="node-type-tag ' + (clsMap[d.nodeType] || '') + '">' + (typeMap[d.nodeType] || '') + '</span>';
                    }
                },
                {field: 'qty', title: '数量', width: 80},
                {field: 'remark', title: '备注'},
                {field: 'updateTime', title: '更新时间', width: 160},
                {
                    field: 'nodeType', title: '操作', width: 280, align: 'center', templet: function (d) {
                        if (isFrozen) {
                            return '<span class="layui-btn layui-btn-xs layui-btn-disabled">已定版</span>';
                        }
                        var html = '';
                        if (d.nodeType === '0') {
                            html += '<a class="layui-btn layui-btn-xs layui-btn-normal node-action-btn" data-action="addChild" data-id="' + d.id + '" data-level="' + d.nodeLevel + '" data-ntype="' + d.nodeType + '">添加零部件</a>';
                        } else if (d.nodeType === '1') {
                            html += '<a class="layui-btn layui-btn-xs layui-btn-normal node-action-btn" data-action="addChild" data-id="' + d.id + '" data-level="' + d.nodeLevel + '" data-ntype="' + d.nodeType + '">添加物料</a>';
                            html += '<a class="layui-btn layui-btn-xs node-action-btn" data-action="edit" data-id="' + d.id + '" data-ntype="' + d.nodeType + '">编辑</a>';
                            html += '<a class="layui-btn layui-btn-danger layui-btn-xs node-action-btn" data-action="delete" data-id="' + d.id + '" data-name="' + (d.nodeName || '').replace(/"/g, '&quot;') + '" data-level="' + d.nodeLevel + '">删除</a>';
                        } else if (d.nodeType === '2') {
                            html += '<a class="layui-btn layui-btn-xs node-action-btn" data-action="edit" data-id="' + d.id + '" data-ntype="' + d.nodeType + '">编辑</a>';
                            html += '<a class="layui-btn layui-btn-danger layui-btn-xs node-action-btn" data-action="delete" data-id="' + d.id + '" data-name="' + (d.nodeName || '').replace(/"/g, '&quot;') + '" data-level="' + d.nodeLevel + '">删除</a>';
                        }
                        return html;
                    }
                }
            ],
            reqData: function (data, callback) {
                spUtil.ajax({
                    url: '${request.contextPath}/admin/productBom/node/list',
                    type: 'POST',
                    data: {bomId: bomId},
                    success: function (res) {
                        if (res.code === 0) {
                            callback(res.data || []);
                        } else {
                            layer.msg(res.msg, {icon: 2});
                            callback([]);
                        }
                    }
                });
            },
            style: 'margin-top:0;'
        });

        // ========== 事件委托：所有操作按钮点击 ==========
        $(document).on('click', '.node-action-btn', function () {
            var $btn = $(this);
            var action = $btn.data('action');
            var id = $btn.data('id');
            var nodeType = $btn.attr('data-ntype') || '';
            var nodeLevel = parseInt($btn.data('level')) || 0;
            var nodeName = $btn.data('name');

            switch (action) {
                case 'addChild':
                    if (nodeType === '0') {
                        // 根节点(产品)下：添加零部件
                        openNodeFormForAdd(id, nodeLevel, '1');
                    } else if (nodeType === '1') {
                        // 零部件下：添加物料
                        openNodeFormForAdd(id, nodeLevel, '2');
                    } else {
                        layer.msg('当前节点类型不支持添加子节点', {icon: 2});
                    }
                    break;
                case 'edit':
                    editNodeById(id);
                    break;
                case 'delete':
                    if (nodeLevel === 0) {
                        layer.msg('不能删除根节点', {icon: 2});
                        return;
                    }
                    layer.confirm('确定删除【' + (nodeName || '该节点') + '】吗？其子节点也会被删除', function (index) {
                        spUtil.ajax({
                            url: '${request.contextPath}/admin/productBom/node/delete',
                            type: 'POST',
                            data: {id: id},
                            success: function (res) {
                                layer.close(index);
                                if (res.code === 0) {
                                    layer.msg('删除成功', {icon: 1});
                                    insTb.refresh();
                                } else {
                                    layer.msg(res.msg, {icon: 2});
                                }
                            }
                        });
                    });
                    break;
            }
        });

        // ========== 新增节点：打开表单 ==========
        function openNodeFormForAdd(parentId, parentLevel, childNodeType) {
            childNodeType = childNodeType || '1';
            g_selectedRef = null; // 重置选择数据
            // 重置表单
            form.val('js-node-form-filter', {
                id: '',
                parentId: parentId || '',
                nodeLevel: (parentLevel || 0) + 1,
                nodeType: childNodeType,
                nodeName: '',
                refCode: '',
                qty: 1,
                remark: ''
            });
            // 直接设置 hidden input 值（layui form.val 可能不处理 hidden 元素）
            document.getElementById('node-id').value = '';
            document.getElementById('node-parent-id').value = parentId || '';
            document.getElementById('node-level').value = (parentLevel || 0) + 1;
            document.getElementById('node-type').value = childNodeType;
            document.getElementById('node-type-display').value = {'0': '产品', '1': '零部件', '2': '物料'}[childNodeType] || '';
            document.getElementById('node-name-input').value = '';
            document.getElementById('node-name-input').readOnly = true;
            document.getElementById('ref-code-input').value = '';

            layer.open({
                type: 1,
                title: '新增节点',
                area: ['500px', '300px'],
                content: $('#js-node-form-popup')
            });
        }

        // ========== 编辑节点：AJAX获取完整数据 ==========
        function editNodeById(id) {
            spUtil.ajax({
                url: '${request.contextPath}/admin/productBom/node/get',
                type: 'POST',
                data: {id: id},
                success: function (res) {
                    if (res.code === 0 && res.data) {
                        var d = res.data;
                        form.val('js-node-form-filter', {
                            id: d.id,
                            parentId: d.parentId || '',
                            nodeLevel: d.nodeLevel,
                            nodeType: d.nodeType,
                            nodeName: d.nodeName,
                            refCode: d.refCode || '',
                            qty: d.qty || 1,
                            remark: d.remark || ''
                        });
                        // 直接设置 hidden input 值
                        document.getElementById('node-id').value = d.id || '';
                        document.getElementById('node-parent-id').value = d.parentId || '';
                        document.getElementById('node-level').value = d.nodeLevel || '';
                        document.getElementById('node-type').value = d.nodeType || '';
                        document.getElementById('node-type-display').value = {'0': '产品', '1': '零部件', '2': '物料'}[d.nodeType] || '';
                        document.getElementById('node-name-input').readOnly = false;
                        layer.open({
                            type: 1,
                            title: '编辑节点',
                            area: ['500px', '300px'],
                            content: $('#js-node-form-popup')
                        });
                    } else {
                        layer.msg(res.msg || '获取节点数据失败', {icon: 2});
                    }
                }
            });
        }

        // ========== 取消按钮 ==========
        $(document).on('click', '#btn-cancel-form', function () {
            layer.closeAll();
        });

        // ========== "选择"按钮：打开零部件/物料选择弹窗 ==========
        $('#btn-select-ref').on('click', function () {
            var nodeType = document.getElementById('node-type').value;
            if (!nodeType) {
                layer.msg('无法确定节点类型', {icon: 2});
                return;
            }
            openSelectPopup(nodeType);
        });

        function openSelectPopup(nodeType) {
            var isPart = (nodeType === '1');
            var title = isPart ? '选择零部件' : '选择物料';
            var url = isPart
                ? '${request.contextPath}/admin/part/page'
                : '${request.contextPath}/basedata/materile/page';
            var cols = isPart ? [[
                {field: 'code', title: '零部件编号', width: 160, event: 'selectRow'},
                {field: 'name', title: '零部件名称', width: 200, event: 'selectRow'},
                {field: 'remark', title: '备注', event: 'selectRow'}
            ]] : [[
                {field: 'materiel', title: '物料编码', width: 160, event: 'selectRow'},
                {field: 'materielDesc', title: '物料名称', width: 200, event: 'selectRow'},
                {field: 'remark', title: '备注', event: 'selectRow'}
            ]];

            // 动态创建弹窗内容
            var popupHtml = '<div style="padding:10px;">'
                + '<div class="layui-form" style="margin-bottom:10px;">'
                + '<div class="layui-inline"><input type="text" id="js-select-search-dynamic" placeholder="输入名称搜索..." autocomplete="off" class="layui-input" style="width:200px;"></div>'
                + '<div class="layui-inline"><button class="layui-btn layui-btn-sm" id="js-select-search-btn-dynamic"><i class="layui-icon">&#xe615;</i>搜索</button></div>'
                + '</div>'
                + '<table id="js-select-table-dynamic" lay-filter="js-select-table-dynamic"></table>'
                + '</div>';

            var selectIndex = layer.open({
                type: 1,
                title: title,
                area: ['700px', '480px'],
                content: popupHtml,
                success: function (layero, index) {
                    var selectTableIns = table.render({
                        elem: '#js-select-table-dynamic',
                        url: url,
                        method: 'POST',
                        page: true,
                        parseData: function (res) {
                            return {
                                code: res.code,
                                msg: res.msg,
                                count: res.data ? res.data.total : 0,
                                data: res.data ? res.data.records : []
                            };
                        },
                        cols: cols,
                        done: function () {
                            // 表格渲染完成后，绑定行点击事件
                            layero.find('.layui-table-body table tbody tr').off('click').on('click', function () {
                                var tableData = table.cache['js-select-table-dynamic'];
                                if (!tableData || tableData.length === 0) return;
                                var rowIndex = $(this).data('index');
                                if (rowIndex === undefined) return;
                                var data = tableData[rowIndex];
                                if (!data) return;
                                if (isPart) {
                                    g_selectedRef = {refCode: data.code || '', nodeName: data.name || ''};
                                    $('#ref-code-input').val(data.code || '');
                                    $('#node-name-input').val(data.name || '');
                                } else {
                                    g_selectedRef = {refCode: data.materiel || '', nodeName: data.materielDesc || ''};
                                    $('#ref-code-input').val(data.materiel || '');
                                    $('#node-name-input').val(data.materielDesc || '');
                                }
                                layer.close(selectIndex);
                            });
                        }
                    });

                    // 搜索按钮
                    $('#js-select-search-btn-dynamic').on('click', function () {
                        var keyword = $('#js-select-search-dynamic').val();
                        selectTableIns.reload({
                            where: isPart ? {name: keyword} : {materielDescLike: keyword},
                            page: {curr: 1}
                        });
                    });

                    $('#js-select-search-dynamic').on('keydown', function (e) {
                        if (e.keyCode === 13) {
                            $('#js-select-search-btn-dynamic').click();
                        }
                    });
                }
            });
        }

        // ========== 保存按钮：直接读取DOM值 + 全局变量，绕过layui form缓存 ==========
        $(document).on('click', '#btn-save-form', function () {
            var id = $('#node-id').val() || '';
            var parentId = $('#node-parent-id').val() || '';
            var nodeLevel = $('#node-level').val() || '';
            var nodeType = $('#node-type').val() || '';
            var refCode = $('#ref-code-input').val() || '';
            var nodeName = $('#node-name-input').val() || '';
            var qty = $('[name="qty"]').val() || '1';
            var remark = $('[name="remark"]').val() || '';

            // 校验：必须有选择的数据
            if (!refCode && !nodeName && !g_selectedRef) {
                layer.msg('请先选择零部件或物料', {icon: 2});
                return;
            }
            // 优先使用全局变量
            if (g_selectedRef) {
                refCode = g_selectedRef.refCode;
                nodeName = g_selectedRef.nodeName;
            }
            if (!nodeName && refCode) {
                nodeName = refCode;
            }

            var postData = {
                id: id,
                bomId: bomId,
                parentId: parentId,
                nodeLevel: nodeLevel,
                nodeType: nodeType,
                refCode: refCode,
                nodeName: nodeName,
                qty: qty,
                remark: remark
            };

            spUtil.ajax({
                url: '${request.contextPath}/admin/productBom/node/save',
                type: 'POST',
                data: postData,
                success: function (res) {
                    if (res.code === 0) {
                        layer.msg('保存成功', {icon: 1, time: 800}, function () {
                            layer.closeAll();
                            insTb.refresh();
                        });
                    } else {
                        layer.msg(res.msg, {icon: 2});
                    }
                }
            });
        });
    });
</script>
</body>
</html>