<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>工艺规划详情</title>
    <#include "${request.contextPath}/common/common.ftl">
    <style>
        .plan-header { padding: 12px 15px; background: #f2f2f2; margin-bottom: 10px; display: flex; align-items: center; justify-content: space-between; }
        .plan-header .plan-title { font-size: 15px; }
        .plan-header .plan-title strong { margin-right: 10px; }
        .node-type-tag { display: inline-block; padding: 0 6px; border-radius: 2px; font-size: 12px; line-height: 20px; }
        .node-type-0 { background: #009688; color: #fff; }
        .node-type-1 { background: #5FB878; color: #fff; }
        .node-type-2 { background: #1E9FFF; color: #fff; }
    </style>
</head>
<body>
<div class="splayui-container">
    <div class="plan-header">
        <div class="plan-title">
            <strong>产品：</strong>${bomName}
            <#if processPlanLocked == '1'>
                <span class="layui-btn layui-btn-xs layui-btn-danger">已锁定</span>
            <#else>
                <span class="layui-btn layui-btn-xs">未锁定</span>
            </#if>
        </div>
        <div>
            <#if processPlanLocked == '1'>
                <button class="layui-btn layui-btn-xs layui-btn-warm" id="js-btn-unlock"><i class="layui-icon layui-icon-unlock"></i>解锁</button>
            <#else>
                <button class="layui-btn layui-btn-xs layui-btn-danger" id="js-btn-lock"><i class="layui-icon layui-icon-lock"></i>锁定</button>
            </#if>
        </div>
    </div>

    <table id="js-node-table" class="layui-table" lay-filter="js-node-table"></table>
</div>

<!-- 工艺规划编辑弹窗 -->
<div id="js-plan-form-popup" style="display:none; padding: 20px;">
    <form class="layui-form" lay-filter="js-plan-form-filter">
        <input type="hidden" name="id" id="plan-id">
        <input type="hidden" name="bomId" value="${bomId}">
        <input type="hidden" name="bomNodeId" id="plan-bom-node-id">
        <input type="hidden" name="parentId" id="plan-parent-id">

        <div class="layui-form-item">
            <label class="layui-form-label">BOM节点</label>
            <div class="layui-input-block">
                <input type="text" id="plan-node-name" class="layui-input" disabled>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">上级工艺</label>
            <div class="layui-input-block">
                <input type="text" id="plan-parent-process" class="layui-input" disabled value="无（顶级节点）">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label sp-required">工序流程定义</label>
            <div class="layui-input-block">
                <select name="flowId" id="plan-flow-select" lay-verify="required" lay-filter="plan-flow-select" lay-search="">
                    <option value="">请选择工序流程定义</option>
                </select>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label sp-required">选择工序</label>
            <div class="layui-input-block">
                <select name="processId" id="plan-process-select" lay-verify="required" lay-search="">
                    <option value="">请选择工序</option>
                </select>
            </div>
        </div>

        <div class="layui-form-item" style="text-align: center;">
            <button class="layui-btn" id="btn-save-plan">保存</button>
            <button type="button" class="layui-btn layui-btn-primary" id="btn-cancel-plan">取消</button>
        </div>
    </form>
</div>

<script>
    layui.use(['treeTable', 'form', 'layer'], function () {
        var treeTable = layui.treeTable,
            form = layui.form,
            layer = layui.layer;

        var bomId = '${bomId}';
        var isLocked = '${processPlanLocked}' === '1';
        var g_nodes = []; // 存储所有节点数据
        var g_flowList = []; // 存储所有工序流程定义列表
        var g_flowProcessMap = {}; // 缓存 flowId -> 工序明细列表

        // 加载工序流程定义列表
        function loadFlowList(callback) {
            spUtil.ajax({
                url: '${request.contextPath}/admin/processPlan/flow-list',
                type: 'GET',
                success: function (res) {
                    if (res.code === 0 && res.data) {
                        g_flowList = res.data;
                    }
                    callback && callback();
                }
            });
        }

        // 加载指定流程定义中的工序明细
        function loadFlowProcessList(flowId, callback) {
            if (!flowId) {
                callback && callback([]);
                return;
            }
            if (g_flowProcessMap[flowId]) {
                callback && callback(g_flowProcessMap[flowId]);
                return;
            }
            spUtil.ajax({
                url: '${request.contextPath}/admin/processPlan/flow-process-list',
                type: 'GET',
                data: {flowId: flowId},
                success: function (res) {
                    if (res.code === 0 && res.data) {
                        g_flowProcessMap[flowId] = res.data;
                        callback && callback(res.data);
                    } else {
                        callback && callback([]);
                    }
                }
            });
        }

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
                {field: 'nodeName', title: '节点名称', width: 200},
                {field: 'nodeLevel', title: '层级', width: 60},
                {
                    field: 'nodeType', title: '类型', width: 80, templet: function (d) {
                        var typeMap = {'0': '产品', '1': '零部件', '2': '物料'};
                        var clsMap = {'0': 'node-type-0', '1': 'node-type-1', '2': 'node-type-2'};
                        return '<span class="node-type-tag ' + (clsMap[d.nodeType] || '') + '">' + (typeMap[d.nodeType] || '') + '</span>';
                    }
                },
                {
                    field: 'processName', title: '当前工序', width: 180, templet: function (d) {
                        return d.processName || '<span style="color:#999;">未设置</span>';
                    }
                },
                {
                    field: 'parentProcessName', title: '上级工艺', width: 180, templet: function (d) {
                        return d.parentProcessName || '<span style="color:#999;">-</span>';
                    }
                },
                {
                    title: '操作', width: 150, align: 'center', templet: function (d) {
                        if (isLocked) {
                            return '<span class="layui-btn layui-btn-xs layui-btn-disabled">已锁定</span>';
                        }
                        // 物料类型无需绑定工艺
                        if (d.nodeType === '2') {
                            return '<span class="layui-btn layui-btn-xs layui-btn-disabled">无需工艺</span>';
                        }
                        return '<a class="layui-btn layui-btn-xs layui-btn-normal" data-action="editPlan" data-id="' + d.id + '" data-name="' + (d.nodeName || '').replace(/"/g, '&quot;') + '" data-parentid="' + (d.parentId || '') + '">编辑工艺规划</a>';
                    }
                }
            ],
            reqData: function (data, callback) {
                $.ajax({
                    url: '${request.contextPath}/admin/processPlan/node-list',
                    type: 'POST',
                    data: {bomId: bomId},
                    dataType: 'json',
                    success: function (res) {
                        if (res.code === 0) {
                            g_nodes = res.data || [];
                            callback(g_nodes);
                        } else {
                            layer.msg(res.msg || '获取BOM数据失败', {icon: 2});
                            callback([]);
                        }
                    },
                    error: function () {
                        layer.msg('获取BOM数据失败，请检查网络', {icon: 2});
                        callback([]);
                    }
                });
            }
        });

        // 获取节点的工艺规划信息
        function getNodePlan(item) {
            for (var i = 0; i < g_nodes.length; i++) {
                if (g_nodes[i].id === item.id) {
                    return g_nodes[i];
                }
            }
            return null;
        }

        // 获取上级工艺名称
        function getParentProcessName(parentId) {
            if (!parentId) return '无（顶级节点）';
            for (var i = 0; i < g_nodes.length; i++) {
                if (g_nodes[i].id === parentId) {
                    return g_nodes[i].processName || '未设置';
                }
            }
            return '未设置';
        }

        // 填充工序流程定义下拉框
        function fillFlowSelect(selectedFlowId, callback) {
            var select = $('#plan-flow-select');
            select.empty();
            select.append('<option value="">请选择工序流程定义</option>');
            for (var i = 0; i < g_flowList.length; i++) {
                var f = g_flowList[i];
                var selected = (f.id === selectedFlowId) ? 'selected' : '';
                select.append('<option value="' + f.id + '" ' + selected + '>' + f.code + ' - ' + f.name + '</option>');
            }
            form.render('select');
            if (selectedFlowId) {
                loadFlowProcessList(selectedFlowId, function (processList) {
                    callback && callback(processList);
                });
            } else {
                callback && callback([]);
            }
        }

        // 填充工序下拉框
        function fillProcessSelect(processList, selectedProcessId) {
            var select = $('#plan-process-select');
            select.empty();
            select.append('<option value="">请选择工序</option>');
            for (var i = 0; i < processList.length; i++) {
                var p = processList[i];
                var selected = (p.processId === selectedProcessId) ? 'selected' : '';
                select.append('<option value="' + p.processId + '" ' + selected + '>' + p.processCode + ' - ' + p.processName + '</option>');
            }
            form.render('select');
        }

        // 监听工序流程定义下拉框变化
        form.on('select(plan-flow-select)', function (data) {
            var flowId = data.value;
            loadFlowProcessList(flowId, function (processList) {
                fillProcessSelect(processList, '');
            });
        });

        // 监听工艺规划编辑按钮
        $(document).on('click', '[data-action="editPlan"]', function () {
            var nodeId = $(this).data('id');
            var nodeName = $(this).data('name');
            var parentId = $(this).data('parentid');

            var nodePlan = getNodePlan({id: nodeId});
            var parentProcessName = getParentProcessName(parentId);

            // 填充表单
            $('#plan-id').val(nodePlan ? (nodePlan.planId || '') : '');
            $('#plan-bom-node-id').val(nodeId);
            $('#plan-parent-id').val('');
            $('#plan-node-name').val(nodeName);
            $('#plan-parent-process').val(parentProcessName);

            var selectedFlowId = nodePlan ? nodePlan.flowId : '';
            var selectedProcessId = nodePlan ? nodePlan.processId : '';

            fillFlowSelect(selectedFlowId, function (processList) {
                fillProcessSelect(processList, selectedProcessId);
            });

            layer.open({
                type: 1,
                title: '编辑工艺规划',
                area: ['520px', '420px'],
                content: $('#js-plan-form-popup'),
                success: function (layero, index) {
                    form.render();
                }
            });
        });

        // 保存工艺规划
        $('#btn-save-plan').click(function () {
            var formData = {
                id: $('#plan-id').val(),
                bomId: bomId,
                bomNodeId: $('#plan-bom-node-id').val(),
                parentId: $('#plan-parent-id').val(),
                flowId: $('#plan-flow-select').val(),
                processId: $('#plan-process-select').val()
            };

            if (!formData.flowId) {
                layer.msg('请选择工序流程定义', {icon: 2});
                return;
            }
            if (!formData.processId) {
                layer.msg('请选择工序', {icon: 2});
                return;
            }

            spUtil.ajax({
                url: '${request.contextPath}/admin/processPlan/save',
                type: 'POST',
                data: formData,
                success: function (res) {
                    if (res.code === 0) {
                        layer.msg('保存成功', {icon: 1, time: 1000}, function () {
                            layer.closeAll();
                            insTb.reload();
                        });
                    } else {
                        layer.msg(res.msg, {icon: 2});
                    }
                }
            });
        });

        $('#btn-cancel-plan').click(function () {
            layer.closeAll();
        });

        // 锁定
        $('#js-btn-lock').click(function () {
            layer.confirm('确定锁定当前产品的工艺规划吗？锁定后将无法新增、编辑、删除操作。', {icon: 3, title: '确认锁定'}, function (index) {
                spUtil.ajax({
                    url: '${request.contextPath}/admin/processPlan/lock',
                    type: 'POST',
                    data: {bomId: bomId},
                    success: function (res) {
                        layer.close(index);
                        if (res.code === 0) {
                            layer.msg('已锁定', {icon: 1, time: 1000}, function () {
                                var pIndex = parent.layer.getFrameIndex(window.name);
                                parent.location.reload();
                            });
                        } else {
                            layer.msg(res.msg, {icon: 2});
                        }
                    }
                });
            });
        });

        // 解锁
        $('#js-btn-unlock').click(function () {
            layer.confirm('确定解锁当前产品的工艺规划吗？', {icon: 3, title: '确认解锁'}, function (index) {
                spUtil.ajax({
                    url: '${request.contextPath}/admin/processPlan/unlock',
                    type: 'POST',
                    data: {bomId: bomId},
                    success: function (res) {
                        layer.close(index);
                        if (res.code === 0) {
                            layer.msg('已解锁', {icon: 1, time: 1000}, function () {
                                var pIndex = parent.layer.getFrameIndex(window.name);
                                parent.location.reload();
                            });
                        } else {
                            layer.msg(res.msg, {icon: 2});
                        }
                    }
                });
            });
        });

        // 初始化加载工序流程定义列表
        loadFlowList();
    });
</script>
</body>
</html>