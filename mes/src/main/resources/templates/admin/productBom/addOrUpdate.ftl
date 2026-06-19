<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>产品BOM</title>
    <#include "${request.contextPath}/common/common.ftl">
    <style>
        .materiel-select-box {
            display: flex;
            align-items: center;
        }
        .materiel-select-box input {
            flex: 1;
        }
        .materiel-select-box button {
            margin-left: 10px;
        }
    </style>
</head>
<body>
<div class="layui-form" style="padding: 20px;">
    <input type="hidden" name="id" value="${(result.id)!}">

    <div class="layui-form-item">
        <label class="layui-form-label sp-required">产品物料</label>
        <div class="layui-input-block materiel-select-box">
            <input type="hidden" name="productMaterielCode" id="productMaterielCode" value="${(result.productMaterielCode)!}">
            <input type="text" id="productMaterielDesc" autocomplete="off" class="layui-input" readonly
                   value="${(result.productMaterielDesc)!}" placeholder="请选择产品物料">
            <button type="button" class="layui-btn layui-btn-sm" id="btnSelectMateriel">选择</button>
        </div>
    </div>

    <#if result?? && result.id??>
    <div class="layui-form-item">
        <label class="layui-form-label">BOM编码</label>
        <div class="layui-input-block">
            <input type="text" class="layui-input" value="${result.bomCode}" disabled>
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">版本号</label>
        <div class="layui-input-block">
            <input type="text" class="layui-input" value="${result.version}" disabled>
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">状态</label>
        <div class="layui-input-block">
            <input type="text" class="layui-input" value="${(result.state == '1')?string('已定版', '草稿')}" disabled>
        </div>
    </div>
    </#if>

    <div class="layui-form-item">
        <label class="layui-form-label">备注</label>
        <div class="layui-input-block">
            <textarea name="remark" class="layui-textarea" rows="3">${(result.remark)!}</textarea>
        </div>
    </div>

    <div class="layui-form-item" style="text-align: center;">
        <button class="layui-btn" lay-submit lay-filter="bom-form-filter">保存</button>
        <button type="reset" class="layui-btn layui-btn-primary">重置</button>
    </div>
</div>

<script>
    layui.use(['form', 'layer', 'table'], function () {
        var form = layui.form,
            layer = layui.layer,
            table = layui.table;

        // 选择产品物料
        document.getElementById('btnSelectMateriel').addEventListener('click', function() {
            layer.open({
                type: 1,
                title: '选择产品物料',
                area: ['700px', '500px'],
                content: '<div style="padding: 10px;"><table id="materielTable" lay-filter="materielTable"></table></div>',
                success: function(){
                    table.render({
                        elem: '#materielTable',
                        url: '${request.contextPath}/admin/productBom/materiel/list?matType=产品',
                        cols: [[
                            {field: 'materiel', title: '物料编码', width: 120},
                            {field: 'materielDesc', title: '物料名称', width: 180},
                            {field: 'unit', title: '单位', width: 80},
                            {title: '操作', width: 100, templet: function(d){
                                return '<a class="layui-btn layui-btn-xs" onclick="selectMateriel(\''+d.materiel+'\',\''+d.materielDesc+'\')">选择</a>';
                            }}
                        ]]
                    });
                }
            });
        });

        window.selectMateriel = function(code, desc){
            document.getElementById('productMaterielCode').value = code;
            document.getElementById('productMaterielDesc').value = desc;
            layer.closeAll();
        };

        form.on('submit(bom-form-filter)', function (data) {
            if (!data.field.productMaterielCode) {
                layer.msg('请选择产品物料');
                return false;
            }
            spUtil.ajax({
                url: '${request.contextPath}/admin/productBom/add-or-update',
                type: 'POST',
                data: data.field,
                success: function (res) {
                    if (res.code === 0) {
                        layer.msg('保存成功', {icon: 1, time: 1000}, function () {
                            var index = parent.layer.getFrameIndex(window.name);
                            parent.layer.close(index);
                        });
                    } else {
                        layer.msg(res.msg, {icon: 2});
                    }
                }
            });
            return false;
        });
    });
</script>
</body>
</html>
