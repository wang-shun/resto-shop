<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<h2 class="text-center"><strong>结算报表</strong></h2>
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
		  <button type="button" class="btn btn-primary" id="searchReport">查询报表</button><form>
			<input type="hidden" id="brandDataTable">
			<input type="hidden" id="shopDataTable">
		</form>&nbsp;&nbsp;&nbsp;
		</form>
	</div>
</div>
<button type="button" class="btn btn-primary" id="brandreportExcel">品牌数据导出excel</button><br/>
<button type="button" class="btn btn-primary" id="shopreportExcel">店铺数据导出excel</button><br/>
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
    url:'totalRevenue/reportIncome',
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
			title : "营收总额(元)",
			data : "totalIncome",
		},       
		{                 
			title : "红包支付(元)",
			data : "redIncome",
		},       
		{                 
			title : "优惠券支付收入(元)",
			data : "couponIncome",
		},       
		{                 
			title : "微信支付收入(元)",
			data : "wechatIncome",
		},       
		{                 
			title : "充值账户支付(元)",
			data : "chargeAccountIncome",
		},       
		{                 
			title : "充值赠送账户支付(元)",
			data : "chargeGifAccountIncome",
		},       
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
		{                 
			title : "营收总额(元)",
			data : "totalIncome",
		},       
		{                 
			title : "红包支付收入(元)",
			data : "redIncome",
		},       
		{                 
			title : "优惠券支付收入(元)",
			data : "couponIncome",
		},       
		{                 
			title : "微信支付收入(元)",
			data : "wechatIncome",
		},     
		{                 
			title : "充值账户支付(元)",
			data : "chargeAccountIncome",
		},     
		{                 
			title : "充值赠送账户支付(元)",
			data : "chargeGifAccountIncome",
		}     
	]
	
});

$("#searchReport").click(function(){
	 beginDate = $("#beginDate").val();
	 endDate = $("#endDate").val();
	//更新数据源
	 $.ajax( {  
		    url:'totalRevenue/reportIncome',
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
})

//导出品牌数据
$("#brandreportExcel").click(function(){
	 beginDate = $("#beginDate").val();
	 endDate = $("#endDate").val();
	location.href="totalRevenue/brandExprotExcel?beginDate="+beginDate+"&&endDate="+endDate;
	
})

//导出店铺数据

$("#shopreportExcel").click(function(){
	beginDate=$("#beginDate").val();
	endDate = $("#endDate").val();
	location.href="totalRevenue/shopExprotExcel?beginDate="+beginDate+"&&endDate="+endDate;
})



</script>
