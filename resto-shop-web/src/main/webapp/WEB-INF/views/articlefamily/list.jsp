<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div id="control">
	<div class="row form-div" v-if="showform">
		<div class="col-md-offset-3 col-md-6" >
			<div class="portlet light bordered">
	            <div class="portlet-title">
	                <div class="caption">
	                    <span class="caption-subject bold font-blue-hoki">新建菜品类型</span>
	                </div>
	            </div>
	            <div class="portlet-body">
	            	<form role="form" class="form-horizontal" action="{{m.id?'articlefamily/modify':'articlefamily/create'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
			           			<label class="col-sm-3 control-label">类型名称：</label>
							    <div class="col-sm-8">
									<input type="text" class="form-control" required name="name" v-model="m.name">
							    </div>
							</div>
							<div class="form-group">
			           			<label class="col-sm-3 control-label">序&nbsp;&nbsp;号：</label>
							    <div class="col-sm-8">
							    <input type="number" class="form-control" required placeholder="请输入数字！" min="0" name="peference" v-model="m.peference">
							    </div>
							</div>
							<div class="form-group">
			           			<label class="col-sm-3 control-label">就餐模式：</label>
							    <div class="col-sm-8">
							    <select class="form-control" name="distributionModeId" required v-model="m.distributionModeId?m.distributionModeId:selected">
							    	<option v-for="temp in distributionMode" v-bind:value="temp.id">
							    		{{ temp.name }}
							    	</option>
							    </select>
							    </div>
							</div>
							<div class="text-center">
								<input type="hidden" name="id" v-model="m.id" />
								<input class="btn green"  type="submit"  value="保存"/>
								<a class="btn default" @click="cancel" >取消</a>
							</div>
						</div>
					</form>
	            </div>
	        </div>
		</div>
	</div>
	
	<div class="table-div">
		<div class="table-operator">
			<s:hasPermission name="articlefamily/add">
			<button class="btn green pull-right" @click="create">新建</button>
			</s:hasPermission>
		</div>
		<div class="clearfix"></div>
		<div class="table-filter"></div>
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
				url : "articlefamily/list_all",
				dataSrc : ""
			},
			columns : [
				{                 
					title : "类型名称",
					data : "name",
				},                 
				{                 
					title : "序号",
					data : "peference",
				},              
				{                 
					title : "就餐模式",
					data : "distributionModeId",
					createdCell:function(td,tdData,rowData,row){
						var str = "";
						if("1" == tdData){
							str = "堂吃" ;
						}else{
							str = "情况不明" ;
						}
						$(td).html(str);
					}
				},                 
				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
						var operator=[
							<s:hasPermission name="articlefamily/delete">
							C.createDelBtn(tdData,"articlefamily/delete"),
							</s:hasPermission>
							<s:hasPermission name="articlefamily/edit">
							C.createEditBtn(rowData),
							</s:hasPermission>
						];
						$(td).html(operator);
					}
				}],
		});
		
		var C = new Controller(cid,tb);
		var vueObj = C.vueObj();
		
		
		//获取 就餐模式
		$.ajax({
			type:"post",
			url:"articlefamily/querydistributionMode",
			dataType:"json",
			success:function(data){
				vueObj.$set("distributionMode",data.data);
				vueObj.$set("selected",data.data[0].id);
			}
		})
		
	}());
	
</script>
