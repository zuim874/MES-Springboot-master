<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>授权菜单</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0">
    <#include "${request.contextPath}/common/common.ftl">
    <link rel="stylesheet" href="${request.contextPath}/static/js/layuimodule/treeTable/treeTable.css" media="all">
</head>
<body>
<div class="splayui-container">
    <div class="splayui-main">
        <div id="js-menu-tree" class="demo-tree-more"></div>
        <div class="layui-form-item layui-hide">
            <div class="layui-input-block">
                <input id="js-role-id" value="${roleId}"/>
                <button id="js-submit" class="layui-btn" lay-submit lay-filter="js-submit-filter">确定</button>
            </div>
        </div>
    </div>
</div>
<script src="${request.contextPath}/static/js/layuimodule/treeTable/treeTable.js"></script>
<script>
    layui.use(['tree', 'util'], function () {
        var tree = layui.tree,
            util = layui.util;

        var roleId = $('#js-role-id').val();

        // 加载菜单树数据
        $.ajax({
            url: '${request.contextPath}/admin/sys/role/menu-tree',
            type: 'GET',
            data: {roleId: roleId},
            success: function (res) {
                if (res.code === 0) {
                    // 渲染树形菜单
                    tree.render({
                        elem: '#js-menu-tree',
                        data: res.data,
                        showCheckbox: true,
                        id: 'menuTree',
                        isJump: false,
                        click: function (obj) {
                            // 点击节点事件
                        }
                    });
                } else {
                    layer.msg('菜单加载失败');
                }
            },
            error: function () {
                layer.msg('菜单加载失败');
            }
        });

        // 提交按钮点击事件（由父页面触发）
        $('#js-submit').on('click', function () {
            // 获取选中的节点
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

            // 提交保存
            $.ajax({
                url: '${request.contextPath}/admin/sys/role/save-auth-menu',
                type: 'POST',
                data: {
                    roleId: roleId,
                    menuIds: menuIds
                },
                traditional: true,
                success: function (res) {
                    if (res.code === 0) {
                        layer.msg('授权成功', {icon: 1});
                        // 关闭弹窗
                        var index = parent.layer.getFrameIndex(window.name);
                        parent.layer.close(index);
                    } else {
                        layer.msg('授权失败：' + res.msg);
                    }
                },
                error: function () {
                    layer.msg('授权失败');
                }
            });
        });
    });
</script>
</body>
</html>
