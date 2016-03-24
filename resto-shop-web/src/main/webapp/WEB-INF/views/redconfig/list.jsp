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
	            	<form role="form" action="{{m.id?'redconfig/modify':'redconfig/create'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
    <label>delay</label>
    <input type="text" class="form-control" name="delay" v-model="m.delay">
</div>
<div class="form-group">
    <label>minRatio</label>
    <input type="text" class="form-control" name="minRatio" v-model="m.minRatio">
</div>
<div class="form-group">
    <label>maxRatio</label>
    <input type="text" class="form-control" name="maxRatio" v-model="m.maxRatio">
</div>
<div class="form-group">
    <label>maxSingleRed</label>
    <input type="text" class="form-control" name="maxSingleRed" v-model="m.maxSingleRed">
</div>
<div class="form-group">
    <label>title</label>
    <input type="text" class="form-control" name="title" v-model="m.title">
</div>
<div class="form-group">
    <label>remark</label>
    <input type="text" class="form-control" name="remark" v-model="m.remark">
</div>
<div class="form-group">
    <label>minSignleRed</label>
    <input type="text" class="form-control" name="minSignleRed" v-model="m.minSignleRed">
</div>
<div class="form-group">
    <label>isAddRatio</label>
    <input type="text" class="form-control" name="isAddRatio" v-model="m.isAddRatio">
</div>
<div class="form-group">
    <label>minTranslateMoney</label>
    <input type="text" class="form-control" name="minTranslateMoney" v-model="m.minTranslateMoney">
</div>
<div class="form-group">
    <label>isActivity</label>
    <input type="text" class="form-control" name="isActivity" v-model="m.isActivity">
</div>
<div class="form-group">
    <label>shopDetailId</label>
    <input type="text" class="form-control" name="shopDetailId" v-model="m.shopDetailId">
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
			<s:hasPermission name="redconfig/add">
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
				url : "redconfig/list_all",
				dataSrc : ""
			},
			columns : [
				{                 
	title : "delay",
	data : "delay",
},                 
{                 
	title : "minRatio",
	data : "minRatio",
},                 
{                 
	title : "maxRatio",
	data : "maxRatio",
},                 
{                 
	title : "maxSingleRed",
	data : "maxSingleRed",
},                 
{                 
	title : "title",
	data : "title",
},                 
{                 
	title : "remark",
	data : "remark",
},                 
{                 
	title : "minSignleRed",
	data : "minSignleRed",
},                 
{                 
	title : "isAddRatio",
	data : "isAddRatio",
},                 
{                 
	title : "minTranslateMoney",
	data : "minTranslateMoney",
},                 
{                 
	title : "isActivity",
	data : "isActivity",
},                 
{                 
	title : "shopDetailId",
	data : "shopDetailId",
},                 

				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
						var operator=[
							<s:hasPermission name="redconfig/delete">
							C.createDelBtn(tdData,"redconfig/delete"),
							</s:hasPermission>
							<s:hasPermission name="redconfig/edit">
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
