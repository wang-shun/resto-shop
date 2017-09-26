<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div id="control">
    <div class="row form-div" v-if="showform">
        <div class="col-md-offset-3 col-md-6" style="text-align:center">
            <div class="portlet light bordered">
                <div class="portlet-title">
                    <div class="caption">
                        <span class="caption-subject bold font-blue-hoki">新增供应商</span>
                    </div>
                </div>

                <div class="portlet-body">
                    <form role="form" class="form-horizontal" action="{{m.id?'scmSupplier/modify':'scmSupplier/create'}}" @submit.prevent="save" style="text-align:center">
                        <input type="hidden" name="id" v-model="m.id" />
                        <div class="form-body">

                            <div class="form-group row">
                                <label class="col-md-2 control-label">类型 </label>
                                <select class="col-md-3 border-radius" name="supplierType" v-model="m.supplierType"  style="height:30px">
                                    <option  v-for="materialType in materialTypes" value="{{supplierType.code}}">
                                        {{materialType.name}}
                                    </option>
                                </select>

                                <label class="col-md-2 control-label">编码</label>
                                <div class="col-md-3">
                                    <input type="text" class="form-control" name="supCode" v-model="m.supCode"
                                           required="required">
                                </div>
                            </div>

                            <div class="form-group row" >
                                <label class="col-md-2 control-label">公司名</label>
                                <div class="col-md-3">
                                    <input type="text" class="form-control" name="materialName" v-model="productCode" required="required">
                                </div>

                                <label class="col-md-2 control-label">别名</label>
                                <div class="col-md-3">
                                    <input type="text" class="form-control" name="supAliasName" v-model="m.supAliasName" required="required">
                                </div>
                            </div>


                            <div class="form-group row">
                                <label class="col-md-2 control-label">序号</label>
                                <div class="col-md-3">
                                    <input type="text" class="form-control" name="version" v-model="m.version"
                                           required="required">
                                </div>
                            </div>

                            <div class="form-group row">
                                <label class="col-md-2 control-label">产品(可多选)</label>
                                <div  class="col-md-4 checkbox-list">
                                    <label class="checkbox-inline">
                                        <input type="checkbox" name="printReceipt" v-model="m.printKitchen1" value = "1">主料
                                    </label>
                                    <label class="checkbox-inline">
                                        <input type="checkbox" name="printKitchen" v-model="m.printKitchen2" value ="1">辅料
                                    </label>
                                    <label class="checkbox-inline">
                                       <input type="checkbox" name="printKitchen" v-model="m.printKitchen3" value ="1">配料
                                   </label>
                                </div>
                            </div>
                         <%--编辑添加--%>
                                <table class="table table-bordered" style= "width:600px;">
                                    <thead >
                                    <tr>
                                        <th>编号</th>
                                        <th>姓名</th>
                                        <th>电话</th>
                                        <th>邮箱</th>
                                        <th>设为默认</th>
                                        <th>操作</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                    </tr>
                                    </tbody>
                            </table>
                            <div class="form-group row">
                                <label class="col-md-2 control-label">备注</label>
                                <div class="col-sm-8">
                                    <textarea class="form-control" name="content"></textarea>
                                </div>
                            </div>
                        </div>

                        <div class="form-group text-center">
                            <input class="btn green"  type="submit"  value="保存"/>&nbsp;&nbsp;&nbsp;
                            <a class="btn default" @click="cancel">取消</a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <div class="table-div">
        <div class="table-operator">
            <s:hasPermission name="scmMaterial/add">
                <button class="btn green pull-right" @click="create">新增供应商</button>
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
        var cid="#control";
        var $table = $(".table-body>table");
        var tb = $table.DataTable({
            ajax : {
                url : "scmSupplier/list_all",
                dataSrc : "data"
            },
            columns : [
                {
                    title : "序号",
                    data : "id",
                },
                {
                    title : "编码",
                    data : "supCode",
//					createdCell : function(td,tdData){
//						$(td).html("<span class='label label-primary'>"+tdData+"%</span>");
//					}
                },
                {
                    title : "类型",
                    data : "supplierType",
//					createdCell : function(td,tdData){
//						$(td).html("<span class='label label-primary'>"+tdData+"%</span>");
//					}
                }
                ,
                {
                    title : "公司全称",
                    data : "supName",
//					createdCell : function(td,tdData){
//						$(td).html("<span class='label label-primary'>"+tdData+"%</span>");
//					}
                },
                {
                    title : "别称",
                    data : "supAliasName",
                },
                {
                    title : "联系人 ",
                    data : "contact",
                },
                {
                    title : "电话",
                    data : "mobile",
                },
                {
                    title : "邮件",
                    data : "email",
                },
                {
                    title : "产品",
                    data : "supplierType",
                },
                {
                    title : "备注",
                    data : "note"
                },

                {
                    title : "操作",
                    data : "id",
                    createdCell:function(td,tdData,rowData,row){
                        var operator=[
                            <s:hasPermission name="scmMaterial/edit">
                            C.createEditBtn(rowData),
                            <s:hasPermission name="scmMaterial/delete">
                            C.createDelBtn(tdData,"scmMaterial/delete"),
                            </s:hasPermission>
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
                materialTypes: [
                    {
                        code:"INGREDIENTS" ,
                        name:"主料"
                    },
                    {
                        code:"ACCESSORIES" ,
                        name:"辅料"
                    },{
                        code:"SEASONING" ,
                        name:"调料"
                    }],
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
              
            }
       });
        C.vue=vueObj;
    }());
</script>



