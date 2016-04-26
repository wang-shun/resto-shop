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
	            	<form role="form" action="{{m.id?'showphoto/modify':'showphoto/create'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
						    <label>图片的类型</label>
						    <input type="text" class="form-control" name="showType" v-model="m.showType">
						</div>
						<div class="form-group">
						    <label>主题</label>
						    <input type="text" class="form-control" name="title" v-model="m.title">
						</div>
						<div class="form-group">
						    <label>图片地址</label>
						    <input type="text" class="form-control" name="picUrl" v-model="m.picUrl">
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
			<s:hasPermission name="showphoto/add">
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
				url : "showphoto/list_all",
				dataSrc : ""
			},
			columns : [
								{                 
					title : "展示类型",
					data : "showType",
					createdCell:function(td,tdData,rowData,row){
						console.log(tdData);
						var typeName;
						if(tdData==1){
							typeName='餐品图片';
						}else if(tdData==2){
							typeName='展示的图片';
						}else if(tdData==4){
							typeName='差评';
						}
						$(td).html(typeName);
					}
				},                 
				{                 
					title : "主题",
					data : "title",
				},                 
				{                 
					title : "图片地址",
					data : "picUrl",
				},                 
				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
						var operator=[
							<s:hasPermission name="showphoto/delete">
							C.createDelBtn(tdData,"showphoto/delete"),
							</s:hasPermission>
							<s:hasPermission name="showphoto/edit">
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
