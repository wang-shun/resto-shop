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
	            	<form role="form" action="{{m.id?'wechattemptype/modify':'wechattemptype/create'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
    <label>typeName</label>
    <input type="text" class="form-control" name="typeName" v-model="m.typeName">
</div>
<div class="form-group">
    <label>typeSign</label>
    <input type="text" class="form-control" name="typeSign" v-model="m.typeSign">
</div>
<div class="form-group">
    <label>typeJson</label>
    <input type="text" class="form-control" name="typeJson" v-model="m.typeJson">
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
			<s:hasPermission name="wechattemptype/add">
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
				url : "wechattemptype/list_all",
				dataSrc : ""
			},
			columns : [
				{                 
	title : "typeName",
	data : "typeName",
},                 
{                 
	title : "typeSign",
	data : "typeSign",
},                 
{                 
	title : "typeJson",
	data : "typeJson",
},                 

				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
						var operator=[
							<s:hasPermission name="wechattemptype/delete">
							C.createDelBtn(tdData,"wechattemptype/delete"),
							</s:hasPermission>
							<s:hasPermission name="wechattemptype/edit">
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
