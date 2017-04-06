<%@ page language="java" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div id="control">
    <div class="row form-div" v-if="showform">
        <div class="col-md-offset-3 col-md-6">
            <div class="portlet light bordered">
                <div class="portlet-title">
                    <div class="caption">
                        <span class="caption-subject bold font-blue-hoki"> 表单</span>
                    </div>
                </div>
                <div class="portlet-body">
                    <form role="form" action="{{m.id?'area/modify':'area/create'}}" @submit.prevent="save">
                        <div class="form-body">
                            <div class="form-group">
                                <label>桌位名称</label>
                                <input type="text" class="form-control" name="name" v-model="m.name"
                                       @blur="checkName(m.name)">
                            </div>



                            <div class="form-group">
                                <label class="col-sm-3 control-label">打印机名称：</label>
                                <div class="col-sm-8">
                                    <select class="form-control" name="printerId" required v-if="printerList" v-model="m.printId?m.printerId:selected">
                                        <option v-for="temp in printerList" v-bind:value="temp.id">
                                            {{ temp.name }}
                                        </option>
                                    </select>
                                    <input class="form-control" value="暂无可用打印机" disabled="disabled" v-else />
                                </div>
                            </div>



                        </div>
                        <input type="hidden" name="id" v-model="m.id"/>
                        <input class="btn green" type="submit" value="保存"/>
                        <a class="btn default" @click="cancel">取消</a>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <div class="table-div">
        <div class="table-operator">
            <s:hasPermission name="tablecode/add">
                <button class="btn green pull-right" @click="create">新建</button>
            </s:hasPermission>
        </div>
        <div class="clearfix"></div>
        <div class="table-filter"></div>
        <div class="table-body">
            <table class="table table-striped table-hover table-bordered "></table>
        </div>
    </div>
</div>


<script>
    (function () {
        var cid = "#control";
        var $table = $(".table-body>table");
        var tb = $table.DataTable({
            "order": [[2, "asc"]],
            ajax: {
                url: "area/list_all",
                dataSrc: ""
            },
            columns: [
                {
                    title: "桌位名称",
                    data: "name",
                },
                {
                    title: "模板项",
                    data: "printer",
                    defaultContent: "",
                    createdCell: function (td, tdData) {
                        $(td).html('');
                        if (tdData.name) {
                            var span = $("<span class='btn blue btn-xs'></span>");
                            $(td).append(span.html(tdData.name));
                        }


                    }
                },
                {
                    title: "是否开启",
                    data: "isUsed",
                    createdCell: function (td, tdData) {
                        if (tdData == 1) {
                            $(td).html("开启");
                        } else if (tdData == 0) {
                            $(td).html("未开启")
                        }
                    }
                },

                {
                    title: "操作",
                    data: "id",
                    createdCell: function (td, tdData, rowData, row) {
                        var operator = [
                            <s:hasPermission name="tabcode/delete">
                            C.createDelBtn(tdData, "tablecode/delete"),
                            </s:hasPermission>
                            <s:hasPermission name="tablecode/modify">
                            C.createEditBtn(rowData),
                            </s:hasPermission>
                        ];
                        $(td).html(operator);
                    }
                }],
        });

        var C = new Controller(null, tb);
        var vueObj = new Vue({
            el: "#control",
            mixins: [C.formVueMix],
            methods: {



            }

        });
        C.vue = vueObj;
        //获取 就餐模式
        $.ajax({
            type:"post",
            url:"printer/list_all",
            dataType:"json",
            success:function(data){
                if(data.length > 0){
                    vueObj.$set("printerList",data);
                    vueObj.$set("selected",data[0].id);
                }
            }
        })
    }());


</script>
