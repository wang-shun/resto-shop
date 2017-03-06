<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>
<div id="control">
    <a class="btn btn-info ajaxify" href="orderReport/list">
        <span class="glyphicon glyphicon-circle-arrow-left"></span>
        返回
    </a>
    <h2 class="text-center">
        <strong>订单列表</strong>
    </h2>
    <br />
    <div class="row">
        <div class="col-md-12">
            <form class="form-inline">
                <div class="form-group" style="margin-right: 50px;">
                    <label for="beginDate">开始时间：</label>
                    <input type="text" class="form-control form_datetime" id="beginDate" v-model="searchDate.beginDate" readonly="readonly">
                </div>
                <div class="form-group" style="margin-right: 50px;">
                    <label for="endDate">结束时间：</label>
                    <input type="text" class="form-control form_datetime" id="endDate" v-model="searchDate.endDate" readonly="readonly">
                </div>
                <button type="button" class="btn btn-primary" @click="today"> 今日</button>
                <button type="button" class="btn btn-primary" @click="yesterDay">昨日</button>
                <button type="button" class="btn btn-primary" @click="week">本周</button>
                <button type="button" class="btn btn-primary" @click="month">本月</button>
                <button type="button" class="btn btn-primary" @click="searchInfo">查询报表</button>&nbsp;
                <button type="button" class="btn btn-primary" @click="downloadShopOrder">下载报表</button><br/>
            </form>
        </div>
    </div>
    <br />
    <br />
    <!-- 店铺订单列表  -->
    <div class="panel panel-info">
        <div class="panel-heading text-center" style="font-size: 22px;">
            <strong>店铺订单列表</strong>
        </div>
        <div class="panel-body">
            <table class="table table-striped table-bordered table-hover"
                   id="shopOrder">
            </table>
        </div>
    </div>

    <div class="modal fade" id="orderDetail" tabindex="-1" role="dialog">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal"
                            @click="closeModal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title text-center">
                        <strong>订单详情</strong>
                    </h4>
                </div>
                <div class="modal-body">
                    <dl class="dl-horizontal">
                        <dt>店铺名称：</dt>
                        <dd></dd>
                        <dt>订单编号：</dt>
                        <dd></dd>
                        <dt>微信支付单号：</dt>
                        <dd></dd>
                        <dt>订单时间：</dt>
                        <dd></dd>
                        <dt>就餐模式：</dt>
                        <dd></dd>
                        <dt>验 证 码：</dt>
                        <dd></dd>
                        <dt>手 机 号：</dt>
                        <dd></dd>
                        <dt>订单金额：</dt>
                        <dd></dd>
                        <dt>评&nbsp;&nbsp;价：</dt>
                        <dd></dd>
                        <dt>评价内容：</dt>
                        <dd></dd>
                        <dt>状&nbsp;&nbsp;态：</dt>
                        <dd></dd>
                        <dt>菜品总价：</dt>
                        <dd></dd>
                        <dt>服&nbsp;务&nbsp;费：</dt>
                        <dd></dd>
                    </dl>
                </div>
                <div class="table-scrollable">
                    <table class="table table-condensed table-hover">
                        <thead>
                            <tr>
                                <th>餐品类别</th>
                                <th>餐品名称</th>
                                <th>餐品单价</th>
                                <th>餐品数量</th>
                                <th>小记</th>
                            </tr>
                        </thead>
                        <tbody style="height: 300px;">
                        </tbody>
                    </table>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-block btn-primary" data-dismiss="modal" @click="closeModal">关闭</button>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="assets/customer/date.js" type="text/javascript"></script>
<script>
    //时间插件
    $('.form_datetime').datetimepicker({
        endDate : new Date(),
        minView : "month",
        maxView : "month",
        autoclose : true,//选择后自动关闭时间选择器
        todayBtn : true,//在底部显示 当天日期
        todayHighlight : true,//高亮当前日期
        format : "yyyy-mm-dd",
        startView : "month",
        language : "zh-CN"
    });

    var shopId = "${shopId}"
    var beginDate = "${beginDate}";
    var endDate = "${endDate}";

    //创建vue对象
    var vueObj =  new Vue({
        el:"#control",
        data:{
            searchDate : {
                beginDate : "",
                endDate : "",
            },
            shopOrderTable:{},
            shopOrderDetails:[]
        },
        created : function() {
            this.searchDate.beginDate = beginDate;
            this.searchDate.endDate = endDate;
            this.createShopOrderTable();
            this.searchInfo();
        },
        methods:{
            createShopOrderTable : function(){
                this.shopOrderTable = $("#shopOrder").DataTable({
                    lengthMenu : [ [ 50, 75, 100, -1 ], [ 50, 75, 100, "All" ] ],
                    order: [[ 1, 'desc' ]],
                    columns : [
                        {
                            title : "店铺",
                            data : "shopName",
                            orderable : false
                        },
                        {
                            title : "下单时间",
                            data : "beginTime"
                        }, {
                            title : "手机号",
                            data : "telephone",
                            orderable : false
                        }, {
                            title : "订单金额",
                            data : "orderMoney"
                        }, {
                            title : "微信支付",
                            data : "weChatPay"
                        }, {
                            title : "红包支付",
                            data : "accountPay"
                        }, {
                            title : "优惠券支付",
                            data : "couponPay"
                        }, {
                            title : "充值金额支付",
                            data : "chargePay"
                        }, {
                            title : "充值赠送金额支付",
                            data : "rewardPay"
                        },{
                            title : "等位红包支付",
                            data : "waitRedPay"
                        }
                        ,{
                            title : "支付宝支付",
                            data : "aliPayment"
                        }
                        ,{
                            title : "现金支付",
                            data : "moneyPay"
                        },
                        {
                            title : "银联支付",
                            data : "backCartPay"
                        },
                        {
                            title : "退菜返还红包",
                            data : "articleBackPay"
                        },
                        {
                            title : "营销撬动率",
                            data : 'incomePrize'
                        },{
                            title : "操作",
                            data : "orderId",
                            orderable : false,
                            createdCell : function(td, tdData) {
                                var button = $("<button @click='openOrderDetailModal("+tdData+")' class='btn green'>查看详情</button>");
                                $(td).html(button);
                            }
                        }
                    ]
                });
            },
            searchInfo : function() {
                toastr.success("查询中...");
                var that = this;
                try {
                    $.post("orderReport/AllOrder", this.getDate(), function (result) {
                        if(result.success) {
                            that.shopOrderTable.clear();
                            that.shopOrderTable.rows.add(result.data.result).draw()
                            that.shopOrderList = result.data.result;
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
            openOrderDetailModal : function (orderId) {
                alert(orderId);
            },
            getDate : function(){
                var data = {
                    beginDate : this.searchDate.beginDate,
                    endDate : this.searchDate.endDate
                };
                return data;
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
            closeModal : function () {
                $("#orderDetail").modal("hide");
            }
        }
    });
</script>
