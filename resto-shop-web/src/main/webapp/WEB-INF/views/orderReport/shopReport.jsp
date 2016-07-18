<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>
<style>
th {
	width: 30%;
}
</style>
<div id="control">
	<h2 class="text-center">
		<strong>订单列表</strong>
	</h2>
	<br />
	<div class="row">
		<div class="col-md-12">
			<form class="form-inline">
				<div class="form-group" style="margin-right: 50px;">
					<label for="beginDate">开始时间：</label> <input type="text"
						class="form-control form_datetime" id="beginDate"
						readonly="readonly">
				</div>
				<div class="form-group" style="margin-right: 50px;">
					<label for="endDate">结束时间：</label> <input type="text"
						class="form-control form_datetime" id="endDate"
						readonly="readonly">
				</div>
				<button type="button" class="btn btn-primary" @click="searchInfo">查询报表</button>
			</form>
		</div>
	</div>
	<br /> <br />
	<!-- 店铺订单列表  -->
	<div class="panel panel-info">
		<div class="panel-heading text-center" style="font-size: 22px;">
			<strong>店铺订单列表</strong>
		</div>
		<div class="panel-body">
			<table class="table table-striped table-bordered table-hover" id="shopOrder">
			</table>
		</div>
	</div>

</div>
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
	debugger;

	$("#beginDate").val("${beginDate}");
	$("#endDate").val("${endDate}");

	var shopId = "${shopId}"
	
	var tb1 = $("#shopOrder").DataTable({
		dom:'',
		ajax : {
			url : "orderReport/AllOrder",   
			dataSrc : "",
			data:function(d){
				d.beginDate=$("#beginDate").val();
				d.endDate=$("#endDate").val();
				d.shopId = shopId;
				return d;
			}
		},
		columns : [
			{ 
				title : "店铺",
				data : "shopName" 
				
			},                 
			{ 
				title : "订单编号",
				data : "id" 
			},
			{ 
				title : "下单时间", 
				data : "createTime" 
			},
			{ 
				title : "就餐模式",
			    data : "distributionModeId"
			},
			{ 
			    title : "验证码", 
			    data : "vercode" 
			},
			{
				title : "手机号", 
				data : "telephone" 
			},
			{ 
				title : "订单金额", 
				data : "paymentAmount" 
			},
			{ 
			  title : "评价", 
			  data : "level" 
			},
			{
			 title : "订单状态", 
			 data : "orderState"
			 },
			{
			 title : "操作", 
			 data : "id"
			 }
		]
	});

</script>
