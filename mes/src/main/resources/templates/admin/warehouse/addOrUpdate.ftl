<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>库房信息</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <#include "${request.contextPath}/common/common.ftl">
</head>
<body>
<div class="splayui-container">
    <div class="splayui-main">
        <form class="layui-form" action="">
            <input type="hidden" name="id" value="${(result.id)!}">

            <div class="layui-form-item">
                <div class="layui-inline">
                    <label class="layui-form-label sp-required">库房编码</label>
                    <div class="layui-input-inline">
                        <input type="text" name="code" lay-verify="required" autocomplete="off" class="layui-input" value="${(result.code)!}">
                    </div>
                </div>
                <div class="layui-inline">
                    <label class="layui-form-label sp-required">库房名称</label>
                    <div class="layui-input-inline">
                        <input type="text" name="name" lay-verify="required" autocomplete="off" class="layui-input" value="${(result.name)!}">
                    </div>
                </div>
            </div>

            <div class="layui-form-item">
                <div class="layui-inline">
                    <label class="layui-form-label sp-required">库房类型</label>
                    <div class="layui-input-inline">
                        <select name="type" id="js-type" lay-verify="required">
                            <option value="">请选择</option>
                        </select>
                    </div>
                </div>
            </div>

            <div class="layui-form-item">
                <div class="layui-inline">
                    <label class="layui-form-label sp-required">组数</label>
                    <div class="layui-input-inline">
                        <input type="number" name="groupCount" lay-verify="required|number|positive" autocomplete="off" class="layui-input" value="${(result.groupCount)!1}">
                    </div>
                </div>
                <div class="layui-inline">
                    <label class="layui-form-label sp-required">排数</label>
                    <div class="layui-input-inline">
                        <input type="number" name="rowCount" lay-verify="required|number|positive" autocomplete="off" class="layui-input" value="${(result.rowCount)!2}">
                    </div>
                </div>
            </div>

            <div class="layui-form-item">
                <div class="layui-inline">
                    <label class="layui-form-label sp-required">层数</label>
                    <div class="layui-input-inline">
                        <input type="number" name="layerCount" lay-verify="required|number|positive" autocomplete="off" class="layui-input" value="${(result.layerCount)!3}">
                    </div>
                </div>
                <div class="layui-inline">
                    <label class="layui-form-label sp-required">列数</label>
                    <div class="layui-input-inline">
                        <input type="number" name="columnCount" lay-verify="required|number|positive" autocomplete="off" class="layui-input" value="${(result.columnCount)!4}">
                    </div>
                </div>
            </div>

            <div class="layui-form-item">
                <div class="layui-inline">
                    <label class="layui-form-label sp-required">默认长(cm)</label>
                    <div class="layui-input-inline">
                        <input type="number" name="defaultLength" lay-verify="required|number|positive" autocomplete="off" class="layui-input" value="${(result.defaultLength)!50}">
                    </div>
                </div>
                <div class="layui-inline">
                    <label class="layui-form-label sp-required">默认宽(cm)</label>
                    <div class="layui-input-inline">
                        <input type="number" name="defaultWidth" lay-verify="required|number|positive" autocomplete="off" class="layui-input" value="${(result.defaultWidth)!50}">
                    </div>
                </div>
            </div>

            <div class="layui-form-item">
                <div class="layui-inline">
                    <label class="layui-form-label sp-required">默认高(cm)</label>
                    <div class="layui-input-inline">
                        <input type="number" name="defaultHeight" lay-verify="required|number|positive" autocomplete="off" class="layui-input" value="${(result.defaultHeight)!50}">
                    </div>
                </div>
            </div>

            <#if result.id??>
            <div class="layui-form-item">
                <div class="layui-inline">
                    <label class="layui-form-label">库位总数</label>
                    <div class="layui-input-inline">
                        <input type="text" class="layui-input" readonly value="${(result.groupCount)!1} x ${(result.rowCount)!2} x ${(result.layerCount)!3} x ${(result.columnCount)!4} = ${(result.groupCount!1)*(result.rowCount!2)*(result.layerCount!3)*(result.columnCount!4)}">
                    </div>
                </div>
            </div>
            </#if>

            <div class="layui-form-item layui-hide">
                <div class="layui-input-block">
                    <button id="js-submit" class="layui-btn" lay-submit lay-filter="js-submit-filter">确定</button>
                </div>
            </div>
        </form>
    </div>
</div>

<script>
    layui.use(['form', 'util'], function () {
        var form = layui.form,
            util = layui.util;

        // 加载库房类型字典
        spUtil.ajax({
            url: '${request.contextPath}/basedata/dict/list/warehouse_type',
            success: function (res) {
                if (res.code === 0) {
                    var html = '<option value="">请选择</option>';
                    for (var i = 0; i < res.data.length; i++) {
                        var item = res.data[i];
                        html += '<option value="' + item.value + '">' + item.name + '</option>';
                    }
                    $('#js-type').html(html);
                    <#if result.type??>
                    $('#js-type').val('${result.type}');
                    </#if>
                    form.render('select');
                }
            }
        });

        // 自定义验证规则：正整数
        form.verify({
            positive: function(value, item){
                if (!/^[1-9]\d*$/.test(value)) {
                    return '请输入大于0的正整数';
                }
            }
        });

        form.on('submit(js-submit-filter)', function (data) {
            spUtil.submitForm({
                url: "${request.contextPath}/admin/warehouse/add-or-update",
                data: data.field
            });

            return false;
        });
    });
</script>
</body>
</html>
