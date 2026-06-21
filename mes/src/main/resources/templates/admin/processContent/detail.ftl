<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>工艺内容编制详情</title>
    <#include "${request.contextPath}/common/common.ftl">
    <style>
        .content-header { padding: 12px 15px; background: #f2f2f2; margin-bottom: 10px; display: flex; align-items: center; justify-content: space-between; }
        .content-header .content-title { font-size: 15px; }
        .content-header .content-title strong { margin-right: 10px; }
        .node-type-tag { display: inline-block; padding: 0 6px; border-radius: 2px; font-size: 12px; line-height: 20px; }
        .node-type-0 { background: #009688; color: #fff; }
        .node-type-1 { background: #5FB878; color: #fff; }
        .node-type-2 { background: #1E9FFF; color: #fff; }
        .content-status-ok { color: #5FB878; }
        .content-status-no { color: #999; }
        .warn-tip { background: #fff3cd; border: 1px solid #ffc107; color: #856404; padding: 10px 15px; margin-bottom: 10px; border-radius: 2px; }
        .sop-form .layui-form-item { margin-bottom: 8px; }
        .sop-form textarea { min-height: 80px; }
        .img-list { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 8px; }
        .img-list .img-item { position: relative; width: 100px; height: 100px; border: 1px solid #e6e6e6; display: flex; align-items: center; justify-content: center; }
        .img-list .img-item img { max-width: 100%; max-height: 100%; }
        .img-list .img-item .img-del { position: absolute; top: 0; right: 0; width: 20px; height: 20px; line-height: 20px; text-align: center; background: rgba(255,0,0,0.7); color: #fff; cursor: pointer; font-size: 12px; }
        .file-list { margin-top: 8px; }
        .file-list .file-item { display: flex; align-items: center; justify-content: space-between; padding: 6px 10px; background: #f8f8f8; border: 1px solid #e6e6e6; margin-bottom: 6px; border-radius: 2px; }
        .file-list .file-item a { color: #1E9FFF; }
        .file-list .file-item .file-del { color: #FF5722; cursor: pointer; margin-left: 10px; }
    </style>
</head>
<body>
<div class="splayui-container">
    <div class="content-header">
        <div class="content-title">
            <strong>产品：</strong>${bomName}
            <#if processPlanLocked == '1'>
                <span class="layui-btn layui-btn-xs layui-btn-danger">工艺已锁定</span>
            <#else>
                <span class="layui-btn layui-btn-xs layui-btn-warm">工艺未锁定</span>
            </#if>
        </div>
    </div>

    <#if processPlanLocked != '1'>
    <div class="warn-tip">
        <i class="layui-icon layui-icon-tips"></i>
        <strong>提示：</strong>该产品工艺尚未锁定，无法编制工艺内容。请先完成"工艺流程管理"并锁定工艺后，再回到本页面编制SOP内容。
    </div>
    </#if>

    <table id="js-node-table" class="layui-table" lay-filter="js-node-table"></table>
</div>

<!-- 工艺内容编制弹窗 -->
<div id="js-content-form-popup" style="display:none; padding: 15px;">
    <form class="layui-form sop-form" lay-filter="js-content-form-filter">
        <input type="hidden" name="id" id="content-id">
        <input type="hidden" name="bomId" value="${bomId}">
        <input type="hidden" name="bomNodeId" id="content-bom-node-id">
        <input type="hidden" name="processId" id="content-process-id">
        <input type="hidden" name="processImages" id="content-process-images">
        <input type="hidden" name="techDocs" id="content-tech-docs">

        <div class="layui-form-item">
            <label class="layui-form-label">BOM节点</label>
            <div class="layui-input-block">
                <input type="text" id="content-node-name" class="layui-input" disabled>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">当前工序</label>
            <div class="layui-input-block">
                <input type="text" id="content-process-name" class="layui-input" disabled>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">操作步骤</label>
            <div class="layui-input-block">
                <textarea name="operationSteps" id="content-operation-steps" placeholder="请输入工序详细操作步骤..." class="layui-textarea"></textarea>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">设备工装</label>
            <div class="layui-input-block">
                <textarea name="equipmentTools" id="content-equipment-tools" placeholder="请输入所需设备及工装夹具..." class="layui-textarea"></textarea>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">物料清单</label>
            <div class="layui-input-block">
                <textarea name="materialList" id="content-material-list" placeholder="请输入所需物料清单..." class="layui-textarea"></textarea>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">技术参数</label>
            <div class="layui-input-block">
                <textarea name="techParams" id="content-tech-params" placeholder="请输入技术参数要求..." class="layui-textarea"></textarea>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">自检标准</label>
            <div class="layui-input-block">
                <textarea name="selfCheckStd" id="content-self-check-std" placeholder="请输入自检标准及要求..." class="layui-textarea"></textarea>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">安全要求</label>
            <div class="layui-input-block">
                <textarea name="safetyReq" id="content-safety-req" placeholder="请输入安全及防静电要求..." class="layui-textarea"></textarea>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">工艺附图</label>
            <div class="layui-input-block">
                <button type="button" class="layui-btn layui-btn-sm" id="btn-upload-img">
                    <i class="layui-icon">&#xe67c;</i>上传图片
                </button>
                <div class="img-list" id="img-list"></div>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">技术文档</label>
            <div class="layui-input-block">
                <button type="button" class="layui-btn layui-btn-sm layui-btn-normal" id="btn-upload-doc">
                    <i class="layui-icon">&#xe655;</i>上传文档
                </button>
                <div class="file-list" id="file-list"></div>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">备注</label>
            <div class="layui-input-block">
                <textarea name="remark" id="content-remark" placeholder="请输入备注信息..." class="layui-textarea"></textarea>
            </div>
        </div>

        <div class="layui-form-item" style="text-align: center; margin-top: 15px;">
            <button type="button" class="layui-btn" id="btn-save-content">保存</button>
            <button type="button" class="layui-btn layui-btn-primary" id="btn-cancel-content">取消</button>
        </div>
    </form>
</div>

<script>
    layui.use(['treeTable', 'form', 'layer', 'upload'], function () {
        var treeTable = layui.treeTable,
            form = layui.form,
            layer = layui.layer,
            upload = layui.upload;

        var bomId = '${bomId}';
        var isLocked = '${processPlanLocked}' === '1';
        var g_nodes = [];
        var g_processImages = [];
        var g_techDocs = [];

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
                    field: 'processName', title: '绑定工序', width: 180, templet: function (d) {
                        return d.processName || '<span style="color:#999;">未绑定</span>';
                    }
                },
                {
                    field: 'hasContent', title: '编制状态', width: 100, templet: function (d) {
                        if (d.hasContent) {
                            return '<span class="content-status-ok"><i class="layui-icon layui-icon-ok-circle"></i> 已编制</span>';
                        }
                        return '<span class="content-status-no">未编制</span>';
                    }
                },
                {
                    title: '操作', width: 140, align: 'center', templet: function (d) {
                        if (d.nodeType === '2') {
                            return '<span class="layui-btn layui-btn-xs layui-btn-disabled">无需编制</span>';
                        }
                        if (!isLocked) {
                            return '<span class="layui-btn layui-btn-xs layui-btn-disabled">未锁定</span>';
                        }
                        if (!d.processId) {
                            return '<span class="layui-btn layui-btn-xs layui-btn-disabled">未绑定工序</span>';
                        }
                        if (d.hasContent) {
                            return '<a class="layui-btn layui-btn-xs layui-btn-warm" data-action="editContent" data-id="' + d.id + '">编辑内容</a>';
                        }
                        return '<a class="layui-btn layui-btn-xs layui-btn-normal" data-action="editContent" data-id="' + d.id + '">编制内容</a>';
                    }
                }
            ],
            reqData: function (data, callback) {
                $.ajax({
                    url: '${request.contextPath}/admin/processContent/node-list',
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

        // 获取节点数据
        function getNode(nodeId) {
            for (var i = 0; i < g_nodes.length; i++) {
                if (g_nodes[i].id === nodeId) {
                    return g_nodes[i];
                }
            }
            return null;
        }

        // 加载节点工艺内容
        function loadContent(nodeId, callback) {
            $.ajax({
                url: '${request.contextPath}/admin/processContent/get-content',
                type: 'GET',
                data: {bomNodeId: nodeId},
                dataType: 'json',
                success: function (res) {
                    if (res.code === 0) {
                        callback(res.data);
                    } else {
                        callback(null);
                    }
                },
                error: function () {
                    callback(null);
                }
            });
        }

        // 渲染图片列表
        function renderImgList() {
            var html = '';
            for (var i = 0; i < g_processImages.length; i++) {
                html += '<div class="img-item">' +
                    '<img src="' + g_processImages[i] + '">' +
                    '<span class="img-del" data-index="' + i + '">&times;</span>' +
                    '</div>';
            }
            $('#img-list').html(html);
            $('#content-process-images').val(JSON.stringify(g_processImages));
        }

        // 渲染文档列表
        function renderFileList() {
            var html = '';
            for (var i = 0; i < g_techDocs.length; i++) {
                var doc = g_techDocs[i];
                html += '<div class="file-item">' +
                    '<a href="' + doc.url + '" target="_blank" title="点击下载">' + (doc.name || doc.url) + '</a>' +
                    '<span class="file-del" data-index="' + i + '"><i class="layui-icon">&#x1006;</i> 删除</span>' +
                    '</div>';
            }
            $('#file-list').html(html);
            $('#content-tech-docs').val(JSON.stringify(g_techDocs));
        }

        // 删除图片
        $(document).on('click', '.img-del', function () {
            var idx = parseInt($(this).data('index'));
            g_processImages.splice(idx, 1);
            renderImgList();
        });

        // 删除文档
        $(document).on('click', '.file-del', function () {
            var idx = parseInt($(this).data('index'));
            g_techDocs.splice(idx, 1);
            renderFileList();
        });

        // 工艺附图上传
        upload.render({
            elem: '#btn-upload-img',
            url: '${request.contextPath}/upload/process-img',
            multiple: true,
            done: function (res) {
                if (res.code === 0) {
                    g_processImages.push(res.data);
                    renderImgList();
                    layer.msg('上传成功', {icon: 1});
                } else {
                    layer.msg(res.msg || '上传失败', {icon: 2});
                }
            },
            error: function () {
                layer.msg('上传失败', {icon: 2});
            }
        });

        // 技术文档上传
        upload.render({
            elem: '#btn-upload-doc',
            url: '${request.contextPath}/upload/tech-doc',
            multiple: true,
            accept: 'file',
            done: function (res) {
                if (res.code === 0) {
                    g_techDocs.push(res.data);
                    renderFileList();
                    layer.msg('上传成功', {icon: 1});
                } else {
                    layer.msg(res.msg || '上传失败', {icon: 2});
                }
            },
            error: function () {
                layer.msg('上传失败', {icon: 2});
            }
        });

        // 打开编制弹窗
        function openContentForm(node) {
            $('#content-id').val('');
            $('#content-bom-node-id').val(node.id);
            $('#content-process-id').val(node.processId || '');
            $('#content-node-name').val(node.nodeName || '');
            $('#content-process-name').val(node.processName || '');
            $('#content-operation-steps').val('');
            $('#content-equipment-tools').val('');
            $('#content-material-list').val('');
            $('#content-tech-params').val('');
            $('#content-self-check-std').val('');
            $('#content-safety-req').val('');
            $('#content-remark').val('');
            g_processImages = [];
            g_techDocs = [];
            renderImgList();
            renderFileList();

            // 加载已有内容
            loadContent(node.id, function (data) {
                if (data) {
                    $('#content-id').val(data.id || '');
                    $('#content-operation-steps').val(data.operationSteps || '');
                    $('#content-equipment-tools').val(data.equipmentTools || '');
                    $('#content-material-list').val(data.materialList || '');
                    $('#content-tech-params').val(data.techParams || '');
                    $('#content-self-check-std').val(data.selfCheckStd || '');
                    $('#content-safety-req').val(data.safetyReq || '');
                    $('#content-remark').val(data.remark || '');
                    if (data.processImages) {
                        try {
                            g_processImages = JSON.parse(data.processImages) || [];
                        } catch (e) { g_processImages = []; }
                    }
                    if (data.techDocs) {
                        try {
                            g_techDocs = JSON.parse(data.techDocs) || [];
                        } catch (e) { g_techDocs = []; }
                    }
                    renderImgList();
                    renderFileList();
                }
                form.render();
            });

            layer.open({
                type: 1,
                title: '编制工艺内容 - ' + (node.nodeName || ''),
                area: ['860px', '92%'],
                content: $('#js-content-form-popup'),
                success: function (layero, index) {
                    form.render();
                }
            });
        }

        // 监听编制按钮
        $(document).on('click', '[data-action="editContent"]', function () {
            var nodeId = $(this).data('id');
            var node = getNode(nodeId);
            if (!node) {
                layer.msg('节点数据异常', {icon: 2});
                return;
            }
            openContentForm(node);
        });

        // 保存工艺内容
        $('#btn-save-content').click(function () {
            var formData = {
                id: $('#content-id').val(),
                bomId: bomId,
                bomNodeId: $('#content-bom-node-id').val(),
                processId: $('#content-process-id').val(),
                operationSteps: $('#content-operation-steps').val(),
                equipmentTools: $('#content-equipment-tools').val(),
                materialList: $('#content-material-list').val(),
                techParams: $('#content-tech-params').val(),
                selfCheckStd: $('#content-self-check-std').val(),
                safetyReq: $('#content-safety-req').val(),
                processImages: $('#content-process-images').val(),
                techDocs: $('#content-tech-docs').val(),
                remark: $('#content-remark').val()
            };

            if (!formData.processId) {
                layer.msg('该节点未绑定工序，无法保存', {icon: 2});
                return;
            }

            $.ajax({
                url: '${request.contextPath}/admin/processContent/save',
                type: 'POST',
                data: formData,
                dataType: 'json',
                success: function (res) {
                    if (res.code === 0) {
                        layer.msg('保存成功', {icon: 1, time: 1000}, function () {
                            layer.closeAll();
                            insTb.reload();
                        });
                    } else {
                        layer.msg(res.msg || '保存失败', {icon: 2});
                    }
                },
                error: function () {
                    layer.msg('保存失败，请检查网络', {icon: 2});
                }
            });
        });

        $('#btn-cancel-content').click(function () {
            layer.closeAll();
        });
    });
</script>
</body>
</html>
