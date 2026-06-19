<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>绑定班组</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0">
    <#include "${request.contextPath}/common/common.ftl">
</head>
<body>
<div class="splayui-container">
    <div class="splayui-main">
        <div id="js-team-list" class="demo-transfer" style="padding: 10px;"></div>
        <div class="layui-form-item layui-hide">
            <div class="layui-input-block">
                <input id="js-process-unit-id" value="${processUnitId}"/>
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

        $.when(
            $.ajax({ url: '${request.contextPath}/admin/process/unit/team-list', type: 'GET' }),
            $.ajax({ url: '${request.contextPath}/admin/process/unit/bind-team-ids?processUnitId=' + processUnitId, type: 'GET' })
        ).done(function (teamRes, bindRes) {
            var teamData = [];
            var selectedIds = [];

            if (teamRes[0].code === 0 && teamRes[0].data) {
                for (var i = 0; i < teamRes[0].data.length; i++) {
                    var t = teamRes[0].data[i];
                    teamData.push({
                        value: t.id,
                        title: t.name + ' (' + t.code + ')',
                        disabled: false,
                        checked: false
                    });
                }
            }

            if (bindRes[0].code === 0 && bindRes[0].data) {
                selectedIds = bindRes[0].data;
            }

            transfer.render({
                elem: '#js-team-list',
                id: 'teamTransfer',
                data: teamData,
                value: selectedIds,
                title: ['可选班组', '已选班组'],
                showSearch: true,
                width: 240,
                height: 320
            });
        }).fail(function () {
            layer.msg('数据加载失败');
        });

        $('#js-submit').on('click', function () {
            var selectedData = transfer.getData('teamTransfer');
            var teamIds = [];
            for (var i = 0; i < selectedData.length; i++) {
                teamIds.push(selectedData[i].value);
            }

            $.ajax({
                url: '${request.contextPath}/admin/process/unit/save-bind-teams',
                type: 'POST',
                traditional: true,
                data: {
                    processUnitId: processUnitId,
                    teamIds: teamIds
                },
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
