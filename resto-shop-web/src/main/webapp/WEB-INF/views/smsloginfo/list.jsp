<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>
<link rel="stylesheet" type="text/css"
	href="assets/global/plugins/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css">

<h2 class="text-center">
	<strong>短信记录</strong>
</h2>
<br />
<div class="row">
	<div class="col-md-8 col-md-offset-2">
		<form class="form-inline" id="smsForm">
			<div class="form-group" style="margin-right: 50px;">
				<label for="beginDate">开始时间：</label>
				<input type="text" class="form-control form_datetime" id="beginDate" readonly="readonly">
			</div>
			<div class="form-group" style="margin-right: 50px;">
				<label for="endDate">结束时间：</label>
				<input type="text" class="form-control form_datetime" id="endDate" readonly="readonly">
			</div>
		</form>
	</div>
</div>
<br />
<p class="text-danger text-center" hidden="true">
	<strong>开始时间不能大于结束时间！</strong>
</p>
<br />

<div class="row" id="controller">
	<div class="portlet-body form col-md-8">
		<form class="form-horizontal" role="form">
			<div class="form-body">
				<div class="form-group">
					<label class="col-md-3 control-label">店铺选择</label>
					
					<div class="col-md-9" id="choiceShop">
					</div>
				</div>
				
				<div class="form-group">
				    <div class="col-sm-10 col-md-offset-2">
				      	<label class="checkbox-inline" style="margin-left:-25px;">
						  <input type="checkbox" id="checkAll">全选
						</label>
				    </div>
				  </div>
					
				<div class="form-group">
				    <div class="col-sm-offset-2 col-sm-10">
				      <button type="button" id="querySms" class="btn btn-primary">查询短信记录</button>
				    </div>
				 </div>
				
			</div>
		</form>
	</div>
</div>


<div class="panel panel-default">
  <div class="panel-heading">Panel heading without title</div>
  <div class="panel-body">
    <div class="table-body">
			<table class="table table-striped table-hover table-bordered" id="selectList" style="display:none;"></table>
	</div>
  </div>
</div>




<!-- <!-- 日期框 -->
<script
	src="assets/global/plugins/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
<script
	src="assets/global/plugins/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js"></script>


<script>

	$(function(){
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
		
		//查询店铺
		$.ajax({
			url:'smsloginfo/shopName',
			success:function(data){
					$(data).each(function(i, shop) {
						var str = "<label class='checkbox-inline'>"+
						"<input type='checkbox' name='databaseIds' value='"+shop.id+"'/>"+shop.name+
						"</label>";
						$("#choiceShop").append(str);
					})
					$("#choiceShop").trigger("create");
				}
		})
		
		var $table = $(".table-body>table");
		var tb = $table.DataTable({
			ajax : {
				url : "smsloginfo/list_all",
				dataSrc : ""
			},
			columns : [
			    {
			    	title:"手机号",
			    	data:"phone",
			    },
				{                 
					title : "内容",
					data : "content",
				},
				{                 
					title : "发送类型",
					data : "smsType",
				},
			
				{                 
					title : "创建时间",
					data : "createTime",
					createdCell:function(td,tdData){
						$(td).html(new Date().format("yyyy-mm-dd hh:ss"));
					}
					
				},
			
				{                 
					title : "返回结果",
					data : "smsResult",
				},
				
				{                 
					title : "是否成功",
					data : "isSuccess",
				}
				
			
		]
			
		})
		
		$("#querySms").click(function(){
			$.post(){
				
			}
			$("#selectList").show();
		})
		
	})
	

	

</script>
