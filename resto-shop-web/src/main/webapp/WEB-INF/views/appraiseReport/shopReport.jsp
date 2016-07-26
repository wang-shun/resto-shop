<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>
<style>
th {
	width: 30%;
}
</style>
	<h2 class="text-center">
		<strong>${shopName}</strong>
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
			<strong>店铺评论列表</strong>
		</div>
		<div class="panel-body">
			<table class="table table-striped table-bordered table-hover" id="shopAppraise">
			</table>
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
	
		 var tb1 = $("#shopAppraise").DataTable({
			 "lengthMenu": [ [50, 75, 100, 150], [50, 75, 100, "All"] ],
			 "autoWidth": false,
			 "columnDefs": [
			                { "width": "30%", "targets": 5 }
			              ],
				ajax : {
					url : "appraiseReport/shop_data",   
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
						title:'评论等级',
						data:'appraise.level',
						createdCell:function(td,tdData){
							
							$(td).html(getLevel(tdData))
						}
						
					},
					
					{
						title : "手机号", 
						data : "customer.telephone" 
					},
					
					
					{ 
						title : "订单金额", 
						data : "orderMoney" 
					},
					{ 
						title : "评论金额", 
						data : "appraise.redMoney" 
					},
					{ 
						title : "评论对象", 
						data : "appraise.feedback" 
					},
					{ 
						title : "评论内容", 
						data : "appraise.content" 
					},
				]
			});
		
	 
	 $("#searchInfo2").click(function(){
		 var beginDate = $("#beginDate2").val();
		 var endDate = $("#endDate2").val();
		 var data = {"beginDate":beginDate,"endDate":endDate,"shopId":shopId};
		 tb1.ajax.reload();
		 toastr.success("查询成功");
	 })
	 
	 
	 function getLevel(level){
		 var levelName = '';
		 switch (level)
		 {
		 case 1:
		   levelName="一星";
		   break;
		 case 2:
		   levelName="二星";
		   break;
		 case 3:
		   levelName="三星";
		   break;
		 case 4:
		   levelName="四星";
		   break;
		 case 5:
		   levelName="五星";
		   break;
		 
		 }
		 return levelName; 
	 }
	 
	
</script>
