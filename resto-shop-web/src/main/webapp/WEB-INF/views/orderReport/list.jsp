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
	<!-- 品牌订单列表 -->
	<div class="panel panel-info">
		<div class="panel-heading text-center" style="font-size: 22px;">
			<strong>品牌订单列表</strong>
		</div>
		<div class="panel-body">
			<table class="table table-striped table-bordered table-hover" id="brandOrder">
			</table>
		</div>
	</div>

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

	<!-- 报表详情 -->
<!-- 	<div class="modal fade bs-example-modal-lg" id="reportModal" -->
<!-- 		tabindex="-1" role="dialog" aria-labelledby="reportModal" -->
<!-- 		data-backdrop="static"> -->
<!-- 		<div class="modal-dialog modal-lg"> -->
<!-- 			<div class="modal-content"> -->
<!-- 				<div class="modal-header"> -->
<!-- 					<button type="button" class="close" data-dismiss="modal" -->
<!-- 						aria-label="Close"> -->
<%-- 						<span aria-hidden="true">&times;</span> --%>
<!-- 					</button> -->
<!-- 					<h4 class="modal-title text-center"> -->
<%-- 						<strong>菜品销售详情</strong> --%>
<!-- 					</h4> -->
<!-- 				</div> -->
<!-- 				<div class="modal-body"></div> -->
<!-- 				<div class="modal-footer"> -->
<!-- 					<button type="button" class="btn btn-info btn-block" @click="closeModal">关闭</button> -->
<!-- 				</div> -->
<!-- 			</div> -->
<!-- 		</div> -->
<!-- 	</div> -->

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

	//文本框默认值
	$('.form_datetime').val(new Date().format("yyyy-MM-dd"));
	
	var tb1 = $("#brandOrder").DataTable({
		dom:'',
		ajax : {
			url : "orderReport/orderPaymentItems",   
			dataSrc : "",
			data:function(d){
				d.beginDate=$("#beginDate").val();
				d.endDate=$("#endDate").val();
				return d;
			}
		},
		columns : [
			{ title : "品牌", data : "brandName" },                 
			{ title : "已消费订单份数(份)", data : "orderNum" },
			{ title : "已消费订单金额(元)", data : "payValue" }
		]
	});
	
	
	var tb2 = $("#shopOrder").DataTable({
		dom:'',
		ajax : {
			url : "orderReport/AllOrder",   
			dataSrc : "",
			data:function(d){
				d.beginDate=$("#beginDate").val();
				d.endDate=$("#endDate").val();
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
