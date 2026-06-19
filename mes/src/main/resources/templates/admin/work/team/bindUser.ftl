<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>绑定员工</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0">
    <#include "${request.contextPath}/common/common.ftl">
</head>
<body>
<div class="splayui-container">
    <div class="splayui-main">
        <div id="js-user-list" class="demo-transfer" style="padding: 10px;"></div>
        <div class="layui-form-item layui-hide">
            <div class="layui-input-block">
                <input id="js-team-id" value="${teamId}"/>
                <button id="js-submit" class="layui-btn">确定</button>
            </div>
        </div>
    </div>
</div>
<script>
    layui.use(['transfer', 'layer'], function () {
        var transfer = layui.transfer,
            layer = layui.layer;

        var teamId = spUtil.parseQueryString(window.location.href).teamId || $('#js-team-id').val();

        // 加载所有用户和已绑定用户
        $.when(
            $.ajax({ url: '${request.contextPath}/admin/work/team/user-list', type: 'GET' }),
            $.ajax({ url: '${request.contextPath}/admin/work/team/bind-user-ids?teamId=' + teamId, type: 'GET' })
        ).done(function (userRes, bindRes) {
            var userData = [];
            var selectedIds = [];

            if (userRes[0].code === 0 && userRes[0].data) {
                for (var i = 0; i < userRes[0].data.length; i++) {
                    var u = userRes[0].data[i];
                    userData.push({
                        value: u.id,
                        title: u.name + ' (' + u.username + ')',
                        disabled: false,
                        checked: false
                    });
                }
            }

            if (bindRes[0].code === 0 && bindRes[0].data) {
                selectedIds = bindRes[0].data;
            }

            transfer.render({
                elem: '#js-user-list',
                id: 'userTransfer',
                data: userData,
                value: selectedIds,
                title: ['可选员工', '已选员工'],
                showSearch: true,
                width: 240,
                height: 320
            });
        }).fail(function () {
            layer.msg('数据加载失败');
        });

        // 提交按钮点击事件
        $('#js-submit').on('click', function () {
            var selectedData = transfer.getData('userTransfer');
            var userIds = [];
            for (var i = 0; i < selectedData.length; i++) {
                userIds.push(selectedData[i].value);
            }

            $.ajax({
                url: '${request.contextPath}/admin/work/team/save-bind-users',
                type: 'POST',
                traditional: true,
                data: {
                    teamId: teamId,
                    userIds: userIds
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
