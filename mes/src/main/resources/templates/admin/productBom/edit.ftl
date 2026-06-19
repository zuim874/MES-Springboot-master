<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>BOM编制</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <#include "${request.contextPath}/common/common.ftl">
    <style>
        .bom-info {
            background: #f8f8f8;
            padding: 15px;
            margin-bottom: 15px;
            border-left: 4px solid #009688;
        }
        .bom-info-item {
            display: inline-block;
            margin-right: 40px;
            font-size: 14px;
        }
        .bom-info-item strong {
            color: #333;
        }
        .tree-box {
            border: 1px solid #e6e6e6;
            padding: 15px;
            min-height: 350px;
            background: #fff;
        }
        .layui-tree-txt {
            font-size: 14px;
        }
    </style>
</head>
<body>
<div class="layui-fluid" style="padding: 15px;">
    <div class="bom-info">
        <div class="bom-info-item"><strong>BOM编码：</strong>${bom.bomCode}</div>
        <div class="bom-info-item"><strong>产品物料：</strong>${bom.productMaterielDesc}（${bom.productMaterielCode}）</div>
        <div class="bom-info-item"><strong>版本：</strong>${bom.version}</div>
        <div class="bom-info-item"><strong>状态：</strong><span id="bomStateText">${(bom.state == '1')?string('<span style="color:green;">已定版</span>', '<span style="color:orange;">草稿</span>')}</span></div>
    </div>

    <div class="layui-btn-container" id="operateArea">
        <button class="layui-btn layui-btn-sm" id="btnAdd"><i class="layui-icon">&#xe61f;</i>添加物料</button>
        <button class="layui-btn layui-btn-sm layui-btn-danger" id="btnDelete"><i class="layui-icon">&#xe640;</i>删除物料</button>
        <button class="layui-btn layui-btn-sm layui-btn-warm" id="btnLock"><i class="layui-icon">&#xe673;</i>锁定BOM结构</button>
        <button class="layui-btn layui-btn-sm layui-btn-primary" id="btnRefresh"><i class="layui-icon">&#xe669;</i>刷新</button>
    </div>

    <div class="tree-box">
        <div id="bomTree"></div>
        <div id="emptyTip" style="text-align:center; color:#999; padding-top:50px; display:none;">暂无BOM子项，请点击"添加物料"按钮添加</div>
    </div>
</div>

<script>
    layui.use(['tree', 'layer', 'table', 'form'], function () {
        var tree = layui.tree;
        var layer = layui.layer;
        var table = layui.table;
        var form = layui.form;

        var bomId = '${bom.id}';
        var bomState = '${bom.state}';
        var selectedNodeId = null;

        // 如果已定版，隐藏操作按钮
        if (bomState === '1') {
            document.getElementById('operateArea').style.display = 'none';
        }

        // 加载树
        function loadTree() {
            spUtil.ajax({
                url: '${request.contextPath}/admin/productBom/item/tree',
                type: 'GET',
                data: {bomId: bomId},
                success: function (res) {
                    if (res.code === 0) {
                        var data = res.data || [];
                        if (data.length === 0) {
                            document.getElementById('bomTree').innerHTML = '';
                            document.getElementById('emptyTip').style.display = 'block';
                        } else {
                            document.getElementById('emptyTip').style.display = 'none';
                            var treeData = convertToTreeData(data);
                            tree.render({
                                elem: '#bomTree',
                                data: treeData,
                                click: function (obj) {
                                    selectedNodeId = obj.data.id;
                                }
                            });
                        }
                    }
                }
            });
        }

        function convertToTreeData(list) {
            var result = [];
            for (var i = 0; i < list.length; i++) {
                var item = list[i];
                var title = '<strong>' + item.materielCode + '</strong> - ' + item.materielDesc;
                if (item.itemNum !== null && item.itemNum !== undefined) {
                    title += ' <span style="color:#999;">（用量: ' + item.itemNum;
                    if (item.itemUnit) title += ' ' + item.itemUnit;
                    title += '）</span>';
                }
                if (item.remark) {
                    title += ' <span style="color:#666;">[' + item.remark + ']</span>';
                }
                var node = {
                    title: title,
                    id: item.id,
                    children: convertToTreeData(item.children || [])
                };
                result.push(node);
            }
            return result;
        }

        loadTree();

        // 添加物料
        document.getElementById('btnAdd').addEventListener('click', function () {
            var selectedMateriel = null;
            var contentHtml = '<div style="padding:15px;">' +
                '<div class="layui-form">' +
                '  <div class="layui-form-item">' +
                '    <div class="layui-inline">' +
                '      <input type="text" id="searchKeyword" placeholder="物料编码/名称" class="layui-input" style="width:200px;">' +
                '    </div>' +
                '    <div class="layui-inline">' +
                '      <button class="layui-btn layui-btn-sm" id="btnSearchMateriel">查询</button>' +
                '    </div>' +
                '  </div>' +
                '</div>' +
                '<table id="materielTable" lay-filter="materielTable"></table>' +
                '<div class="layui-form" style="margin-top:15px;">' +
                '  <div class="layui-form-item">' +
                '    <label class="layui-form-label">用量</label>' +
                '    <div class="layui-input-inline">' +
                '      <input type="number" id="itemNum" value="1" step="0.01" class="layui-input">' +
                '    </div>' +
                '  </div>' +
                '  <div class="layui-form-item">' +
                '    <label class="layui-form-label">单位</label>' +
                '    <div class="layui-input-inline">' +
                '      <input type="text" id="itemUnit" class="layui-input" readonly>' +
                '    </div>' +
                '  </div>' +
                '  <div class="layui-form-item">' +
                '    <label class="layui-form-label">备注</label>' +
                '    <div class="layui-input-inline">' +
                '      <input type="text" id="itemRemark" class="layui-input">' +
                '    </div>' +
                '  </div>' +
                '</div>' +
                '</div>';

            layer.open({
                type: 1,
                title: '添加物料' + (selectedNodeId ? '（作为子项）' : '（作为根级子项）'),
                area: ['750px', '600px'],
                content: contentHtml,
                btn: ['确定', '取消'],
                success: function () {
                    renderMaterielTable();
                    document.getElementById('btnSearchMateriel').addEventListener('click', function () {
                        renderMaterielTable();
                    });
                },
                yes: function (index) {
                    if (!selectedMateriel) {
                        layer.msg('请选择物料');
                        return;
                    }
                    var itemNum = document.getElementById('itemNum').value;
                    var itemUnit = document.getElementById('itemUnit').value;
                    var itemRemark = document.getElementById('itemRemark').value;

                    spUtil.ajax({
                        url: '${request.contextPath}/admin/productBom/item/save',
                        type: 'POST',
                        data: {
                            bomId: bomId,
                            parentId: selectedNodeId || '',
                            materielCode: selectedMateriel.materiel,
                            materielDesc: selectedMateriel.materielDesc,
                            materielType: selectedMateriel.matType || '',
                            itemNum: itemNum,
                            itemUnit: itemUnit,
                            remark: itemRemark
                        },
                        success: function (res) {
                            if (res.code === 0) {
                                layer.close(index);
                                layer.msg('添加成功');
                                loadTree();
                            } else {
                                layer.msg(res.msg);
                            }
                        }
                    });
                }
            });

            function renderMaterielTable() {
                var keyword = document.getElementById('searchKeyword').value;
                table.render({
                    elem: '#materielTable',
                    url: '${request.contextPath}/admin/productBom/materiel/list',
                    where: {keyword: keyword},
                    page: false,
                    limit: 50,
                    cols: [[
                        {type: 'radio'},
                        {field: 'materiel', title: '物料编码', width: 120},
                        {field: 'materielDesc', title: '物料名称', width: 180},
                        {field: 'matType', title: '物料类型', width: 100},
                        {field: 'unit', title: '单位', width: 80}
                    ]]
                });
            }

            table.on('radio(materielTable)', function (obj) {
                selectedMateriel = obj.data;
                document.getElementById('itemUnit').value = selectedMateriel.unit || '';
            });
        });

        // 删除物料
        document.getElementById('btnDelete').addEventListener('click', function () {
            if (!selectedNodeId) {
                layer.msg('请先选中要删除的节点');
                return;
            }
            layer.confirm('确认删除该节点及其所有子节点吗？', function (index) {
                spUtil.ajax({
                    url: '${request.contextPath}/admin/productBom/item/delete',
                    type: 'POST',
                    data: {id: selectedNodeId},
                    success: function (res) {
                        if (res.code === 0) {
                            layer.msg('删除成功');
                            selectedNodeId = null;
                            loadTree();
                        } else {
                            layer.msg(res.msg);
                        }
                    }
                });
                layer.close(index);
            });
        });

        // 锁定BOM
        document.getElementById('btnLock').addEventListener('click', function () {
            layer.confirm('锁定后BOM将不能编辑，确认锁定吗？', function (index) {
                spUtil.ajax({
                    url: '${request.contextPath}/admin/productBom/lock',
                    type: 'POST',
                    data: {id: bomId},
                    success: function (res) {
                        if (res.code === 0) {
                            layer.msg('BOM已锁定');
                            document.getElementById('bomStateText').innerHTML = '<span style="color:green;">已定版</span>';
                            document.getElementById('operateArea').style.display = 'none';
                            layer.close(index);
                        } else {
                            layer.msg(res.msg);
                        }
                    }
                });
            });
        });

        // 刷新
        document.getElementById('btnRefresh').addEventListener('click', function () {
            loadTree();
            layer.msg('已刷新');
        });
    });
</script>
</body>
</html>
