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
	            	<form role="form" action="{{m.id?'article/modify':'article/create'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
							    <label>articleFamilyId</label>
							    <input type="text" class="form-control" name="articleFamilyId" v-model="m.articleFamilyId">
							</div>
							<div class="form-group">
							    <label>name</label>
							    <input type="text" class="form-control" name="name" v-model="m.name">
							</div>
							<div class="form-group">
							    <label>price</label>
							    <input type="text" class="form-control" name="price" v-model="m.price">
							</div>
							<div class="form-group">
							    <label>price</label>
							    <input type="text" class="form-control" name="fans_price" v-model="fans_price">
							</div>
							<div class="form-group">
							    <label>description</label>
							    <textarea rows="3" class="form-control" name="description" v-model="m.description"></textarea>
							</div>
							<div class="form-group">
							    <label>sort</label>
							    <input type="text" class="form-control" name="sort" v-model="m.sort">
							</div>
							<div class="form-group">
							    <label>photoSmall</label>
							    <input type="text" class="form-control" name="photoSmall" v-model="m.photoSmall" readonly>
							    <img-file-upload @success="uploadSuccess" @error="uploadError"></img-file-upload>
							</div>
							<div class="form-group">
							    <label>activated</label>
							    <input type="text" class="form-control" name="activated" v-model="m.activated">
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
			<s:hasPermission name="article/add">
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
				url : "article/list_all",
				dataSrc : ""
			},
			columns : [
				{                 
					title : "name",
					data : "name",
				},                 
				{                 
					title : "nameAlias",
					data : "nameAlias",
				},                 
				{                 
					title : "nameShort",
					data : "nameShort",
				},                 
				{                 
					title : "photoBig",
					data : "photoBig",
				},                 
				{                 
					title : "photoSmall",
					data : "photoSmall",
				},                 
				{                 
					title : "ingredients",
					data : "ingredients",
				},                 
				{                 
					title : "description",
					data : "description",
				},                 
				{                 
					title : "isEmpty",
					data : "isEmpty",
				},                 
				{                 
					title : "sort",
					data : "sort",
				},                 
				{                 
					title : "activated",
					data : "activated",
				},                 
				{                 
					title : "state",
					data : "state",
				},                 
				{                 
					title : "remainNumber",
					data : "remainNumber",
				},                 
				{                 
					title : "saleNumber",
					data : "saleNumber",
				},                 
				{                 
					title : "showSaleNumber",
					data : "showSaleNumber",
				},                 
				{                 
					title : "showPrice",
					data : "showPrice",
				},                 
				{                 
					title : "updateTime",
					data : "updateTime",
				},                 
				{                 
					title : "shopDetailId",
					data : "shopDetailId",
				},                 
				{                 
					title : "articleFamilyId",
					data : "articleFamilyId",
				},                 
				{                 
					title : "createUserId",
					data : "createUserId",
				},                 
				{                 
					title : "updateUserId",
					data : "updateUserId",
				},                 

				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
						var operator=[
							<s:hasPermission name="article/delete">
							C.createDelBtn(tdData,"article/delete"),
							</s:hasPermission>
							<s:hasPermission name="article/edit">
							C.createEditBtn(rowData),
							</s:hasPermission>
						];
						$(td).html(operator);
					}
				}],
		});
		
		var C = new Controller(null,tb);
		
		var vueObj = new Vue({
			el:"#control",
			mixins:[C.formVueMix],
			methods:{
				uploadSuccess:function(url){
					$("[name='photoSmall']").val(url).trigger("change");
					C.simpleMsg("上传成功");
				},
				uploadError:function(msg){
					C.errorMsg(msg);
				}
			}
		});
		
	}());
	
	

	
</script>
