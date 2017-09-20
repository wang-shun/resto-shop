<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<style>
    #tableBodyList table tr th:first-of-type,#tableBodyList table tr td:first-of-type{display: none;}
    #tableBodyList table tr th,#tableBodyList table tr td{text-align: center;}
    #tableBodyLists th,#tableBodyLists td{text-align: center;line-height:2.5;}
</style>
<div id="control">
    <div class="row form-div" v-if="showform">
        <div class="col-md-offset-3 col-md-6" >
            <div class="portlet light bordered">
                <div class="portlet-title">
                    <div class="caption">
                        <span class="caption-subject bold font-blue-hoki">新增BOM</span>
                    </div>
                </div>

                <div class="portlet-body">
                    <form role="form" class="form-horizontal" action="{{id?'scmMaterial/modify':'scmMaterial/create'}}" @submit.prevent="save">
                        <input type="hidden" name="id" v-model="id" />
                        <div class="form-body">
                            <div class="form-group row">
                                <label class="col-md-2 control-label">菜品类别</label>
                                <div class="col-md-3">
                                    <select name="materialType" v-model="articleFamilyId" class="bs-select form-control" >
                                        <option  v-for="materialType in materialTypes" value="{{materialType.code}}">
                                            {{materialType.name}}
                                        </option>
                                    </select>
                                </div>

                                <label class="col-md-2 control-label">菜品</label>
                                <div class="col-md-3">
                                <select name="categoryOneId" v-model="productName" class="bs-select form-control" >
                                    <option  v-for="categoryOne in categoryOnes" value="{{categoryOne.id}}">
                                        {{categoryOne.categoryName}}
                                    </option>
                                </select>
                                </div>
                            </div>

                            <div class="form-group row" >
                                <label class="col-md-2 control-label">菜品编号</label>
                                <div class="col-md-3">
                                    <label class="col-md-2 control-label"> {{productCode}}</label>
                                </div>
                                    <label class="col-md-2 control-label">计量单位</label>
                                    <div class="col-md-3">
                                        <input type="text" class="form-control" name="materialName" v-model="materialName" required="required">
                                    </div>
                            </div>


                            <div class="form-group row">
                                <label class="col-md-2 control-label">版本号</label>
                                <div class="col-md-3">
                                    <input type="text" class="form-control" name="priority" v-model="priority" required="required">
                                </div>

                                <label class="col-md-2 control-label">序号</label>
                                <div class="col-md-3">
                                    <input type="text" class="form-control" name="priority" v-model="priority" required="required">
                                </div>
                            </div>

                            <div class="form-group row">
                                <label class="col-md-2 control-label">产品原料</label>
                                <div class="col-md-3">
                                    <input class="btn btn-default"  type="button"  value="添加原料"/>
                                </div>
                            </div>

                        </div>

                        <div class="form-group text-center">
                            <input class="btn green"  type="submit"  value="保存"/>&nbsp;&nbsp;&nbsp;
                            <a class="btn default" @click="cancel" >取消</a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <div class="table-div">
        <div class="table-operator">
            <s:hasPermission name="scmMaterial/add">
                <button class="btn green pull-right" @click="create">新建BOM</button>
            </s:hasPermission>
        </div>
        <div class="clearfix"></div>
        <div class="table-filter"></div>
        <div class="table-body" id="tableBodyList">
            <table class="table table-striped table-hover table-bordered"></table>
        </div>
    </div>
    <div id="tableBodyLists" v-show="tableBodyListsShow">
        <table border="1" cellpadding="2" cellspacing="0" align="center" width="100%">
        </table>
    </div>
</div>

<script>
    (function(){
        var tableBodyList = $("#tableBodyList>table");
        var tb = tableBodyList.DataTable({
            ajax : {
                url : "scmBom/list_all",
                dataSrc : "data",
                type : "GET",
                data : function(data) {
                    return data;
                },
            },
            columns : [
                {
                    data : "bomDetailDoList",
                    createdCell : function(td,tdData){
                        var html='<tr><th>行号</th><th>原料编码</th><th>原料类型</th><th>原料名称</th><th>规格</th><th>最小单位</th><th>所需最小单位数量</th></tr>';
                        for(var i=0;i<tdData.length;i++){
                            html+='<tr><td>'+(i+1)+'</td><td>'+tdData[i].materialCode+'</td><td>'+tdData[i].materialCode+'</td><td>'+tdData[i].materialName+'</td><td>'+tdData[i].minMeasureUnit+tdData[i].unitName+'/'+tdData[i].specName+'</td><td>'+tdData[i].materialCode+'</td><td>'+tdData[i].minMeasureUnit+'</td></tr>';
                        }
                        $(td).addClass('bomDetailDoList');
                        $(td).html(html);
                    }
                },
                {
                    title : "序号",
                    data : "priority",
                },
                {
                    title: "菜品编码",
                    data : "productCode"
                },
                {
                    title: "版本号",
                    data: "version",
                },
                {
                    title : "菜品类别",
                    data : "familyName",
                },
                {
                    title : "菜品名称",
                    data : "articleName",
                },
                {
                    title : "计量单位 ",
                    data : "measurementUnit",
                },
                {
                    title : "原料种类",
                    data : "size",
                    createdCell : function(td,tdData){
                        $(td).html(tdData+'种');
                    }
                },
                {
                    title : "操作",
                    data : "id",
                    createdCell:function(td,tdData,rowData,row){
                        var operator=[
                            <s:hasPermission name="scmMaterial/edit">
                            C.createEditBtn(rowData),
                            </s:hasPermission>
                            <s:hasPermission name="scmMaterial/delete">
                            C.createDelBtn(tdData,"scmMaterial/delete"),
                            </s:hasPermission>
                        ];
                        $(td).html(operator);
                    }
                }],
        });
        var C = new Controller(null,tb);
        var vueObj = new Vue({
            mixins:[C.formVueMix],
            el:"#control",
            data:{
                tableBodyListsShow:false,//列表详情页
                showform:false,//弹出框

                articleFamilyId:[],//菜品类别id
                productName:[],//菜品名称
                productCode:[],//产品编码
                measurementUnit:[],//计量单位
                version:[],//版本号
                priority:[],//序号
            },
            methods:{
                closeForm:function(){ //关闭新增弹窗
                    this.showform=false;
                },
                create:function(){ //打开新增弹窗
                    this.showform=true;
                },
                edit:function(model){ //编辑打开弹窗
                    this.m= model;
                    this.showform=true;
                },
                save:function(e){
                    var that = this;
                    var formDom = e.target;
                    C.ajaxFormEx(formDom,function(){
                        that.cancel();
                        tb.ajax.reload();
                    });
                }
            },
            ready:function(){//钩子
                var that = this;
                $('#tableBodyList').on('click','table tbody tr',function () {//显示详情
                    that.tableBodyListsShow=true;
                    $('#tableBodyLists table').html('');
                    $('#tableBodyLists table').html($(this).find('.bomDetailDoList').html());
                });
            },
        });
        C.vue=vueObj;
    }());

</script>
