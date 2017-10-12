<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<style>
    #tableBodyList table tr th:first-of-type,#tableBodyList table tr td:first-of-type{display: none;}
    th,td{text-align: center;}
    #tableBodyLists th,#tableBodyLists td{line-height:2.5;}
</style>
<div id="control">
    <div class="row form-div" v-show="showform">
        <div class="col-md-offset-3 col-md-6" >
            <div class="portlet light bordered">
                <div class="portlet-title">
                    <div class="caption">
                        <span class="caption-subject bold font-blue-hoki">新增BOM</span>
                    </div>
                </div>

                <div class="portlet-body">
                        <input type="hidden" name="id" v-model="parameter.id" />
                        <div class="form-body">
                            <div class="form-group row">
                                <label class="col-md-2 control-label">菜品类别</label>
                                <div class="col-md-3">
                                    <select name="productCategory" v-model="parameter.articleFamilyId"  class="bs-select form-control" @change='changeType1' >
                                        <option disabled selected value>请选择</option>
                                        <option  v-for="articleFamily in articleFamilyIdArr" value="{{articleFamily.articleFamilyId}}">
                                            {{articleFamily.name}}
                                        </option>
                                    </select>
                                </div>

                                <label class="col-md-2 control-label">菜品名称</label>
                                <div class="col-md-3">
                                <select name="articleId"  v-model="parameter.articleId"  class="bs-select form-control" @change='changeType2'>
                                    <option disabled selected value>请选择</option>
                                    <option  v-for="productName in productNameArr" value="{{productName.articleId}}" v-if="parameter.articleFamilyId == productName.articleFamilyId">
                                        {{productName.name}}
                                    </option>
                                </select>
                                </div>
                            </div>

                            <div class="form-group row" >
                                <label class="col-md-2 control-label">菜品编码</label>
                                <div class="col-md-8">
                                {{parameter.articleId}}
                                </div>

                            </div>
                            <div class="form-group row">
                                <label class="col-md-2 control-label">版本号</label>
                                <div class="col-md-3">
                                    <input type="text" class="form-control" name="priority" v-model="parameter.version" required="required">
                                </div>

                                <label class="col-md-2 control-label">序号</label>
                                <div class="col-md-3">
                                    <input type="text" class="form-control" name="priority" v-model="parameter.priority" required="required">
                                </div>
                            </div>

                            <div class="form-group row">
                                <label class="col-md-2 control-label">产品原料</label>
                                <div class="col-md-3">
                                    <input class="btn btn-default" type="button" value="添加原料" @click="closeTreeView"/>
                                </div>
                                <label class="col-md-2 control-label">计量单位</label>
                                <div class="col-md-3">
                                    <input type="text" class="form-control" name="materialName" v-model="parameter.measurementUnit" disabled>
                                </div>
                            </div>
                        </div>
                        <div style="max-height: 300px;overflow: auto;">
                            <table class="table table-bordered" id="yuanliaolist">
                                <thead><tr>
                                    <th>行号</th><th>原料编码</th><th>原料类型</th>
                                    <th>原料名称</th><th>规格</th>
                                    <th>所需最小单位数量</th><th>操作</th>
                                </tr></thead>
                                <tbody>
                                <tr v-for="(index,item) in parameter.bomDetailDoList">
                                    <td>{{index+1}}</td><td>{{item.materialCode}}</td><td v-text="((((item.materialType=='INGREDIENTS')?'主料':item.materialType)=='ACCESSORIES')?'辅料':'其他')"></td><td>{{item.materialName}}{{item.name}}</td><td>{{item.minMeasureUnit}}{{item.unitName}}/{{item.specName}}</td>
                                    <td><input type="text" v-model="item.materialCount" value="{{(item.materialCount?item.materialCount:1)}}" ></td><td><button class="btn btn-xs red" @click="removeArticleItem(item)">移除</button></td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                        <div class="form-group text-center">
                            <input class="btn green"  type="submit"  @click="save" value="保存"/>&nbsp;&nbsp;&nbsp;
                            <a class="btn default" @click="cancel" >取消</a>
                        </div>
                </div>
            </div>
        </div>
    </div>
    <!--树状图-->
    <div class="row form-div" v-show="treeView">
        <div class="col-md-offset-3 col-md-6" style="background:#FFF;">
            <div class="text-center" style="padding: 20px 0">
                <span class="caption-subject bold font-blue-hoki">添加原材料</span>
            </div>
            <div class="row">
                <div class="col-md-12">
                    <div class="modal-body">
                        <input type="input" class="form-control" id="search_ay" placeholder="请输入原材料名称">
                    </div>
                    <div id="assignTree" style="height: 300px;overflow: auto;"></div>
                    <div id="treeview-checkable" class="" style="height: 500px;overflow: auto;display: none"></div>
                </div>
                <div class="col-md-6" style="display: none;">
                    <div id="checkable-output">
                        <table class="table table-bordered">
                            <thead><tr>
                                <th>行号</th><th>原料编码</th><th>原料类型</th><th>原料名称</th>
                                <th>规格</th><th>最小单位</th><th>所需最小单位数量</th><th>操作</th>
                            </tr></thead>
                            <tbody>
                        <tr v-for="(index,item) in bomRawMaterial">
                            <td>{{index+1}}</td><td>{{item.materialCode}}</td><td>{{item.materialType}}</td><td>{{item.materialName}}</td><td>{{item.unitName}}</td><td>{{item.minMeasureUnit}}</td>
                            <td><input type="text" v-model="materialCount" value="item.materialCount"></td><td><button class="btn btn-xs red">移除</button></td>
                        </tr>
                        </tbody>
                        </table>
                    </div>
                </div>
            </div>
            <div class="text-center" style="padding: 20px 0">
                <input class="btn green"  @click="bomRawMaterialSub"  value="保存"/>&nbsp;&nbsp;&nbsp;
                <a class="btn default" @click="cancelTreeView" >取消</a>
            </div>
        </div>

    </div>
    <div class="table-div">
        <div class="table-operator">
            <s:hasPermission name="scmBom/add">
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
<!--树状图-->
<%--<link href="assets/treeview/bower_components/bootstrap/dist/css/bootstrap.css" rel="stylesheet">
<script src="assets/treeview/bower_components/jquery/dist/jquery.js"></script>--%>
<%--<script src="assets/treeview/js/bootstrap-treeview.js"></script>--%>
<link href="assets/treeview/themes/default/style.min.css" rel="stylesheet">
<script src="assets/treeview/js/jstrestwo.js"></script>

<script>
    (function(){
        var tableBodyList = $("#tableBodyList>table");
        var tb = tableBodyList.DataTable({
            ajax : {
                url : "scmBom/list_all",
                dataSrc : "data",
                type : "post",
                data : function(data) {
                    return data;
                },
            },
            ordering: false,//取消上下排序
            columns : [
                {
                    data : "bomDetailDoList",
                    createdCell : function(td,tdData){
                        var html='<tr><th>行号</th><th>原料编码</th><th>原料类型</th><th>原料名称</th><th>规格</th><th>最小单位</th><th>所需最小单位数量</th></tr>';

                        for(var i=0;i<tdData.length;i++){
                            switch(tdData[i].materialType){
                                case 'INGREDIENTS':tdData[i].materialType='主料';break;
                                case 'ACCESSORIES':tdData[i].materialType='辅料';break;
                                default:tdData[i].materialType='其他';break;
                            }
                            html+='<tr><td>'+(i+1)+'</td><td>'+tdData[i].materialCode+'</td><td>'+tdData[i].materialType+'</td><td>'+tdData[i].materialName+'</td><td>'+tdData[i].minMeasureUnit+tdData[i].unitName+'/'+tdData[i].specName+'</td><td>'+tdData[i].minMeasureUnit+'/'+tdData[i].minUnitName+'</td><td>'+tdData[i].materialCount+'</td></tr>';
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
                    data : "articleId"
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
                    createdCell:function(td,tdData,rowData){
                        var operator=[
                            <s:hasPermission name="scmBom/edit">
                            C.createEditBtn(rowData),
                            </s:hasPermission>
                            <s:hasPermission name="scmBom/delete">
                            C.createDelBtn(tdData,"scmBom/delete"),
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
                treeView:false,//树状图

                bomRawMaterial:[],//bom原材料

                articleFamilyIdArr:[],//菜品类别选项
                productNameArr:[],//菜品名称选项
                parameter:{
                    id:'',//id
                    priority:'',//序号
                    bomCode:'',//bom清单的编码
                    productCode:'',//菜品编码
                    version:'',//版本号
                    productCategory:'',//菜品类别
                    productName:'',//菜品名称
                    articleFamilyId:'',//菜品类别id
                    articleId:'',//菜品id
                    measurementUnit:'',//计量单位
                    size:'',//原料种类
                    bomDetailDoList:[],//bom原材料显示
                    bomDetailDeleteIds:[],//删除的list节点
                }
            },
            methods:{
                changeType1: function (ele) {
                    this.parameter.productCategory = $(ele.target).find("option:selected").text();
                    this.parameter.articleFamilyId = ele.target.value;

                },
                changeType2: function (ele) {
                    console.log(this.productNameArr);
                    this.parameter.productName = $(ele.target).find("option:selected").text();
                    this.parameter.articleId = ele.target.value;
                    for(var i=0;i<this.productNameArr.length;i++){
                        if(ele.target.value==this.productNameArr[i].articleId){
                            this.parameter.measurementUnit=this.productNameArr[i].unit;
                            break;
                        }
                    }
                },
                create:function(){ //打开新增弹窗
                    this.parameter= {
                        bomDetailDoList:[],//bom原材料显示
                        bomDetailDeleteIds:[],//删除的list节点
                        bomCode:'',
                        productCode:'',
                        priority:'1',
                        measurementUnit:'',
                        articleFamilyId:'',
                        articleId:'',
                    };
                    this.showform=true;
                },
                closeForm:function(){ //关闭新增弹窗
                    this.showform=false;
                    this.parameter.productCategory='';//菜品类别
                    this.parameter.productName='';//菜品名称
                },
                edit:function(model){ //编辑打开弹窗
                    var that=this;
                    var articleIdZhi=model.articleId;
                    this.parameter= model;
                    this.parameter.bomDetailDeleteIds=[];
                    this.showform=true;
                    this.parameter.articleId='';
                    setTimeout(function () {
                        that.tableBodyListsShow=false;
                        that.parameter.articleId=articleIdZhi;
                    },100);
                },
                save:function(e){ //新增and编辑保存
                    var _this=this;
                    var savearr=[];
                    debugger
                    for(var i=0;i<_this.parameter.bomDetailDoList.length;i++){
                        savearr[i]={
                            id:_this.parameter.bomDetailDoList[i].id,
                            materialId:_this.parameter.bomDetailDoList[i].idTwo,
                            minMeasureUnit:_this.parameter.bomDetailDoList[i].minMeasureUnit,
                            unitName:_this.parameter.bomDetailDoList[i].unitName,
                            materialName:_this.parameter.bomDetailDoList[i].name,
                            specName:_this.parameter.bomDetailDoList[i].specName,
                            materialCode:_this.parameter.bomDetailDoList[i].materialCode,
                            lossFactor:_this.parameter.bomDetailDoList[i].lossFactor,
                            actLossFactor:_this.parameter.bomDetailDoList[i].actLossFactor,
                            materialCount:_this.parameter.bomDetailDoList[i].materialCount,
                            measurementUnit:_this.parameter.bomDetailDoList[i].measurementUnit,
                        }
                    }
                    _this.parameter.bomDetailDoList=savearr;
                    var url='scmBom/modify';
                    if(!this.parameter.id) {
                        url='scmBom/create';
                        _this.parameter;
                        delete _this.parameter.bomDetailDeleteIds;
                    }
                    $.ajax({
                        type:"POST",
                        url:url,
                        contentType:"application/json",
                        datatype: "json",//"xml", "html", "script", "json", "jsonp", "text".//返回数据的格式
                        data:JSON.stringify(_this.parameter),
                        beforeSend:function(){ //请求之前执行
                            console.log("请求之前执行");
                            _this.showform=false;
                        },
                        success:function(data){ //成功后返回
                            console.log(data);
                            C.systemButtonNo('success','成功');
                        },
                        error: function(){ //失败后执行
                            C.systemButtonNo('error','失败');
                        }
                    });
                    this.parameter= {
                        bomDetailDoList:[],//bom原材料显示
                        bomDetailDeleteIds:[],//删除的list节点
                        bomCode:'',
                        productCode:'',
                        measurementUnit:''
                    };
                },
                bomRawMaterialSub:function () { //添加原料保存
                    var originaldata=$('#assignTree').jstrestwo().get_bottom_checked(true);//拿到树状图中的数组
                    for(var i=0;i<originaldata.length;i++){
                        if(originaldata[i].original.materialType){
                            this.bomRawMaterial.push(originaldata[i].original);
                        }

                    }
                    for(var i=0;i<this.bomRawMaterial.length;i++){
                        this.bomRawMaterial[i].materialId=this.bomRawMaterial[i].id;
                        delete this.bomRawMaterial[i].id;
                    }
                    this.parameter.bomDetailDoList.push.apply(this.parameter.bomDetailDoList,this.bomRawMaterial);//合并数组
                    for(var i=0;i<this.parameter.bomDetailDoList.length;i++){
                        switch(this.parameter.bomDetailDoList[i].materialType){
                            case 'INGREDIENTS':this.parameter.bomDetailDoList[i].materialType='主料';break;
                            case 'ACCESSORIES':this.parameter.bomDetailDoList[i].materialType='辅料';break;
                            default:this.parameter.bomDetailDoList[i].materialType='其他';break;
                        }
                    }
                    this.treeView=false;
                    $('#assignTree').jstrestwo('deselect_all');//关闭所有选中状态
                },
                closeTreeView:function () { //添加原料打开
                    this.treeView=true;
                },
                cancelTreeView:function () { //添加原料关闭
                    this.treeView=false;
                },
                removeArticleItem: function (mealItem) { //移除
                    this.parameter.bomDetailDoList.$remove(mealItem);
                    console.log(mealItem);
                    this.parameter.bomDetailDeleteIds.push(mealItem.id);
                },
            },
            ready:function(){//钩子加载后
                var that = this;
                $('#tableBodyList').on('click','table tbody tr',function () {//显示详情
                    that.tableBodyListsShow=true;
                    $('#tableBodyLists table').html('');
                    $('#tableBodyLists table').html($(this).find('.bomDetailDoList').html());
                });
                $.get('articlefamily/list_all',function (data) { //菜品类别选项
                    for(var i=0;i<data.length;i++){
                        that.articleFamilyIdArr.push({articleFamilyId:data[i].id , name:data[i].name});
                    }
                });
                $.get('article/list_all',function (data) { //菜品名称选项
                    console.log(data);
                    for(var i=0;i<data.length;i++){
                        that.productNameArr.push({articleId:data[i].id, name:data[i].name,articleFamilyId:data[i].articleFamilyId,unit:data[i].unit});
                    }
                })
            },
        });
        C.vue=vueObj;
        $.get('scmCategory/query',function (jsonData) {
            var defaultData=jsonData.data;
            for(var i=0;i<defaultData.length;i++){
                defaultData[i].idTwo = defaultData[i].id;
                delete defaultData[i].id;
                if (defaultData[i].twoList) {
                    for(var j=0;j<defaultData[i].twoList.length;j++){
                        defaultData[i].twoList[j].idTwo = defaultData[i].twoList[j].id;
                        delete defaultData[i].twoList[j].id;
                        if (defaultData[i].twoList) {
                            defaultData[i].twoList[j].twoList = defaultData[i].twoList[j].threeList;
                            delete defaultData[i].twoList[j].threeList;
                            if(defaultData[i].twoList[j].twoList){
                                for(var k=0;k<defaultData[i].twoList[j].twoList.length;k++){
                                    defaultData[i].twoList[j].twoList[k].idTwo = defaultData[i].twoList[j].twoList[k].id;
                                    delete defaultData[i].twoList[j].twoList[k].id;
                                    defaultData[i].twoList[j].twoList[k].twoList = defaultData[i].twoList[j].twoList[k].materialList;
                                    delete defaultData[i].twoList[j].twoList[k].materialList;
                                    if(defaultData[i].twoList[j].twoList[k].twoList){
                                        for(var l=0;l<defaultData[i].twoList[j].twoList[k].twoList.length;l++){
                                            defaultData[i].twoList[j].twoList[k].twoList[l].idTwo = defaultData[i].twoList[j].twoList[k].twoList[l].id;
                                            delete defaultData[i].twoList[j].twoList[k].twoList[l].id;
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
            $('#assignTree').jstrestwo(
                {'plugins':["wholerow","checkbox","search"],
                    'core' :{
                        'data':defaultData
                    }
                }
            );
            //输入框输入定时自动搜索
            var to = false;
            $('#search_ay').keyup(function () {
                if(to) {
                    clearTimeout(to);
                }
                to = setTimeout(function () {
                    $('#assignTree').jstrestwo(true).search($('#search_ay').val());

                }, 250);
            });
        })
    }());
</script>