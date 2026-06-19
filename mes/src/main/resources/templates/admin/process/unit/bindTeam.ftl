<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>绑定班组/编组</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0">
    <#include "${request.contextPath}/common/common.ftl">
</head>
<body>
<div class="splayui-container">
    <div class="splayui-main">
        <div id="js-transfer-list" class="demo-transfer" style="padding: 10px;"></div>
        <div class="layui-form-item layui-hide">
            <div class="layui-input-block">
                <input id="js-process-unit-id" value="${processUnitId}"/>
                <input id="js-process-unit-type" value="${(type)!'1'}"/>
                <button id="js-submit" class="layui-btn">确定</button>
            </div>
        </div>
    </div>
</div>
<script>
    layui.use(['transfer', 'layer'], function () {
        var transfer = layui.transfer,
            layer = layui.layer;

        var processUnitId = spUtil.parseQueryString(window.location.href).processUnitId || $('#js-process-unit-id').val();
        var isEquipmentType = false;

        function loadTransferData() {
            var listUrl = isEquipmentType
                ? '${request.contextPath}/admin/process/unit/equipment-group-list'
                : '${request.contextPath}/admin/process/unit/team-list';
            var bindIdsUrl = isEquipmentType
                ? '${request.contextPath}/admin/process/unit/bind-equipment-group-ids?processUnitId=' + processUnitId
                : '${request.contextPath}/admin/process/unit/bind-team-ids?processUnitId=' + processUnitId;
            var leftTitle = isEquipmentType ? '可选设备编组' : '可选班组';
            var rightTitle = isEquipmentType ? '已选设备编组' : '已选班组';

            $.when(
                $.ajax({ url: listUrl, type: 'GET', cache: false }),
                $.ajax({ url: bindIdsUrl, type: 'GET', cache: false })
            ).done(function (listRes, bindRes) {
                var data = [];
                var selectedIds = [];

                if (listRes[0].code === 0 && listRes[0].data) {
                    for (var i = 0; i < listRes[0].data.length; i++) {
                        var item = listRes[0].data[i];
                        data.push({
                            value: item.id,
                            title: item.name + ' (' + item.code + ')',
                            disabled: false,
                            checked: false
                        });
                    }
                }

                if (bindRes[0].code === 0 && bindRes[0].data) {
                    selectedIds = bindRes[0].data;
                }

                transfer.render({
                    elem: '#js-transfer-list',
                    id: 'unitTransfer',
                    data: data,
                    value: selectedIds,
                    title: [leftTitle, rightTitle],
                    showSearch: true,
                    width: 240,
                    height: 320
                });
            }).fail(function () {
                layer.msg('数据加载失败');
            });
        }

        // 先查询加工单元最新类型，再加载对应数据
        $.ajax({
            url: '${request.contextPath}/admin/process/unit/detail?id=' + processUnitId,
            type: 'GET',
            cache: false,
            success: function (res) {
                if (res.code === 0 && res.data) {
                    isEquipmentType = (res.data.type === '2');
                }
                loadTransferData();
            },
            error: function () {
                layer.msg('加载加工单元信息失败');
                loadTransferData();
            }
        });

        $('#js-submit').on('click', function () {
            var selectedData = transfer.getData('unitTransfer');
            var ids = [];
            for (var i = 0; i < selectedData.length; i++) {
                ids.push(selectedData[i].value);
            }

            var saveUrl = isEquipmentType
                ? '${request.contextPath}/admin/process/unit/save-bind-equipment-groups'
                : '${request.contextPath}/admin/process/unit/save-bind-teams';
            var saveData = isEquipmentType
                ? { processUnitId: processUnitId, equipmentGroupIds: ids }
                : { processUnitId: processUnitId, teamIds: ids };

            $.ajax({
                url: saveUrl,
                type: 'POST',
                traditional: true,
                data: saveData,
                success: function (res) {
                    if (res.code === 0) {
                        layer.msg('绑定成功', {icon: 1, time: 1500});
                        setTimeout(function () {
                            var index = parent.layer.getFrameIndex(window.name);
                            parent.layer.close(index);
                            parent.location.reload();
                        }, 1500);
                    } else {
                        layer.msg('绑定失败：' + (res.msg || '未知错误'));
                    }
                },
                error: function () {
                    layer.msg('绑定失败，请检查网络');
                }
            });
        });
    });
</script>
</body>
</html>
