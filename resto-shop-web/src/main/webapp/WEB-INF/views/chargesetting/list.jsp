<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div id="control">
	<div class="row form-div" v-if="showform">
		<div class="col-md-offset-3 col-md-6" >
			<div class="portlet light bordered">
	            <div class="portlet-title">
	                <div class="caption">
	                    <span class="caption-subject bold font-blue-hoki"> 表单</span>
	                </div>
	            </div>
	            <div class="portlet-body">
	            	<form role="form" action="{{m.id?'chargesetting/modify':'chargesetting/create'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
						    <label>充值金额</label>
						    <input type="text" class="form-control" name="chargeMoney" v-model="m.chargeMoney">
						</div>
						<div class="form-group">
						    <label>赠送金额</label>
						    <input type="text" class="form-control" name="rewardMoney" v-model="m.rewardMoney">
						</div>
						
						<div class="form-group">
								<div class="control-label">选择是否显示到一级菜单</div>
							    <input type="radio"  name="showIn" v-model="m.showIn" value=1 checked="checked">
							    <label for="showIn">是</label>
							    <input type="radio"  name="showIn" v-model="m.ishowIn" value=0> 
							    <label for="showIn">否</label>
						</div>
						
						<div class="form-group">
						    <label>显示的文本</label>
						    <input type="text" class="form-control" name="labelText" v-model="m.labelText">
						</div>
						<div class="form-group">
						    <label>排序</label>
						    <input type="text" class="form-control" name="sort" v-model="m.sort">
						</div>
						<!-- <div class="form-group">
						    <label>活动状态</label>
						    <input type="text" class="form-control" name="state" v-model="m.state">
						</div> -->
						
						<div class="form-group">
								<div class="control-label">是否开启活动</div>
							    <input type="radio"  name="state" v-model="m.state" value=1 >
							    <label for="showIn">是</label>
							    <input type="radio"  name="state" v-model="m.state" value=0 checked="checked"> 
							    <label for="showIn">否</label>
						</div>
						</div>
						<input type="hidden" name="id" v-model="m.id" />
						<input class="btn green"  type="submit"  value="保存"/>
						<a class="btn default" @click="cancel" >取消</a>
					</form>
	            </div>
	        </div>
		</div>
	</div>
	
	<div class="table-div">
		<div class="table-operator">
			<s:hasPermission name="chargesetting/add">
			<button class="btn green pull-right" @click="create">新建</button>
			</s:hasPermission>
		</div>
		<div class="clearfix"></div>
		<div class="table-filter">&nbsp;</div>
		<div class="table-body">
			<table class="table table-striped table-hover table-bordered "></table>
		</div>
	</div>
</div>


<script>
	(function(){
		var cid="#control";
		var $table = $(".table-body>table");
		var tb = $table.DataTable({
			ajax : {
				url : "chargesetting/list_all",
				dataSrc : ""
			},
			columns : [
				{                 
				title : "充值金额",
				data : "chargeMoney",
			},                 
			{                 
				title : "返还金额",
				data : "rewardMoney",
			},                 
			{                 
				title : "是否显示在菜单栏上",
				data : "showIn",
				"render": function(data){
					if(data==0){
						data="不显示";
					}else if(data==1){
						data="显示";
					}else{
						data="未知";
					}
					return data;
				}
			},                 
			{                 
				title : "显示的文本",
				data : "labelText",
			},                 
			{                 
				title : "排序",
				data : "sort",
			},                 
			{                 
				title : "活动状态",
				data : "state",
				"render": function(data){
					if(data==0){
						data="未开启";
					}else if(data==1){
						data="已开启";
					}else{
						data="未知";
					}
					return data;
				}
				
			},                 

				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
						var operator=[
							<s:hasPermission name="chargesetting/delete">
							C.createDelBtn(tdData,"chargesetting/delete"),
							</s:hasPermission>
							<s:hasPermission name="chargesetting/edit">
							C.createEditBtn(rowData),
							</s:hasPermission>
						];
						$(td).html(operator);
					}
				}],
		});
		
		var C = new Controller(cid,tb);
		var vueObj = C.vueObj();
	}());
	
	

	
</script>
