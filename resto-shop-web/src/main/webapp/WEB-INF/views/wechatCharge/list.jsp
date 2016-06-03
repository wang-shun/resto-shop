<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>

<!-- vue对象开始 -->
<div id="control">
<h2 class="text-center"><strong>结算报表</strong></h2><br/>
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
		  </div>
		  <button type="button" class="btn btn-primary" id="searchReport">查询报表</button>
		</form>
	</div>
</div>

<!-- datatable开始 -->
<div class="table-div">
	<div class="clearfix"></div>
	<div class="table-filter"></div>
	<div class="table-body">
		<table class="table table-striped table-hover table-bordered "></table>
	</div>
</div>
<!-- datatable结束 -->

</div>
<!-- vue对象结束 -->
<br/>


<script>
	(function(){
		var cid="#control";
		//加载datatable
		var $table = $(".table-body>table");
		var tb = $table.DataTable({
			ajax : {
				url : "wechatCharge/list_all",
				dataSrc : ""
			},
			columns : [
				{                 
					title : "充值时间",
					data : "createTime",
				},                 
				{                 
					title : "充值金额(元)",
					data : "paymentMoney",
				},                 
				{                 
					title : "返还的金额(元)",
					data : "rewardMoney",
				},
				{
					title:"充值的手机",
					data:"telephone",
				},
				{
					title:"充值的品牌",
					data:"brandName",
				},
				{
					title:"充值的店铺",
					data:"shopDetailName",
				},
				
				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
// 						var operator=[
// 							<s:hasPermission name="kitchen/delete">
// 							C.createDelBtn(tdData,"kitchen/delete"),
// 							</s:hasPermission>
// 							<s:hasPermission name="kitchen/edit">
// 							C.createEditBtn(rowData),
// 							</s:hasPermission>
			//			];
					//	$(td).html(operator);
					}
				}],
		});
		
// 		var C = new Controller(cid,tb);
// 		var vueObj = C.vueObj();
		
// 		//获取 就餐模式
// 		$.ajax({
// 			type:"post",
// 			url:"printer/list_all",
// 			dataType:"json",
// 			success:function(data){
// 				if(data.length > 0){
// 					vueObj.$set("printerList",data);
// 					vueObj.$set("selected",data[0].id);
// 				}
// 			}
// 		})
	}());
	
	
</script>


