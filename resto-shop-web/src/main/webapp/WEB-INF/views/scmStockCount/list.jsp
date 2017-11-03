<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div id="control">
    <!--查看详情-->
    <div class="row form-div" v-show="details">
        <div class="col-md-offset-3 col-md-6" style="background: #FFF;">
            <div class="text-center" style="padding: 20px 0">
                <span class="caption-subject bold font-blue-hoki">查看详情</span>
            </div>
            <div class="row">
                <div class="col-md-12">
                    <div class="form-group row">
                        <label class="col-md-2 control-label">盘点单号</label>
                        <div class="col-md-4">
                            {{detailsArr.orderCode}}
                        </div>
                        <label class="col-md-2 control-label">盘点时间</label>
                        <div class="col-md-4">
                            {{detailsArr.publishedTime}}
                        </div>
                    </div>
                    <div class="form-group row">
                        <label class="col-md-2 control-label">类型</label>
                        <div class="col-md-4">
                            {{detailsArr.materialType}}
                        </div>
                        <label class="col-md-2 control-label">物料种类</label>
                        <div class="col-md-4">
                            {{detailsArr.size}}种
                        </div>
                    </div>
                    <div class="form-group row">
                        <label class="col-md-2 control-label">盘点人</label>
                        <div class="col-md-4">
                            {{detailsArr.createrName}}
                        </div>
                        <label class="col-md-2 control-label">备注</label>
                        <div class="col-md-4">
                            {{detailsArr.orderStatus}}
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
                                <th>理论库存</th>
                                <th>盘点库存</th>
                                <th>差异数量</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr v-for="item in detailsArr.stockCountDetailList">
                                <td>{{item.materialType}}</td>
                                <td>{{item.categoryOneName}}</td>
                                <td>{{item.categoryTwoName}}</td>
                                <td>{{item.categoryThirdName}}</td>
                                <td>{{item.materialName}}</td>
                                <td>{{item.materialCode}}</td>
                                <td>{{item.measureUnit+item.unitName+"/"+item.specName}}</td>
                                <td>{{item.provinceName+item.cityName+item.districtName}}</td>
                                <td>{{item.theoryStockCount}}</td>
                                <td>{{item.actStockCount}}</td>
                                <td>{{item.actStockCount-item.theoryStockCount}}</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
            <div class="text-center" style="padding: 20px 0">
                <a class="btn default" @click="detailsCli">取消</a>
            </div>
        </div>
    </div>
    <!--查看详情-->
    <div class="table-div">
        <div class="table-body">
            <table class="table table-striped table-hover table-bordered "></table>
        </div>
    </div>
</div>
<script>
    (function(){
        var $table = $(".table-body>table");
        var tb = $table.DataTable({
            ajax : {
                url : "scmStockCount/list_all",
                dataSrc : "data"
            },
            columns : [
                {
                    title: "盘点单号",
                    data: "id",
                },
                {
                    title:"盘点单名",
                    data:"orderName"
                },
                {
                    title:"盘点日期",
                    data:"publishedTime"
                },
                {
                    title:"盘点人",
                    data:"createrName"
                },
                {
                    title:"备注",
                    data:"orderStatus"
                },
                {
                    title : "操作",
                    data : "id",
                    createdCell:function(td,tdData,rowData){
                        var operator=[
                            <s:hasPermission name="scmStockCount/showDetails">
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
                detailsArr:{},//查看详情对象
            },
            methods:{
                showDetails:function (data) { //查看详情
                    this.details=true;
                    this.detailsArr=data;
                },
                detailsCli:function () { //关闭查看详情
                    this.details=false;
                },
            },
        });
        C.vue=vueObj;
    }());


</script>
