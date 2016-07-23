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
	
	<!-- 查看 数据库配置 详细信息  Modal  start-->
	<div class="modal fade" id="orderDetail" tabindex="-1" role="dialog">
	  <div class="modal-dialog" role="document">
	    <div class="modal-content">
	      <div class="modal-header">
	        <button type="button" class="close" data-dismiss="modal" id="closeModal2" aria-label="Close"><span aria-hidden="true">&times;</span></button>
	        <h4 class="modal-title text-center"><strong>订单详情</strong></h4>
	      </div>
	      <div class="modal-body">
	      	<dl class="dl-horizontal">
				<dt>店铺名称：</dt><dd id="shopName"></dd><br/>
				<dt>订单编号：</dt><dd id="orderId"></dd><br/>
				<dt>订单时间：</dt><dd id="createTime"></dd><br/>
				<dt>就餐模式：</dt><dd id="distributionMode"></dd><br/>
				<dt>验证码：</dt><dd id="verCode"></dd><br/>
				<dt>手机号：</dt><dd id="telephone"></dd><br/>
				<dt>订单金额：</dt><dd id="orderMoney"></dd><br/>
				<dt>评价：</dt><dd id="appriase"></dd><br/>
				<dt>评价内容：</dt><dd id="content"></dd><br/>
				<dt>状态：</dt><dd id="orderState"></dd><br/>
			</dl>
	      </div>
	      
	      <div class="table-scrollable">
               <table class="table table-condensed table-hover">
                   <thead>
                       <tr>
<!--                            <th>餐品类型</th> -->
                           <th>餐品类别 </th>
                           <th>餐品名称 </th>
                           <th>餐品单价 </th>
                           <th> 餐品数量 </th>
                           <th> 小记 </th>
                       </tr>
                   </thead>
                   <tbody id="articleList">
                   </tbody>
               </table>
           </div>
	      
	      <div class="modal-footer">
	        <button type="button" class="btn btn-default" data-dismiss="modal" id="closeModal">关闭</button>
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
			 "lengthMenu": [ [50, 75, 100, 150], [50, 75, 100, "All"] ],
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
					    	  $(td).html("五星");
					    	  break;
					    	case 4:
					    	  $(td).html("四星");
					    	case 3:
					    	  $(td).html("三星");
					    	  break;
					    	case 2:
					    	  $(td).html("二星");
					    	  break;
					    	case 1:
					    	  $(td).html("一星");
					    	  break;
					    	default:
					    		$(td).html("");
					    	 break;
					    	}
					  }
					  
					},
					{
					 title : "订单状态", 
					 data : "orderState",
					 createdCell:function(td,tdData,row,rowData){
						  switch(tdData)
					    	{
					    	case 1:
						    	  $(td).html("未付款");
						    	  break;
						  
					    	case 2:
					    	  $(td).html("已付款");
					    	  break;
					    	case 9:
					    	  $(td).html("已取消");
					    	  break;
					    	case 10:
					    		if(row.productionStatus==1){
					    			$(td).html("已确认");
					    		}else if(row.productionStatus==2){
					    			$(td).html("已消费");
					    		}
					    	  
					    	  break;
					    	case 11:
					    	  $(td).html("已评价");
					    	  break;
					    	case 12:
					    	  $(td).html("已分享");
					    	  break;
					    	default:
					    	 break;
					    	}
					  }
					  
					 },
					{
					 title : "操作", 
					 data : "id",
					 createdCell:function(td,tdData){
						 var button = $("<button class='btn green'>详情</button>");
							button.click(function(){
								showDetails(tdData);
							})
							$(td).html(button);		
							
					 }
					 }
				]
			});
		
	 
	 $("#searchInfo2").click(function(){
		 var beginDate = $("#beginDate2").val();
		 var endDate = $("#endDate2").val();
		 var data = {"beginDate":beginDate,"endDate":endDate,"shopId":shopId};
		 tb1.ajax.reload();
		 toastr.success("查询成功");
	 })
	 
	$("#closeModal").click(function(e){
		e.stopPropagation();
		var modal = $("#orderDetail");
		modal.find(".modal-body").html("");
		modal.modal("hide");
	}) 
	 
	function showDetails(orderId){
		$.ajax({
			 url:'orderReport/detailInfo',
			 method:'post',
			 data:{ "orderId":orderId},
			 success:function(result){
				if(result){
					var data = result.data;
					 $("#shopName").html(data.shopName);
					 $("#orderId").html(data.id);
					 $("#createTime").html(new Date(data.createTime).format("yyyy-MM-dd mm:hh:ss"));
					 $("#distributionMode").html(getDistriubtioMode(data.distributionModeId));
					 $("#verCode").html(data.verCode);
					 $("#telephone").html(data.customer.telephone);
					 $("#orderMoney").html(data.orderMoney+"元");
					 if(data.appraise){
						 $("#appriase").html(getLevel(data.appraise.level));
						 $("#content").html(data.appraise.content);
					 }
					
					 $("#orderState").html(getState(data.orderState));
					 $('#articleList').text("");
					
					 for(var i = 0;i< data.orderItems.length;i++){
						 var obj = data.orderItems[i];
						 var article = "<tr><td>"+obj.articleFamily.name+"</td><td>"+obj.articleName+"</td><td>"+
						 obj.unitPrice+"</td><td>"+obj.count+"</td><td>"
						 +obj.finalPrice+"</td></tr>";
						 $('#articleList').append(article);
					 }
					
					 $("#orderDetail").modal();
				}
				 
			 }
		}); 
		 
	 }
	 
	 function getLevel(level){
		 var levelName = '';
		 switch (level)
		 {
		 case 1:
		   levelName="非常不满意";
		   break;
		 case 2:
		   levelName="不满意";
		   break;
		 case 3:
		   levelName="一般";
		   break;
		 case 4:
		   levelName="满意";
		   break;
		 case 5:
		   levelName="非常满意";
		   break;
		 
		 }
		 return levelName; 
	 }
	 
	 
	 function getState(state){
		 var orderState = '';
		 switch(state)
	    	{
	    	case 2:
	    	 orderState = "已支付"
	    	  break;
	    	case 9:
	    	  orderState="已取消";
	    	case 10:
	    	  orderState="已确认";
	    	  break;
	    	case 11:
	    	  orderState="已评价";
	    	  break;
	    	case 12:
	    	 orderState="已分享";
	    	  break;
	    	}
		 return orderState;
	 }
	 
	 
	 function getDistriubtioMode(mode){
		 var distributionMode = ''
				switch(mode)
		    	{
		    	case 1:
		    	  distributionMode ="堂吃";
		    	  break;
		    	case 2:
		    		distributionMode="自提外卖";
		    		break;
		    	case 3:
		    		distributionMode="外带";
		    	  break;
		    	
		    	}
		 return distributionMode;
	 }
	 
	 $("#closeModal2").click(function(e){
		 e.stopPropagation();
			var modal = $("#orderDetail");
			modal.find(".modal-body").html("");
			modal.modal("hide");
	 })
	
	
</script>
