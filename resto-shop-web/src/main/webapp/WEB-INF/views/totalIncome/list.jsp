<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<h2 class="text-center"><strong>营业总额报表</strong></h2>
<div class="row" id="searchTools">
    <div class="col-md-12">
        <form class="form-inline">
            <div class="form-group" style="margin-right: 50px;">
                <label for="beginDate">开始时间：</label>
                <input type="text" class="form-control form_datetime" id="beginDate" readonly="readonly">
            </div>
            <div class="form-group" style="margin-right: 50px;">
                <label for="endDate">结束时间：</label>
                <input type="text" class="form-control form_datetime" id="endDate" readonly="readonly">
                <br></div>
            <button type="button" class="btn btn-primary" id="today"> 今日</button>
            <button type="button" class="btn btn-primary" id="yesterDay">昨日</button>
            <!--              <button type="button" class="btn yellow" id="benxun">本询</button> -->
            <button type="button" class="btn btn-primary" id="week">本周</button>
            <button type="button" class="btn btn-primary" id="month">本月</button>

            <button type="button" class="btn btn-primary" id="searchReport">查询报表</button>&nbsp;
            <button type="button" class="btn btn-primary" id="brandreportExcel">下载报表</button><br/>
            <form>
                <input type="hidden" id="brandDataTable">
                <input type="hidden" id="shopDataTable">
            </form>&nbsp;&nbsp;&nbsp;
        </form>
    </div>
</div>
<br/>
<div>
    <!-- 每日报表 -->
    <div id="report-editor">
        <div class="panel panel-success">
            <div class="panel-heading text-center">
                <strong style="margin-right:100px;font-size:22px">收入条目</strong>
            </div>
            <div class="panel-body">
                <table id="brandReportTable" class="table table-striped table-bordered table-hover" width="100%"></table>
                <br/>
                <table id="shopReportTable" class="table table-striped table-bordered table-hover" width="100%"></table>
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

    //文本框默认值
    $('.form_datetime').val(new Date().format("yyyy-MM-dd"));

    var beginDate = $("#beginDate").val();
    var endDate = $("#endDate").val();
    var dataSource;
    $.ajax( {
        url:'totalIncome/reportIncome',
        async:false,
        data:{
            'beginDate':beginDate,
            'endDate':endDate
        },
        success:function(data) {
            dataSource=data;
        },
        error : function() {
            toastr.error("系统异常请重新刷新");
        }
    });

    var tb1 = $("#brandReportTable").DataTable({
        data:dataSource.brandIncome,
        dom:'i',
        columns : [
            {
                title : "品牌",
                data : "brandName",
            },
            {
                title : "订单总额(元)",
                data : "totalIncome",
            },
            {
                title : "微信支付(元)",
                data : "wechatIncome",
            },
            {
                title : "充值账户支付(元)",
                data : "chargeAccountIncome",
            },
            {
                title : "红包支付(元)",
                data : "redIncome",
            },
            {
                title : "优惠券支付(元)",
                data : "couponIncome",
            },
            {
                title : "充值赠送支付(元)",
                data : "chargeGifAccountIncome",
            },
            {
                title : "等位红包支付(元)",
                data : "waitNumberIncome",
            },
            {
                title : "支付宝支付(元)",
                data : "aliPayment"
            },
            {
                title : "退菜支付(元)",
                data:"articleBackPay",
            },

            {
                title : "其他方式支付(元)",
                data : "otherPayment",
            }
        ]

    });

    var tb2 = $("#shopReportTable").DataTable({
        data:dataSource.shopIncome,
        bSort:false,
        columns : [
            {
                title : "店铺名称",
                data : "shopName",
            },
//		{
//			title : "营收总额(元)",
//			data : "factIncome",
//			defaultContent:'0'
//		},
            {
                title : "订单总额(元)",
                data : "totalIncome",
            },
            {
                title : "微信支付(元)",
                data : "wechatIncome",
            },
            {
                title : "充值账户支付(元)",
                data : "chargeAccountIncome",
            },
            {
                title : "红包支付(元)",
                data : "redIncome",
            },
            {
                title : "优惠券支付(元)",
                data : "couponIncome",
            },

            {
                title : "充值赠送支付(元)",
                data : "chargeGifAccountIncome",
            },
            {
                title : "等位红包支付(元)",
                data : "waitNumberIncome",
            },
            {
                title : "支付宝支付(元)",
                data : "aliPayment",
            },
            {
                title : "退菜支付(元)",
                data:"articleBackPay",
            },
            {
                title : "其他方式支付(元)",
                data : "otherPayment",
            }
        ]

    });

    //查询
    $("#searchReport").click(function(){
        beginDate = $("#beginDate").val();
        endDate = $("#endDate").val();
        searchInfo(beginDate,endDate);

    })

    //今日

    $("#today").click(function(){
        date = new Date().format("yyyy-MM-dd");
        beginDate = date;
        endDate = date;
        searchInfo(beginDate,endDate);
    });

    //昨日
    $("#yesterDay").click(function(){
        beginDate = GetDateStr(-1);
        endDate = GetDateStr(-1);
        $("#beginDate").val(beginDate);
        $("#endDate").val(endDate);
        searchInfo(beginDate,endDate);

    });


    //本周
    $("#week").click(function(){
        beginDate = getWeekStartDate();
        endDate = new Date().format("yyyy-MM-dd");
        $("#beginDate").val(beginDate);
        $("#endDate").val(endDate);
        searchInfo(beginDate,endDate);

    });


    //本旬
    $("#benxun").click(function(){



    });


    //本月
    $("#month").click(function(){
        beginDate = getMonthStartDate();
        endDate = new Date().format("yyyy-MM-dd");
        $("#beginDate").val(beginDate);
        $("#endDate").val(endDate);
        searchInfo(beginDate,endDate);

    });



    function searchInfo(beginDate,endDate){
        //更新数据源
        $.ajax( {
            url:'totalIncome/reportIncome',
            data:{
                'beginDate':beginDate,
                'endDate':endDate
            },
            success:function(result) {
                dataSource=result;
                tb1.clear().draw();
                tb2.clear().draw();
                tb1.rows.add(result.brandIncome).draw();
                tb2.rows.add(result.shopIncome).draw();
                toastr.success('查询成功');
            },
            error : function() {
                toastr.error("系统异常请重新刷新");
            }
        });
    }


    //导出品牌数据
    $("#brandreportExcel").click(function(){
        beginDate = $("#beginDate").val();
        endDate = $("#endDate").val();
        location.href="totalIncome/brandExprotExcel?beginDate="+beginDate+"&&endDate="+endDate;

    })

    //导出店铺数据

    $("#shopreportExcel").click(function(){
        beginDate=$("#beginDate").val();
        endDate = $("#endDate").val();
        location.href="totalIncome/shopExprotExcel?beginDate="+beginDate+"&&endDate="+endDate;
    })

</script>
