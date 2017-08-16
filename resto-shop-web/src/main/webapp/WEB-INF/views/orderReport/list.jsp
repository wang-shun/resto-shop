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
                        <tr><th>品牌</th>
                            <th>订单总数</th>
                            <th>订单总额</th>
                            <th>单均</th>
                            <th>就餐人数</th>
                            <th>人均</th>
                            <th>堂吃订单数</th>
                            <th>堂吃订单额</th>
                            <th>外带订单数</th>
                            <th>外带订单额</th>
                            <th>R+外卖订单数</th>
                            <th>R+外卖订单额</th>
                        </tr>
                        </thead>
                        <tbody>
                        <%--<template v-if="brandOrder.brandName != null">--%>
                            <tr>
                                <td><strong>{{brandOrder.brandName}}</strong></td>
                                <td>{{brandOrder.orderCount}}</td>
                                <td>{{brandOrder.orderPrice}}</td>
                                <td>{{brandOrder.singlePrice}}</td>
                                <td>{{brandOrder.peopleCount}}</td>
                                <td>{{brandOrder.perPersonPrice}}</td>
                                <td>{{brandOrder.tangshiCount}}</td>
                                <td>{{brandOrder.tangshiPrice}}</td>
                                <td>{{brandOrder.waidaiCount}}</td>
                                <td>{{brandOrder.waidaiPrice}}</td>
                                <td>{{brandOrder.waimaiCount}}</td>
                                <td>{{brandOrder.waimaiPrice}}</td>
                            </tr>
                       <%-- </template>
                        <template v-else>
                            <tr>
                                <td align="center" colspan="5">
                                    暂时没有数据...
                                </td>
                            </tr>
                        </template>--%>
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
                            title : "店铺",
                            data : "shopName",
                            orderable : false
                        },
                        {
                            title : "订单总数",
                            data : "shop_orderCount"
                        },
                        {
                            title : "订单总额",
                            data : "shop_orderPrice"
                        },
                        {
                            title : "单均",
                            data : "shop_singlePrice"
                        },
                        {
                            title : "就餐人数",
                            data : "shop_peopleCount"
                        },
                        {
                            title : "人均",
                            data : "shop_perPersonPrice"
                        },
                        {
                            title : "堂吃订单数",
                            data : "shop_tangshiCount"
                        },
                        {
                            title : "堂吃订单额",
                            data : "shop_tangshiPrice"
                        },
                        {
                            title : "外带订单数",
                            data : "shop_waidaiCount"
                        },
                        {
                            title : "外带订单额",
                            data : "shop_waidaiPrice"
                        },
                        {
                            title : "R+外卖订单数",
                            data : "shop_waimaiCount"
                        },
                        {
                            title : "R+外卖订单额",
                            data : "shop_waimaiPrice"
                        },
                        {
                            title: "操作",
                            data: "shopDetailId",
                            orderable : false,
                            createdCell: function (td, tdData, rowData) {
                                var shopName = rowData.shopName;
                                var button = $("<a href='orderReport/show/shopReport?beginDate="+that.searchDate.beginDate+"&&endDate="+that.searchDate.endDate+"&&shopId="+tdData+"&&shopName="+shopName+"' class='btn green ajaxify '>查看详情</a>");
                                $(td).html(button);
                            }
                        }
                    ]
                });
            },
            searchInfo : function() {
                var that = this;
                var timeCha = new Date(that.searchDate.endDate).getTime() - new Date(that.searchDate.beginDate).getTime();
                if(timeCha < 0){
                    toastr.clear();
                    toastr.error("开始时间应该少于结束时间！");
                    return false;
                }else if(timeCha > 2678400000){
                    toastr.clear();
                    toastr.error("暂时未开放大于一月以内的查询！");
                    return false;
                }
                var nowDate = new Date().format("HH");
                nowDate = parseInt(nowDate);
                if (nowDate >= 11 && nowDate <= 13){
                    toastr.clear();
                    toastr.error("亲，报表查询功能正在维护中，请您多多谅解~维护时间段： 11:00-13:00 17:00-19:00");
                    return false;
                }else if (nowDate >= 17 && nowDate <= 20){
                    toastr.clear();
                    toastr.error("亲，报表查询功能正在维护中，请您多多谅解~维护时间段： 11:00-13:00 17:00-19:00");
                    return false;
                }
                toastr.clear();
                toastr.success("查询中...");
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

