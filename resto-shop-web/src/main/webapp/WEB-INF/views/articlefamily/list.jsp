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
	            	<form role="form" action="{{m.id?'articlefamily/modify':'articlefamily/create'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
    <label>name</label>
    <input type="text" class="form-control" name="name" v-model="m.name">
</div>
<div class="form-group">
    <label>peference</label>
    <input type="text" class="form-control" name="peference" v-model="m.peference">
</div>
<div class="form-group">
    <label>parentId</label>
    <input type="text" class="form-control" name="parentId" v-model="m.parentId">
</div>
<div class="form-group">
    <label>level</label>
    <input type="text" class="form-control" name="level" v-model="m.level">
</div>
<div class="form-group">
    <label>shopDetailId</label>
    <input type="text" class="form-control" name="shopDetailId" v-model="m.shopDetailId">
</div>
<div class="form-group">
    <label>distributionModeId</label>
    <input type="text" class="form-control" name="distributionModeId" v-model="m.distributionModeId">
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
			<s:hasPermission name="articlefamily/add">
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
				url : "articlefamily/list_all",
				dataSrc : ""
			},
			columns : [
				{                 
	title : "name",
	data : "name",
},                 
{                 
	title : "peference",
	data : "peference",
},                 
{                 
	title : "parentId",
	data : "parentId",
},                 
{                 
	title : "level",
	data : "level",
},                 
{                 
	title : "shopDetailId",
	data : "shopDetailId",
},                 
{                 
	title : "distributionModeId",
	data : "distributionModeId",
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
	}());
	
	

	
</script>
