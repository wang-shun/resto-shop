<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>
<style>
th {
	width: 30%;
}
</style>
	<h2 class="text-center">
		<strong>订单列表</strong>
	</h2>
	<br />
	<div class="row">
		<div class="col-md-12">
			<form class="form-inline">
				<div class="form-group" style="margin-right: 50px;">
					<label for="beginDate2">开始时间：</label> <input type="text"
						class="form-control form_datetime2" id="beginDate2" readonly="readonly">
				</div>
				<div class="form-group" style="margin-right: 50px;">
					<label for="endDate2">结束时间：</label> <input type="text"
						class="form-control form_datetime2" id="endDate2" 
						readonly="readonly">
				</div>
				<button type="button" class="btn btn-primary" id="searchInfo2">查询报表</button>
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
	
	<!-- 查看 订单的详细信息-->
	<div class="modal fade" id="orderShopdetail" tabindex="-1" role="dialog">
	  <div class="modal-dialog" role="document">
	    <div class="modal-content">
	      <div class="modal-header">
	        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
	        <h4 class="modal-title text-center"><strong>订单详情</strong></h4>
	      </div>
	      <div class="modal-body">
	      	<dl class="dl-horizontal">
				<dt>店铺名称：</dt><dd id="shopName"></dd><br/>
				<dt>订单编号：</dt><dd id="orderNumber"></dd><br/>
				<dt>订单时间：</dt><dd id="createTime"></dd><br/>
				<dt>就餐模式：</dt><dd id="distributionMode"></dd><br/>
				<dt>验证码：</dt><dd id="verCode"></dd><br/>
				<dt>手机号：</dt><dd id="telePhone"></dd><br/>
				<dt>订单金额：</dt><dd id="orderMoney"></dd><br/>
				<dt>评价：</dt><dd id="appraise"></dd><br/>
				<dt>评价内容：</dt><dd id="content"></dd><br/>
				<dt>状态：</dt><dd id="orderState"></dd><br/>
			</dl>
			<table class="table table-bordered">
				<thead>
					<tr>
						<th>餐品类型</th>
						<th>餐品类别</th>
						<th>餐品名称</th>
						<th>餐品单价(元)</th>
						<th>餐品数量</th>
						<th>小计(元)</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="shop in shopOrderList">
						<td><strong>{{shop.shopName}}</strong></td>
						<td>{{shop.number}}</td>	
						<td>{{shop.orderMoney}}</td>
						<td>{{shop.average}}</td>
						<td>1</td>
					</tr>
				</tbody>
			</table>
			
			
	      </div>
	      <div class="modal-footer">
	      	<button type="button" class="btn btn-success" data-dismiss="modal">关闭</button>
	        <button type="button" class="btn btn-danger" data-dismiss="modal">退款</button>
	      </div>
	    </div>
	  </div>
	</div>
	

<script>
	//时间插件

	$('.form_datetime2').datetimepicker({
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
	$("#beginDate2").val("${beginDate}");
	$("#endDate2").val("${endDate}");
	
		 var tb1 = $("#shopOrder").DataTable({
				ajax : {
					url : "orderReport/AllOrder",   
					dataSrc : "",
					data:function(d){
						d.beginDate=$("#beginDate2").val();
						d.endDate=$("#endDate2").val();
						d.shopId = shopId;
						return d;
					}
				},
				columns : [
					{ 
						title : "店铺",
						data : "shopName" ,
						
					},                 
					{ 
						title : "订单编号",
						data : "id" 
					},
					{ 
						title : "下单时间", 
						data : "createTime",
						createdCell:function(td,tdData){
							$(td).html( new Date(tdData).format("yyyy-MM-dd hh:mm:ss"))
						}
						
					},
					{ 
						title : "就餐模式",
					    data : "distributionModeId",
					    createdCell:function(td,tdData){
					    	switch(tdData)
					    	{
					    	case 1:
					    	  $(td).html("堂吃");
					    	  break;
					    	case 2:
					    	  $(td).html("自提外卖");
					    	case 3:
					    	  $(td).html("外带");
					    	  break;
					    	default:
					    	 $(td).html("未知")
					    	}
					    	
					    }
					    
					},
					{ 
					    title : "验证码", 
					    data : "verCode" 
					},
					{
						title : "手机号", 
						data : "telephone" 
					},
					{ 
						title : "订单金额", 
						data : "orderMoney" 
					},
					{ 
					  title : "评价", 
					  data : "level" ,
					  createdCell:function(td,tdData){
						  switch(tdData)
					    	{
					    	case 5:
					    	  $(td).html("非常满意");
					    	  break;
					    	case 4:
					    	  $(td).html("基本满意");
					    	case 3:
					    	  $(td).html("一般");
					    	  break;
					    	case 2:
					    	  $(td).html("差");
					    	  break;
					    	case 1:
					    	  $(td).html("非常满意");
					    	  break;
					    	default:
					    	 $(td).html("未评价")
					    	}
					  }
					  
					},
					{
					 title : "订单状态", 
					 data : "orderState",
					 createdCell:function(td,tdData){
						  switch(tdData)
					    	{
					    	case 2:
					    	  $(td).html("已付款");
					    	  break;
					    	case 9:
					    	  $(td).html("基本满意");
					    	case 3:
					    	  $(td).html("已取消");
					    	  break;
					    	case 10:
					    	  $(td).html("已确认");
					    	  break;
					    	case 11:
					    	  $(td).html("已评价");
					    	  break;
					    	case 12:
					    	  $(td).html("已分享");
					    	  break;
					    	default:
					    	 $(td).html("未评价")
					    	}
					  }
					  
					 
					 
					 },
					{
					 title : "操作", 
					 data : "id",
					 createdCell:function(td,tdData){
						 var button = $("<button class='btn'>点击查看详情</button>");
							button.click(function(){
								$("#orderShopdetail").modal();
							});
							$(td).html(button);
					 }
					 }
				]
			});
		
	
		
	
	 
	 //查询
	 $("#searchInfo2").click(function(){
		 var beginDate = $("#beginDate2").val();
		 var endDate = $("#endDate2").val();
		 var data = {"beginDate":beginDate,"endDate":endDate,"shopId":shopId};
		 tb1.ajax.reload();
		 toastr.success("查询成功");
	 })
	 
	 
	 

</script>
