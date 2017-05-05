<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>

<div id="control">
    <h2 class="text-center"><strong>订单报表</strong></h2><br/>
    <div class="row" id="searchTools">
        <div class="col-md-12">
            <form class="form-inline">
                <div class="form-group" style="margin-right: 50px;">
                    <label for="beginDate">开始时间：</label>
                    <input type="text" class="form-control form_datetime" id="beginDate" v-model="searchDate.beginDate"   readonly="readonly">
                </div>
                <div class="form-group" style="margin-right: 50px;">
                    <label for="endDate">结束时间：</label>
                    <input type="text" class="form-control form_datetime" id="endDate" v-model="searchDate.endDate"   readonly="readonly">
                </div>
                <button type="button" class="btn btn-primary" @click="today"> 今日</button>
                <button type="button" class="btn btn-primary" @click="yesterDay">昨日</button>
                <button type="button" class="btn btn-primary" @click="week">本周</button>
                <button type="button" class="btn btn-primary" @click="month">本月</button>
                <button type="button" class="btn btn-primary" @click="searchInfo">查询报表</button>&nbsp;
                <button type="button" class="btn btn-primary" @click="createOrderExcel">下载报表</button><br/>
            </form>

        </div>
    </div>
    <br/>
    <br/>

    <!-- 品牌订单列表 -->
    <div role="tabpanel" class="tab-pane" id="orderReport">
        <div class="panel panel-primary" style="border-color:write;">
            <!-- 品牌订单 -->
            <div class="panel panel-info">
                <div class="panel-heading text-center">
                    <strong style="margin-right:100px;font-size:22px">品牌订单报表</strong>
                </div>
                <div class="panel-body">
                    <table id="brandOrderTable" class="table table-striped table-bordered table-hover" width="100%">
                        <thead>
                        <tr>
                            <th>品牌</th>
                            <th>订单总额(元)</th>
                            <th>订单总数(份)</th>
                            <th>订单平均金额(元)</th>
                            <th>营销撬动率</th>
                        </tr>
                        </thead>
                        <tbody>
                        <template v-if="brandOrder.name != null">
                            <tr>
                                <td><strong>{{brandOrder.name}}</strong></td>
                                <td>{{brandOrder.orderMoney}}</td>
                                <td>{{brandOrder.number}}</td>
                                <td>{{brandOrder.average}}</td>
                                <td>{{brandOrder.marketPrize}}</td>
                            </tr>
                        </template>
                        <template v-else>
                            <tr>
                                <td align="center" colspan="5">
                                    暂时没有数据...
                                </td>
                            </tr>
                        </template>
                        </tbody>
                    </table>
                </div>

                <div class="panel-heading text-center">
                    <strong style="margin-right:100px;font-size:22px">店铺订单报表</strong>
                </div>
                <div class="panel-body">
                    <table id="shopOrderTable" class="table table-striped table-bordered table-hover" width="100%">
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="assets/customer/date.js" type="text/javascript"></script>

<script>

    //时间插件
    $('.form_datetime').datetimepicker({
        endDate:new Date(),
        minView:"month",
        maxView:"month",
        autoclose:true,//选择后自动关闭时间选择器
        todayBtn:true,//在底部显示 当天日期
        todayHighlight:true,//高亮当前日期
        format:"yyyy-mm-dd",
        startView:"month",
        language:"zh-CN"
    });


    //创建vue对象
    var vueObj =  new Vue({
        el:"#control",
        data:{
            shopOrderList : [],
            brandOrder:{},
            searchDate : {
                beginDate : "",
                endDate : "",
            },
            shopOrderTable : {}
        },
        created : function() {
            var date = new Date().format("yyyy-MM-dd");
            this.searchDate.beginDate = date;
            this.searchDate.endDate = date;
            this.createShopOrderTable();
            this.searchInfo();
        },
        methods:{
            createShopOrderTable : function(){
                //that代表 vue对象
                var that = this;
                //datatable对象
                that.shopOrderTable=$("#shopOrderTable").DataTable({
                    lengthMenu: [ [50, 75, 100, -1], [50, 75, 100, "All"] ],
                    order: [[ 1, "desc" ]],
                    columns : [
                        {
                            title : "店铺名称",
                            data : "name",
                            orderable : false
                        },
                        {
                            title : "订单总额(元)",
                            data : "orderMoney"
                        },
                        {
                            title : "订单总数(份)",
                            data : "number"
                        },
                        {
                            title : "订单平均金额(元)",
                            data : "average"
                        },
                        {
                            title : "营销撬动率",
                            data : "marketPrize"
                        },
                        {
                            title: "操作",
                            data: "shopDetailId",
                            orderable : false,
                            createdCell: function (td, tdData, rowData) {
                                var shopName = rowData.name;
                                var button = $("<a href='orderReport/show/shopReport?beginDate="+that.searchDate.beginDate+"&&endDate="+that.searchDate.endDate+"&&shopId="+tdData+"&&shopName="+shopName+"' class='btn green ajaxify '>查看详情</a>");
                                $(td).html(button);
                            }
                        }
                    ]
                });
            },
            searchInfo : function() {
                //判断两个日期的天数是否相差31天之内
                var days =this.getDays(this.getDate().beginDate,this.getDate().endDate);
                if(this.getDate().endDate<this.getDate().beginDate){
                    toastr.error("开始时间不能大于结束时间请重新选择")
                    return;
                }
                if(days>31){
                    toastr.info("只能查31天内的数据....")
                    return;
                }
                toastr.clear();
                toastr.success("查询中...");
                var that = this;
                try {
                    $.post("orderReport/brand_data", this.getDate(), function (result) {
                        if(result.success) {
                            that.shopOrderTable.clear();
                            that.shopOrderTable.rows.add(result.data.result.shopId).draw();
                            that.shopOrderList = result.data.result.shopId;
                            that.brandOrder = result.data.result.brandId;
                            toastr.clear();
                            toastr.success("查询成功");
                        }else{
                            toastr.clear();
                            toastr.error("查询出错");
                        }
                    });
                }catch (e){
                    toastr.clear();
                    toastr.error("系统异常，请刷新重试");
                }
            },
            getDate : function(){
                var data = {
                    beginDate : this.searchDate.beginDate,
                    endDate : this.searchDate.endDate
                };
                return data;
            },
            createOrderExcel : function () {
                //判断两个日期的天数是否相差31天之内
                var days =this.getDays(this.searchDate.beginDate,this.searchDate.endDate);
                if( this.searchDate.endDate<this.searchDate.beginDate){
                    toastr.error("开始时间不能大于结束时间请重新选择")
                    return;
                }
                if(days>31){
                    toastr.info("只能下载31天内的数据....")
                    return;
                }

                var object = {
                    beginDate : this.searchDate.beginDate,
                    endDate : this.searchDate.endDate,
                    brandOrderDto : this.brandOrder,
                    shopOrderDtos : this.shopOrderList
                }
                try {
                    $.post("orderReport/create_brand_excel",object,function (result) {
                        if(result.success){
                            window.location.href = "orderReport/downloadBrandOrderExcel?path="+result.data+"";
                        }else{
                            toastr.clear();
                            toastr.error("生成报表出错");
                        }
                    });
                }catch (e){
                    toastr.clear();
                    toastr.error("系统异常，请刷新重试");
                }
            },
            today : function(){
                date = new Date().format("yyyy-MM-dd");
                this.searchDate.beginDate = date
                this.searchDate.endDate = date
                this.searchInfo();
            },
            yesterDay : function(){

                this.searchDate.beginDate = GetDateStr(-1);
                this.searchDate.endDate  = GetDateStr(-1);
                this.searchInfo();
            },
            week : function(){
                this.searchDate.beginDate  = getWeekStartDate();
                this.searchDate.endDate  = new Date().format("yyyy-MM-dd")
                this.searchInfo();
            },
            month : function(){
                this.searchDate.beginDate  = getMonthStartDate();
                this.searchDate.endDate  = new Date().format("yyyy-MM-dd")
                this.searchInfo();
            },
            getDays:function (beginDate,endDate) {
                var strSeparator = "-"; //日期分隔符
                var oDate1;
                var oDate2;
                var iDays;
                oDate1= beginDate.split(strSeparator);
                oDate2= endDate.split(strSeparator);
                var strDateS = new Date(oDate1[0], oDate1[1]-1, oDate1[2]);
                var strDateE = new Date(oDate2[0], oDate2[1]-1, oDate2[2]);
                iDays = parseInt(Math.abs(strDateS - strDateE ) / 1000 / 60 / 60 /24)//把相差的毫秒数转换为天数
                return iDays ;
            }


        }
    });
</script>

