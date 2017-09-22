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
                                    <input class="btn btn-default" type="button" value="添加原料" @click="closeTreeView"/>
                                </div>
                            </div>
                        </div>
                        <div>
                            <table class="table table-bordered">
                                <thead><tr>
                                    <th>行号</th><th>原料编码</th><th>原料类型</th>
                                    <th>原料名称</th><th>规格</th><th>最小单位</th>
                                    <th>所需最小单位数量</th><th>操作</th>
                                </tr></thead>
                                <tbody>
                                <tr>
                                    <td>Tanmay</td><td>Bangalore</td><td>560001</td><td>Tanmay</td>
                                    <td>Tanmay</td><td>Bangalore</td><td>560001</td><td>Tanmay</td>
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
        <div class="col-md-offset-4 col-md-4">
            <div class="portlet light bordered form-horizontal">
            <div class="text-center">
                    <span class="caption-subject bold font-blue-hoki">添加原材料</span>
            </div>
                <div class="modal-body">
                    <input type="input" class="form-control" id="input-check-node" placeholder="请输入原材料名称" value="">
                </div>
            <div id="treeview-checkable" class=""></div>

        <div class="col-sm-4">
            <div id="checkable-output"></div>
        </div>
        <div class="form-group text-center">
            <input class="btn green"  type="submit"  value="保存"/>&nbsp;&nbsp;&nbsp;
            <a class="btn default" @click="cancelTreeView" >取消</a>
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
                type : "post",
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
                treeView:false,//树状图

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
                },
                closeTreeView:function () {
                    this.treeView=true;
                },
                cancelTreeView:function () {
                    this.treeView=false;
                },
            },
            ready:function(){//钩子加载后
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
<!--树状图-->
<%--<link href="assets/treeview/bower_components/bootstrap/dist/css/bootstrap.css" rel="stylesheet">
<script src="assets/treeview/bower_components/jquery/dist/jquery.js"></script>--%>
<script src="assets/treeview/js/bootstrap-treeview.js"></script>
<script type="text/javascript">
    $(function() {
        $.get('scmCategory/query',function (jsonData) {
            console.log(jsonData)
            var data=JSON.stringify(jsonData).data;
            var defaultData=[];
            for (var i=0;i<data.length;i++){
                defaultData[i]=data[i]
            }
        })

        var defaultData = [
            {
                text: '一',
                href: '一',
                tags: ['4'],
                nodes: [
                    {
                        text: 'Child 1',
                        href: '#child1',
                        tags: ['2'],
                        nodes: [
                            {
                                text: '序号1',
                                href: '#grandchild1',
                                tags: ['0']
                            },
                            {
                                text: '序号2',
                                href: '#grandchild2',
                                tags: ['0']
                            }
                        ]
                    },
                    {
                        text: 'Child 2',
                        href: '#child2',
                        tags: ['0']
                    }
                ]
            },
            {
                text: 'Parent 3',
                href: '#parent3',
                tags: ['0']
            },
            {
                text: 'Parent 4',
                href: '#parent4',
                tags: ['0']
            },
            {
                text: 'Parent 5',
                href: '#parent5'  ,
                tags: ['0']
            }
        ];
        var $checkableTree = $('#treeview-checkable').treeview({
            data: defaultData,
            showIcon: true,
            showCheckbox: true,
            onNodeChecked: function(event, node) {
                $('#checkable-output').prepend('<p>' + node.text + ' was checked</p>');
            },
            onNodeUnchecked: function (event, node) {
                $('#checkable-output').prepend('<p>' + node.text + ' was unchecked</p>');
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
        //
    });
</script>
