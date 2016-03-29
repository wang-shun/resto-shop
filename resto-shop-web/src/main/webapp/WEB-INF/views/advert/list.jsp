<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>
<div id="control">
	<div class="row form-div" v-if="showform">
		<div class="col-md-offset-3 col-md-6">
			<div class="portlet light bordered">
				<div class="portlet-title">
					<div class="caption">
						<span class="caption-subject bold font-blue-hoki">新增店铺介绍</span>
					</div>
				</div>
				<div class="portlet-body">
					<form role="form" class="form-horizontal" action="{{m.id?'advert/modify':'advert/create'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
			           			<label class="col-sm-3 control-label">标题：</label>
							    <div class="col-sm-8">
									<input type="text" class="form-control" required name="slogan" v-model="m.slogan">
							    </div>
							</div>
							<div class="form-group">
			           			<label class="col-sm-3 control-label">描述：</label>
							    <div class="col-sm-8">
							    	<textarea class="form-control" required name="description" v-model="m.description"></textarea>
							    </div>
							</div>
							<div class="form-group">
			           			<label class="col-sm-3 control-label">状态：</label>
							    <div class="col-sm-8">
									<input type="text" class="form-control" required name="state" v-model="m.state">
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
			<s:hasPermission name="advert/add">
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
				url : "advert/list_all",
				dataSrc : ""
			},
			columns : [
				{                 
					title : "标题",
					data : "slogan",
				},             
				{                 
					title : "详情",
					data : "description",
				},                  
				{                 
					title : "状态",
					data : "state",
				},
				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
						var operator=[
							<s:hasPermission name="advert/delete">
							C.createDelBtn(tdData,"advert/delete"),
							</s:hasPermission>
							<s:hasPermission name="advert/edit">
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
