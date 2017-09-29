<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div id="control">
    <div class="row form-div" v-show="showform">
        <div class="col-md-offset-3 col-md-6" >
            <div class="portlet light bordered">
                <div class="portlet-title">
                    <div class="caption">
                        <span class="caption-subject bold font-blue-hoki">新增报价单</span>
                    </div>
                </div>
                <div class="portlet-body">
                    <form role="form" class="form-horizontal" action="{{parameter.id?'scmMaterial/modify':'scmMaterial/create'}}" @submit.prevent="save">
                        <input type="hidden" name="id" v-model="parameter.id" />
                        <div class="form-body">
                            <div class="form-group row">
                                <label class="col-md-2 control-label">报价单名称</label>
                                <div class="col-md-3">
                                    <input type="text" class="form-control" name="priceName" v-model="parameter.priceName"
                                           required="required">
                                </div>
                                <label class="col-md-2 control-label">供应商类型</label>
                                <div class="col-md-3">
                                    <select class="bs-select form-control" name="supplierType" v-model="supplierType">
                                        <option  v-for="supplierType in supplierTypes" value="{{supplierType.name}}">
                                            {{supplierType.name}}
                                        </option>
                                    </select>
                                </div>
                            </div>
                            <div class="form-group row" >
                                <label class="col-md-2 control-label">供应商</label>
                                <div class="col-md-3">
                                <select name="categoryOneId" v-model="parameter.supplierId" class="bs-select form-control">
                                    <%--<option  v-for="supName in supNames" value="{{supName.id}}" v-if="supplierType==supName.supplierType">--%>
                                    <option  v-for="supName in supNames" value="{{supName.id}}">
                                        {{supName.supAliasName}}
                                    </option>
                                </select>
                                </div>
                                <label class="col-md-2 control-label">联系人 </label>
                                <div class="col-md-3">
                                <select name="categoryOneId" v-model="parameter.contactId" class="bs-select form-control" >
                                    <option  v-for="contact in contacts" value="{{contact.id}}" v-if="parameter.supplierId==contact.id">
                                        {{contact.contact}}
                                    </option>
                                </select>
                                </div>
                                <%--<div  class="checkbox-list">--%>
                                    <%--<input type="checkbox" name="printReceipt" v-model="parameter.printKitchen1" value = "1"> 配料--%>
                                    <%--<input type="checkbox" name="printKitchen" v-model="parameter.printKitchen2" value = "1"> 辅料--%>
                                    <%--<input type="checkbox" name="printKitchen" v-model="parameter.printKitchen3" value = "1"> 主料--%>
                                <%--</div>--%>
                            </div>
                                <div class="form-group row">
                                    <label for="inputEmail3" class="col-sm-2 control-label">开始时间：</label>
                                    <div class="col-sm-3">
                                        <input type="text" class="form-control form_datetime" id="beginDate" v-model="parameter.startEffect" name="beginDate" readonly="readonly">
                                    </div>
                                    <label for="inputPassword3" class="col-sm-2 control-label">结束时间：</label>
                                    <div class="col-sm-3">
                                        <input type="text" class="form-control form_datetime" id="endDate" v-model="parameter.endEffect" name="endDate" readonly="readonly">
                                    </div>
                                </div>

                            <div class="form-group row">
                                <label for="inputEmail3" class="col-sm-2 control-label">备注</label>
                                <div class="col-sm-8">
                                    <input type="text" class="form-control" name="" v-model="parameter.remark">
                                </div>
                            </div>
                            <div class="form-group row">
                                <label class="col-md-2 control-label">产品原料</label>
                                <div class="col-md-3">
                                    <input class="btn btn-default" type="button" value="添加原料" @click="closeTreeView"/>
                                </div>
                            </div>
                        </div>
                        <div>
                            <table class="table table-bordered">
                                <thead><tr>
                                    <th>类型</th><th>一级类别</th><th>二级类别</th>
                                    <th>品牌名</th><th>材料名</th><th>编码</th>
                                    <th>规格</th><th>产地</th><th>单价</th><th>操作</th>
                                </tr></thead>
                                <tbody>

                                <tr v-for="(index,item) in parameter.mdSupplierPriceDetailDoList">
                                    <td>{{item.materialType}}</td>
                                    <td>{{item.categoryOneName}}</td>
                                    <td>{{item.categoryTwoName}}</td>
                                    <td>{{item.categoryThirdName}}</td>
                                    <td>{{item.name}}</td>
                                    <td>{{item.materialCode}}</td>
                                    <td>{{item.measureUnit+item.unitName+"/"+item.specName}}</td>
                                    <td>{{item.provinceName+item.cityName+item.districtName}}</td>
                                    <td><input type="text" v-model="item.purchasePrice"></td>
                                    <td><button class="btn btn-xs red" @click="removeArticleItem(item)">移除</button></td>
                                </tr>
                                </tbody>
                            </table>
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
    <!--树状图-->
    <div class="row form-div" v-show="treeView">
        <div class="col-md-offset-3 col-md-6" style="background: #FFF;">
            <div class="text-center" style="padding: 20px 0">
                <span class="caption-subject bold font-blue-hoki">添加原材料</span>
            </div>
            <div class="row">
                <div class="col-md-12">
                    <div class="modal-body">
                        <input type="input" class="form-control" id="input-check-node" placeholder="请输入原材料名称" value="">
                    </div>
                    <div id="treeview-checkable" class="" style="height: 500px;overflow: auto;"></div>
                </div>
            </div>
            <div class="text-center" style="padding: 20px 0">
                <input class="btn green"  @click="bomRawMaterialSub"  value="保存"/>&nbsp;&nbsp;&nbsp;
                <a class="btn default" @click="cancelTreeView" >取消</a>
            </div>
        </div>
    </div>
    <!--树状图结束-->
    <!--查看详情-->
    <div class="row form-div" v-show="details">
        <div class="col-md-offset-3 col-md-6" style="background: #FFF;">
            <div class="text-center" style="padding: 20px 0">
                <span class="caption-subject bold font-blue-hoki">查看详情</span>
            </div>
            <div class="row">
                <div class="col-md-12">
                    <div class="form-group row">
                        <label class="col-md-2 control-label">报价单号</label>
                        <div class="col-md-4">
                            {{detailsArr.priceNo}}
                        </div>
                        <label class="col-md-2 control-label">有效期</label>
                        <div class="col-md-4">
                            {{detailsArr.startEffect}}--{{detailsArr.endEffect}}
                        </div>
                    </div>
                    <div class="form-group row">
                        <label class="col-md-2 control-label">类型</label>
                        <div class="col-md-4">
                            {{detailsArr.materialTypes}}
                        </div>
                        <label class="col-md-2 control-label">供应商</label>
                        <div class="col-md-4">
                            {{detailsArr.supName}}
                        </div>
                    </div>
                    <div class="form-group row">
                        <label class="col-md-2 control-label">联系人</label>
                        <div class="col-md-4">
                            {{detailsArr.contact}}
                        </div>
                        <label class="col-md-2 control-label">备注</label>
                        <div class="col-md-4">
                            {{detailsArr.remark}}
                        </div>
                    </div>
                    <div class="form-group row">
                        <table class="table table-bordered" >
                            <thead>
                            <tr>
                                <th>类型</th>
                                <th>一级类别</th>
                                <th>二级类别</th>
                                <th>品牌名</th>
                                <th>材料名</th>
                                <th>编码</th>
                                <th>规格</th>
                                <th>产地</th>
                                <th>单价</th>

                            </tr>
                            </thead>
                            <tbody>
                            <tr v-for="item in detailsArr.mdSupplierPriceDetailDoList">
                                <td>{{item.materialType}}</td>
                                <td>{{item.categoryOneName}}</td>
                                <td>{{item.categoryTwoName}}</td>
                                <td>{{item.categoryThirdName}}</td>
                                <td>{{item.name}}</td>
                                <td>{{item.materialCode}}</td>
                                <td>{{item.measureUnit+item.unitName+"/"+item.specName}}</td>
                                <td>{{item.provinceName+item.cityName+item.districtName}}</td>
                                <td>{{item.purchasePrice}}</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
            <div class="text-center" style="padding: 20px 0">
                <a class="btn default" @click="detailsCli" v-if="detailsBtn">取消</a>
            </div>
            <div class="text-center" style="padding: 20px 0" v-if="approveBtn">
                <a class="btn default" @click="approveCli1" >驳回</a>
                <a class="btn blue pull-center" @click="approveCli2" >批准</a>
            </div>
        </div>
    </div>
    <!--查看详情-->
    <div class="table-div">
        <div class="table-operator">
            <s:hasPermission name="scmSupplerPrice/add">
                <button class="btn green pull-right" @click="create">新建报价单</button>
            </s:hasPermission>
        </div>
        <div class="clearfix"></div>
        <div class="table-filter"></div>
        <div class="table-body">
            <table class="table table-striped table-hover table-bordered "></table>
        </div>
    </div>
</div>

<!-- <!-- 日期框 -->
<script src="assets/global/plugins/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
<script src="assets/global/plugins/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js"></script>
<!--树状图-->
<script src="assets/treeview/js/bootstrap-treeview.js"></script>
<script>
    (function(){
        var cid="#control";
        var $table = $(".table-body>table");

        var tb = $table.DataTable({
            ajax : {
                url : "scmSupplerPrice/list_all",
                dataSrc : "data"
            },
            columns : [
                {
                    title : "报价单号",
                    data : "priceNo",
                },
                {
                    title : "报价单名称",
                    data : "priceName",
                },
                {
                    title: "开始日期",
                    data: "startEffect",
                },
                {
                    title : "结束日期",
                    data : "endEffect",
                },
                {
                    title : "物料类型",
                    data : "materialTypes",
                },
                {
                    title : "物料类别 ",
                    data : "materialSizes",
                },
                {
                    title : "供应商名称",
                    data : "supName",
                },
                {
                    title : "联系人",
                    data : "contact",
                },
                {
                    title : "联系邮箱",
                    data : "email",
                },
                {
                    title : "备注",
                    data : "remark",
                },
                {
                    title : "状态",
                    data : "supStatus",
                },
                {
                    title : "操作",
                    data : "id",
                    createdCell:function(td,tdData,rowData){
                        var operator=[
                            <s:hasPermission name="scmSupplerPrice/approve">
                             C.createApproveBtn(rowData),
                            </s:hasPermission>
                            <s:hasPermission name="scmSupplerPrice/showDetails">
                            C.findBtn(rowData),
                            </s:hasPermission>
                        ];
                        $(td).html(operator);
                    }
                },
                ],
        });
        var C = new Controller(null,tb);
        var vueObj = new Vue({
            mixins:[C.formVueMix],
            el:"#control",
            data:{
                details:false,//查看详情
                detailsBtn:false,//查看详情返回按钮
                approveBtn:false,//查看详情（审核）-审核按钮
                showform:false,//弹窗
                treeView:false,//树状图
                bomRawMaterial:[],//树状图原材料
                detailsArr:'',//查看详情对象
                supplierTypes: [ //供应商类型数组
                    {code:"1",name:"物料类"},
                    {code:"2",name:"服务类"},
                    {code:"3" , name:"工程类"}
                ],
                supplierType:'物料类',//供应商类型
                supNames:[],//供应商数组
                contacts:[],//联系人数组

                parameter:{
                    supplierId:'',//供应商id
                    startEffect:'',//生效日期
                    endEffect:'',//失效效日期
                    contactId:4,//联系人id
                    remark:'',//备注
                    priceName:'',//报价单名称
                    mdSupplierPriceDetailDoList:[

                    ],

                }
            },
            methods:{
                closeForm:function(){ //关闭新增弹窗
                    this.showform=false;
                },
                create:function(){ //打开新增弹窗
                    this.showform=true;
                },
                closeTreeView:function () { //添加原料打开
                    this.treeView=true;
                },
                approve:function (data) { //开始审核
                    this.details=true;
                    this.detailsArr=data;
                    this.approveBtn=true;
                },
                approveCli1:function () { //驳回审核
                    this.details=false;
                    this.approveBtn=false;
                    C.systemButton('scmSupplerPrice/approve',{id:this.detailsArr.id,supStatus:'13'},['驳回成功','驳回失败']);
                },
                approveCli2:function () { //批准审核
                    this.details=false;
                    this.approveBtn=false;
                    C.systemButton('scmSupplerPrice/approve',{id:this.detailsArr.id,supStatus:'12'},['审核成功','审核失败']);
                },
                showDetails:function (data) { //查看详情
                    this.details=true;
                    this.detailsArr=data;
                    this.detailsBtn=true;
                },
                detailsCli:function () { //关闭查看详情
                    this.details=false;
                    this.detailsBtn=false;
                },
                cancelTreeView:function () { //添加原料关闭
                    this.treeView=false;
                },
                removeArticleItem: function (mealItem) { //移除
                    this.parameter.mdSupplierPriceDetailDoList.$remove(mealItem);
                },
                bomRawMaterialSub:function () { //添加原材料保存
                    this.treeView=false;
                    this.parameter.mdSupplierPriceDetailDoList.push.apply(this.parameter.mdSupplierPriceDetailDoList,this.bomRawMaterial);//合并数组
                    console.log(this.parameter.mdSupplierPriceDetailDoList);
                },
                save:function(e){
                    var _this=this;
                    var saveObj=[];
                    var parSup=this.parameter.mdSupplierPriceDetailDoList;
                    for(var i=0;i<parSup.length;i++){
                        saveObj[i]={
                            materialId:parSup[i].id,
                            materialCode:parSup[i].materialCode,
                            purchasePrice:parSup[i].purchasePrice,
                        }
                    }
                    _this.parameter.mdSupplierPriceDetailDoList=saveObj;
                    console.log(_this.parameter);
                    $.ajax({
                        type:"POST",
                        url:'scmSupplerPrice/create',
                        contentType:"application/json",
                        datatype: "json",
                        data:JSON.stringify(_this.parameter),
                        beforeSend:function(){ //请求之前执行
                            _this.showform=false;
                        },
                        success:function(data){ //成功后返回
                            console.log(data);
                        },
                        error: function(){ //失败后执行
                        }
                    });
//                    var that = this;
//                    var formDom = e.target;
//                    C.ajaxFormEx(formDom,function(){
//                        that.cancel();
//                        tb.ajax.reload();
//                    });
                },
            },
            ready:function(){//钩子函数加载后
                var _this=this;
                $.get('scmSupplier/list_all',function (jsonData) { //供应商查询
                    var data=jsonData.data;
                    _this.supNames=data;//供应商
                        for(var i=0;i<data.length;i++){
                            if(data[i].supplierContacts){
                                _this.contacts=_this.contacts.concat(data[i].supplierContacts);
                            }
                        }
                });
                $.get('scmCategory/query',function (jsonData) { //加载树状图
                    var defaultData=jsonData.data;
                    for(var i=0;i<defaultData.length;i++){
                        if (defaultData[i].twoList) {
                            for(var j=0;j<defaultData[i].twoList.length;j++){
                                if (defaultData[i].twoList) {
                                    defaultData[i].twoList[j].twoList = defaultData[i].twoList[j].threeList;
                                    delete defaultData[i].twoList[j].threeList;
                                    if(defaultData[i].twoList[j].twoList){
                                        for(var k=0;k<defaultData[i].twoList[j].twoList.length;k++){
                                            defaultData[i].twoList[j].twoList[k].twoList = defaultData[i].twoList[j].twoList[k].materialList;
                                            delete defaultData[i].twoList[j].twoList[k].materialList;
                                            if(defaultData[i].twoList[j].twoList[k].twoList){
                                                for(var l=0;l<defaultData[i].twoList[j].twoList[k].twoList.length;l++){
                                                    defaultData[i].twoList[j].twoList[k].twoList[l].name = defaultData[i].twoList[j].twoList[k].twoList[l].materialName;
                                                    delete defaultData[i].twoList[j].twoList[k].twoList[l].materialName;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    var $checkableTree = $('#treeview-checkable').treeview({
                        data: defaultData,
                        showIcon: true,
                        showCheckbox: true,
                        onNodeChecked: function(event, data) {
                            if(data){
                                //Vue.set(vueObj.bomRawMaterial,vueObj.bomRawMaterial.length,data);
                                Vue.set(vueObj.parameter.mdSupplierPriceDetailDoList,vueObj.parameter.mdSupplierPriceDetailDoList.length,data);
                                //console.log(vueObj.bomRawMaterial);
                            }
                        },
                        onNodeUnchecked: function (event, node) {
                            //vueObj.bomRawMaterial= vueObj.bomRawMaterial.filter(o => o.id != node.id);
                            vueObj.parameter.mdSupplierPriceDetailDoList= vueObj.parameter.mdSupplierPriceDetailDoList.filter(o => o.id != node.id);
                        }
                    });
                    var findCheckableNodess = function() {
                        return $checkableTree.treeview('search', [ $('#input-check-node').val(), { ignoreCase: false, exactMatch: false } ]);
                    };
                    var checkableNodes = findCheckableNodess();
                    // Check/uncheck/toggle nodes
                    $('#input-check-node').on('keyup', function (e) {
                        checkableNodes = findCheckableNodess();
                        $('.check-node').prop('disabled', !(checkableNodes.length >= 1));
                    });
                })
            },
            //vue实例化之后执行的方法
            created : function(){
                //初始化多选框按钮 和 时间插件
                //时间默认值
                $('.form_datetime').val(new Date().format("yyyy-mm-dd HH:mm:ss"));
                //this.initTime();
                $('.form_datetime').datetimepicker({
                    endDate : new Date(),
                    //minView : "month",
                    //maxView : "month",
                    autoclose : true,//选择后自动关闭时间选择器
                    todayBtn : true,//在底部显示 当天日期
                    todayHighlight : true,//高亮当前日期
                    format : "yyyy-mm-dd HH:mm:ss",
                    //startView : "month",
                    language : "zh-CN"
                });

            },
        });
        C.vue=vueObj;
    }());
  

</script>
