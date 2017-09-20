<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div id="control">
    <div class="row form-div" v-if="showform">
        <div class="col-md-offset-3 col-md-6" >
            <div class="portlet light bordered">
                <div class="portlet-title">
                    <div class="caption">
                        <span class="caption-subject bold font-blue-hoki">新增报价单</span>
                    </div>
                </div>

                <div class="portlet-body">
                    <form role="form" class="form-horizontal" action="{{m.id?'scmMaterial/modify':'scmMaterial/create'}}" @submit.prevent="save">
                        <input type="hidden" name="id" v-model="m.id" />
                        <div class="form-body">
                            <div class="form-group row">
                                <label class="col-md-2 control-label">报价单名称</label>
                                <div class="col-md-3">
                                    <input type="text" class="form-control" name="materialName" v-model="m.materialName"
                                           required="required">
                                </div>

                                <label class="col-md-2 control-label">类型</label>
                                <select name="categoryOneId" v-model="m.categoryOneId" class="col-md-3 border-radius" >
                                    <option  v-for="categoryOne in categoryOnes" value="{{categoryOne.id}}">
                                        {{categoryOne.categoryName}}
                                    </option>
                                </select>
                            </div>

                            <div class="form-group row" >
                                <label class="col-md-2 control-label">供应商</label>
                                <select name="categoryOneId" v-model="m.categoryOneId" class="col-md-3 border-radius" >
                                    <option  v-for="categoryOne in categoryOnes" value="{{categoryOne.id}}">
                                        {{categoryOne.categoryName}}
                                    </option>
                                </select>

                                <label class="col-md-2 control-label">联系人 </label>
                                <div  class="col-md-3">
                                        <input type="checkbox" name="printReceipt" v-model="m.printKitchen1" value = "1"> 配料
                                        <input type="checkbox" name="printKitchen" v-model="m.printKitchen2" value = "1"> 辅料
                                        <input type="checkbox" name="printKitchen" v-model="m.printKitchen3" value = "1"> 主料
                                </div>
                            </div>


                            <div class="form-group row">
                                <label class="col-md-2 control-label">产品原料</label>
                                <div class="col-md-3">
                                    <input type="text" class="form-control" name="priority" v-model="m.priority"
                                           required="required">
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
                <button class="btn green pull-right" @click="create">新建供应商</button>
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
                url : "scmMaterial/list_all",
                dataSrc : "data"
            },
            columns : [
                {
                    title : "报价单号",
                    data : "materialType",
                },
                {
                    title : "报价单名称",
                    data : "categoryOneName",
                },
                {
                    title: "开始日期",
                    data: "categoryTwoName",
                },
                {
                    title : "结束日期",
                    data : "categoryThirdName",
                },
                {
                    title : "物料类型",
                    data : "materialName",
                },
                {
                    title : "物料类别 ",
                    data : "priority",
                },
                {
                    title : "供应商名称",
                    data : "materialCode",
                },
                {
                    title : "联系人",
                    data : "materialCode",
                },
                {
                    title : "联系电话",
                    data : "materialCode",
                },
                {
                    title : "备注",
                    data : "materialCode",
                },
                {
                    title : "状态",
                    data : "materialCode",
                },
                {
                    title : "操作",
                    data : "id",
                    createdCell:function(td,tdData,rowData,row){
                        var operator=[
                            <s:hasPermission name="scmMaterial/delete">
                            C.createDelBtn(tdData,"scmMaterial/delete"),
                            </s:hasPermission>
                            <s:hasPermission name="scmMaterial/edit">
                            C.createEditBtn(rowData),
                            </s:hasPermission>
                        ];
                        $(td).html(operator);
                    }
                }],
        });
        //debugger
        var C = new Controller(null,tb);
        var vueObj = new Vue({
            mixins:[C.formVueMix],
            el:"#control",
            data:{
                checkedValues: [],
                categoryOnes:[],
                categoryTwos:[],
                categoryThirds:[],
                specLists:[],
                unitLists:[],
                provinceNameLists:[],
                cityNameLists:[],
                districtNameLists:[],

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
                closeForm:function(){
                    this.m={};
                    this.showform=false;
                    this.checkedValues=[];
                },
                create:function(){
                    var that = this;
                    this.m={
                        materialType: 'INGREDIENTS',
                        categoryOneId :'',
                        categoryTwoId :'',
                        categoryThirdId :'',
                        materialName :'',
                        materialCode :'',
                        specId	 :'',
                        priority :'',
                        unitId	 :'',
                        convertUnitId :'',
                        minConvertUnitId  :'',
                        provinceId :'',
                        cityId	 :'',
                        districtId :'',
                        description :''

                    };
                    that.unitLists = [];
                    $.post("scmUnit/list_type?type=1", null, function (data) {
                        that.unitLists = data.data
                        if(!that.m.unitId ) {
                            that.m.unitId = data.data[0].id;

                        }
                        if(that.m.convertUnitId){
                            that.m.convertUnitId = data.data[0].id;
                        }
                    });

                    this.specLists = [];
                    $.post("scmUnit/list_type?type=2", null, function (data) {
                        that.specLists = data.data;
                        if(!that.m.specId) {
                            that.m.specId = data.data[0].id;
                        }
                    });
                    this.categoryOnes = [];
                    $.post("scmCategory/list_categoryHierarchy?categoryHierarchy=1", null, function (data) {
                        that.categoryOnes = data.data;
                        if(!that.m.categoryOneId){
                            that.m.categoryOneId= data.data[0].id;
                        }
                    });

                    this.categoryTwos = [];
                    $.post("scmCategory/list_categoryHierarchy?categoryHierarchy=2", null, function (data) {
                        that.categoryTwos = data.data;
                        if(!that.m.categoryTwoId){
                            that.m.categoryTwoId= data.data[0].id;
                        }


                    });

                    this.categoryThirds = [];
                    $.post("scmCategory/list_categoryHierarchy?categoryHierarchy=3", null, function (data) {
                        that.categoryThirds = data.data;
                        if(!that.m.categoryThirdId){
                            that.m.categoryThirdId = data.data[0].id;
                        }

                    });

                    this.provinceNameLists = [];
                    $.post("province/list_province", null, function (data) {
                        that.provinceNameLists = data;
                        that.m.provinceId = data[0].id;

                    });

                    this.cityNameLists = [];
                    $.post("province/list_city", null, function (data) {
                        that.cityNameLists = data;
                        that.m.cityId= data[0].id;
                    });

                    this.districtNameLists = [];
                    $.post("province/list_district", null, function (data) {
                        that.districtNameLists = data;
                        that.m.districtId = data[0].id;
                    });
                    this.showform=true;

                },
                edit:function(model){
                    var that = this;
                    this.m= model;
                    this.openForm();


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
