<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>授权菜单</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0">
    <#include "${request.contextPath}/common/common.ftl">
</head>
<body>
<div class="splayui-container">
    <div class="splayui-main">
        <div id="js-menu-tree" class="demo-tree-more" style="padding: 10px;"></div>
        <div class="layui-form-item layui-hide">
            <div class="layui-input-block">
                <input id="js-role-id" value="${roleId}"/>
                <button id="js-submit" class="layui-btn" lay-submit lay-filter="js-submit-filter">确定</button>
            </div>
        </div>
    </div>
</div>
<script>
    layui.use(['tree', 'layer'], function () {
        var tree = layui.tree,
            layer = layui.layer;

        var roleId = $('#js-role-id').val();

        // 加载菜单树数据
        $.ajax({
            url: '${request.contextPath}/admin/sys/role/menu-tree',
            type: 'GET',
            data: {roleId: roleId},
            success: function (res) {
                if (res.code === 0) {
                    // 转换数据格式：name -> title（适配layui tree组件）
                    var treeData = convertToTreeData(res.data);
                    // 渲染树形菜单
                    tree.render({
                        elem: '#js-menu-tree',
                        data: treeData,
                        showCheckbox: true,
                        id: 'menuTree',
                        isJump: false
                    });
                } else {
                    layer.msg('菜单加载失败：' + (res.msg || '未知错误'));
                }
            },
            error: function () {
                layer.msg('菜单加载失败，请检查网络');
            }
        });

        // 将后端TreeVO数据转换为layui tree所需格式
        function convertToTreeData(nodes) {
            if (!nodes || nodes.length === 0) {
                return [];
            }
            var result = [];
            for (var i = 0; i < nodes.length; i++) {
                var node = nodes[i];
                var item = {
                    id: node.id,
                    title: node.name,
                    checked: node.checked || false,
                    spread: true,
                    children: convertToTreeData(node.children)
                };
                result.push(item);
            }
            return result;
        }

        // 提交按钮点击事件（由父页面触发）
        $('#js-submit').on('click', function () {
            // 获取选中的节点（包含半选状态）
            var checkedData = tree.getChecked('menuTree');
            var menuIds = [];
            // 递归收集所有选中节点的ID
            function collectIds(nodes) {
                for (var i = 0; i < nodes.length; i++) {
                    menuIds.push(nodes[i].id);
                    if (nodes[i].children && nodes[i].children.length > 0) {
                        collectIds(nodes[i].children);
                    }
                }
            }
            collectIds(checkedData);

            // 提交保存（同步请求确保spLayer能获取结果）
            $.ajax({
                url: '${request.contextPath}/admin/sys/role/save-auth-menu',
                type: 'POST',
                data: {
                    roleId: roleId,
                    menuIds: menuIds
                },
                traditional: true,
                async: false,
                success: function (res) {
                    window.spChildFrameResult = res;
                    if (res.code === 0) {
                        layer.msg('授权成功', {icon: 1, time: 1500});
                    } else {
                        layer.msg('授权失败：' + (res.msg || '未知错误'));
                    }
                },
                error: function () {
                    window.spChildFrameResult = {code: 1, msg: '网络错误'};
                    layer.msg('授权失败，请检查网络');
                }
            });
        });
    });
</script>
</body>
</html>