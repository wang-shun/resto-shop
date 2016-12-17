<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div id="control">
    <div class="row form-div" v-if="showform">
        <div class="col-md-offset-3 col-md-6" >
            <div class="portlet light bordered">
                <div class="portlet-title">
                    <div class="caption">
                        <span class="caption-subject bold font-blue-hoki"> 新增表单 </span>
                    </div>
                </div>
                <div class="tab-pane active">
                    <form role="form" action="tableQrcode/create" @submit.prevent="save">
                        <div class="form-horizontal">
                            <div class="form-group">
                                <label class="col-sm-3 control-label">开始桌号：</label>
                                <div class="col-sm-5">
                                    <input type="number" class="form-control" name="beginTableNumber"
                                           id="beginTableNumber">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">结束桌号：</label>
                                <div class="col-sm-5">
                                    <input type="number" class="form-control" name="endTableNumber"
                                           id="endTableNumber">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">忽略桌号：</label>
                                <div class="col-sm-5">
                                    <input type="text" class="form-control" name="ignoreNumber" id="ignoreNumber"
                                           placeholder="如有多个桌号，请使用逗号分隔。">
                                </div>
                            </div>
                            <div class="form-group">
                                <div class="col-sm-offset-3 col-sm-3">
                                    <input class="btn green"  type="submit"  value="保存"/>
                                    <a class="btn default" @click="cancel" >取消</a>
                                </div>
                                <div class="col-sm-3" id="downQRFile"></div>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <div class="row form-div" v-if="showPlatform">
        <div class="col-md-offset-3 col-md-6" >
            <div class="portlet light bordered">
                <div class="portlet-title">
                    <div class="caption">
                        <span class="caption-subject bold font-blue-hoki"> 修改表单 </span>
                    </div>
                </div>
                <div class="portlet-body">
                    <form role="form" action="{{m.id?'tablecode/modify':'tablecode/create'}}" @submit.prevent="save">
                        <div class="form-horizontal">
                            <div class="form-group">
                                <label class="col-sm-3 control-label">最小人数</label>
                                <div class="col-sm-5">
                                    <input type="number" class="form-control" name="tableNumber" v-model="m.tableNumber">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label">是否启用：</label>
                                <div class="col-sm-5 radio-list">
                                    <label class="radio-inline">
                                        <input type="radio" name="state" v-model="m.state" value="1"> 启用
                                    </label>
                                    <label class="radio-inline">
                                        <input type="radio" name="state" v-model="m.state" value="0"> 不启用
                                    </label>
                                </div>
                            </div>

                            <div class="form-group">
                                <div class="col-sm-offset-3 col-sm-3">
                                    <input type="hidden" name="id" v-model="m.id" />
                                    <input class="btn green"  type="submit"  value="保存"/>
                                    <a class="btn default" @click="cancel" >取消</a>
                                </div>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <div class="table-div">
        <div class="table-operator">
            <s:hasPermission name="tableQrcode/add">
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
    (function(){
        var cid="#control";
        var $table = $(".table-body>table");
        var tb = $table.DataTable({
            ajax : {
                url : "tableQrcode/list_all",
                dataSrc : ""
            },
            columns : [
                {
                    title : "品牌名称",
                    data : "brandId",
                },
                {
                    title : "店铺名称",
                    data : "shopDetailId",
                },
                {
                    title : "桌号",
                    data : "tableNumber",
                },
                {
                    title : "创建时间",
                    data : "createTime",
                },
                {
                    title : "修改时间",
                    data : "updateTime",
                },
                {
                    title : "是否开启",
                    data : "state",
                    createdCell: function (td,tdData) {
                        if(tdData==1){
                            $(td).html("开启");
                        }else if(tdData==0){
                            $(td).html("未开启")
                        }
                    }
                },

                {
                    title : "操作",
                    data : "id",
                    createdCell:function(td,tdData,rowData,row){
                        alert(JSON.stringify(rowData));
                        var operator = [];
                        <s:hasPermission name="tableQrcode/modify">
                        operator.push(C.createBtn(rowData).html("编辑"));
                        </s:hasPermission>
                        $(td).html(operator);
                    }
                }],
        });

        var C = new Controller(null,tb);
        var vueObj = new Vue({
            el:"#control",
            mixins:[C.formVueMix],
            data: {
                showPlatform: false,
                showform: false
            },
            methods :{
                cancel: function () {
                    this.showPlatform = false;
                    this.showform = false;
                },
                platform: function (model) {
                    var that = this;
                    that.showPlatform = true;
                },
            }
        });
        C.vue=vueObj;
    }());
</script>