<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div id="control">
    <div class="row form-div" v-show="showform">
        <div class="col-md-offset-3 col-md-6" style="text-align:center">
            <div class="portlet light bordered">
                <div class="portlet-title">
                    <div class="caption">
                        <span class="caption-subject bold font-blue-hoki">新增供应商</span>
                    </div>
                </div>
                <div class="portlet-body">
                    <form role="form" class="form-horizontal" action="{{parameter.id?'scmSupplier/modify':'scmSupplier/create'}}" @submit.prevent="save" style="text-align:center">
                        <input type="hidden" name="id" v-model="parameter.id" />
                        <div class="form-body">
                            <div class="form-group row">
                                <label class="col-md-2 control-label">供应商类型</label>
                                <div class="col-md-3">
                                <select class="bs-select form-control" name="supplierType" v-model="parameter.supplierType">
                                    <option disabled="" selected="" value="">请选择</option>
                                    <option  v-for="supplierType in supplierTypes" value="{{supplierType.code}}">
                                        {{supplierType.name}}
                                    </option>
                                </select>
                                </div>
                                <label class="col-md-2 control-label">编码</label>
                                <div class="col-md-3">
                                    <input type="text" class="form-control" name="supCode" v-model="parameter.supCode"
                                           readonly="readonly">
                                </div>
                            </div>
                            <div class="form-group row" >
                                <label class="col-md-2 control-label">公司名</label>
                                <div class="col-md-3">
                                    <input type="text" class="form-control" name="materialName" v-model="parameter.supName" required="required">
                                </div>

                                <label class="col-md-2 control-label">别名</label>
                                <div class="col-md-3">
                                    <input type="text" class="form-control" name="supAliasName" v-model="parameter.supAliasName" required="required">
                                </div>
                            </div>
                            <div class="form-group row">
                                <label class="col-md-2 control-label">序号</label>
                                <div class="col-md-3">
                                    <input type="text" class="form-control" name="version" v-model="parameter.version"
                                           required="required">
                                </div>
                            </div>

                            <div class="form-group row">
                                <label class="col-md-2 control-label">产品(可多选)</label>
                                <div class="col-md-7 checkbox-list" id="checkboxs">
                                    <label class="checkbox-inline" v-for="materialType in productTypes">
                                        <input type="checkbox" name="checkbox" v-model="parameter.materialTypes" value="{{materialType.id}}">
                                        <span>{{materialType.categoryName}}</span>
                                    </label>
                                </div>
                            </div>
                                <table class="table table-bordered" id="supplierContacts">
                                    <thead >
                                    <tr>
                                        <th>编号</th><th>姓名</th><th>电话</th><th>邮箱</th><th>设为默认</th><th>操作</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr v-for="(index,item) in parameter.supplierContacts">
                                        <td>{{index+1}}</td><td><input type="text" v-model="item.contact"></td><td><input type="text" v-model="item.mobile"></td><td><input type="text" v-model="item.email"></td>
                                        <td><input name="isTop" type="radio" v-model="isTop" :value="item.isTop" @click="supplierContactsRadio(item)"></td>
                                        <td><span class="btn btn-xs red" @click="removeArticleItem(item)">移除</span></td>
                                    </tr>
                                    </tbody>
                            </table>
                            <div class="form-group text-center">
                                <span class="btn green" @click="addSupplierContacts">添加联系资料</span>
                            </div>
                            <div class="form-group row">
                                <label class="col-md-2 control-label">备注</label>
                                <div class="col-sm-8">
                                    <textarea class="form-control" v-model="parameter.note" value="{{parameter.note?parameter.note:'内容'}}" name="note"></textarea>
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
            <s:hasPermission name="scmSupplier/add">
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
                    data : "id"
                },
                {
                    title : "编码",
                    data : "supCode"
                },
                {
                    title : "供应商类型",
                    data : "supplierTypeShow"
                }
                ,
                {
                    title : "公司全称",
                    data : "supName"
                },
                {
                    title : "别称",
                    data : "supAliasName"
                },
                {
                    title : "联系人 ",
                    data : "contact"
                },
                {
                    title : "电话",
                    data : "mobile"
                },
                {
                    title : "邮件",
                    data : "email"
                },
                {
                    title : "产品",
                    data : "materialTypes"
                },
                {
                    title : "备注",
                    data : "note"
                },
                {
                    title : "操作",
                    data : "id",
                    createdCell:function(td,tdData,rowData){
                        var operator=[
                            <s:hasPermission name="scmSupplier/create">
                            C.createEditBtn(rowData),
                            </s:hasPermission>
                            <s:hasPermission name="scmSupplier/delete">
                            C.createDelBtn(tdData,"scmSupplier/delete"),
                            </s:hasPermission>
                        ];
                        $(td).html(operator);
                    }
                }]
        });
        var C = new Controller(null,tb);
        var vueObj = new Vue({
            mixins:[C.formVueMix],
            el:"#control",
            data:{
                supplierTypes: [
                    {code:"1",name:"物料类"},
                    {code:"2",name:"服务类"},
                    {code:"3" , name:"工程类"}
                ],
                productTypes:[],//接收所有的产品分类
                isTop:'0',//单选框绑定
                parameter:{
                    supCode: "",
                    supplierType: "",
                    materialTypes:[],
                    materialIds:[],
                    supAliasName: "",
                    supName: "",
                    note:'',//备注
                    bankName: "",
                    bankAccount: "",
                    version: "",
                    topContact: "",
                    topMobile: "",
                    topEmail: "",
                    supplierContacts:[],//详情
                    supContactIds:[],
                }
            },
            methods:{
                addSupplierContacts:function () { //添加供应商联系资料
                    this.parameter.supplierContacts.push({contact:'',mobile:'',email:'',isTop:'1'});
                },
                removeArticleItem:function (data) { //移除供应商联系资料
                    this.parameter.supplierContacts.$remove(data);
                    this.parameter.supContactIds.push(data.id);
                },
                supplierContactsRadio:function(item){ //供应商联系默认值
                    $('#supplierContacts div').removeClass('radio');
                    this.parameter.supplierContacts.forEach(function(element) {
                        element.isTop='1';
                    });
                    item.isTop='0';
                    this.isTop='0';
                },
                closeForm:function(){ //关闭新增弹窗
                    this.showform=false;
                    this.parameter={};
                },
                create:function(){ //打开新增弹窗
                    var that = this;
                    $.get('scmCategory/list_all',function (jsonData) {
                        that.productTypes=jsonData.data;
                    });
                    this.parameter.materialTypes=[];
                    this.showform=true;
                    this.parameter={
                        supCode: "",
                        supplierType: "",
                        materialTypes:[],
                        materialIds:[],
                        supAliasName: "",
                        supName: "",
                        note:'',//备注
                        bankName: "",
                        bankAccount: "",
                        version: "",
                        topContact: "",
                        topMobile: "",
                        topEmail: "",
                        supplierContacts:[],//详情
                        supContactIds:[],
                    };
                },
                edit:function(model){ //编辑打开弹窗
                    var that = this;
                    this.parameter= model;
                    $.get('scmCategory/list_all',function (jsonData) {
                        that.productTypes=jsonData.data;
                    });
                    this.parameter.materialTypes=model.materialIds.split(',');
                    this.showform=true;
                    $('#supplierContacts div').removeClass('radio');
                },
                save:function(){//提交
                    var _this=this;
                    var saveObj={};
                    saveObj.id=this.parameter.id;
                    saveObj.supCode=this.parameter.supCode;
                    saveObj.supplierType=this.parameter.supplierType;
                    saveObj.materialTypes=this.parameter.materialTypes;
                    saveObj.supAliasName=this.parameter.supAliasName;
                    saveObj.supName=this.parameter.supName;
                    saveObj.note=this.parameter.note;
                    saveObj.bankName=this.parameter.bankName;
                    saveObj.bankAccount=this.parameter.bankAccount;
                    saveObj.supplierContacts=[];
                    saveObj.supContactIds =this.parameter.supContactIds;
                    var parSup=this.parameter.supplierContacts;
                    for(var i=0;i<parSup.length;i++){
                        saveObj.supplierContacts[i]={
                            id:parSup[i].id,
                            contact:parSup[i].contact,
                            mobile:parSup[i].mobile,
                            email:parSup[i].email,
                            isTop:parSup[i].isTop
                        }
                    }
                    var url='scmSupplier/modify';
                    saveObj.materialTypes=saveObj.materialTypes.toString();
                    if(!this.parameter.id) {
                        url='scmSupplier/create';
                        _this.parameter;
                    }
                    $.ajax({
                        type:"POST",
                        url:url,
                        contentType:"application/json",
                        datatype: "json",
                        data:JSON.stringify(saveObj),
                        success:function(data){ //成功后返回
                            C.systemButtonNo('success','成功');
                            _this.showform=false;
                        },
                        error: function(){ //失败后执行
                            C.systemButtonNo('error','失败');
                            _this.showform=false;
                        }
                    });
                }
              
            }
       });
        C.vue=vueObj;
    }());
</script>



