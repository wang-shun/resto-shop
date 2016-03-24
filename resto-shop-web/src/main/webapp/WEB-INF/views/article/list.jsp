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
    <label>name</label>
    <input type="text" class="form-control" name="name" v-model="m.name">
</div>
<div class="form-group">
    <label>nameAlias</label>
    <input type="text" class="form-control" name="nameAlias" v-model="m.nameAlias">
</div>
<div class="form-group">
    <label>nameShort</label>
    <input type="text" class="form-control" name="nameShort" v-model="m.nameShort">
</div>
<div class="form-group">
    <label>photoBig</label>
    <input type="text" class="form-control" name="photoBig" v-model="m.photoBig">
</div>
<div class="form-group">
    <label>photoSmall</label>
    <input type="text" class="form-control" name="photoSmall" v-model="m.photoSmall">
</div>
<div class="form-group">
    <label>ingredients</label>
    <input type="text" class="form-control" name="ingredients" v-model="m.ingredients">
</div>
<div class="form-group">
    <label>description</label>
    <input type="text" class="form-control" name="description" v-model="m.description">
</div>
<div class="form-group">
    <label>isEmpty</label>
    <input type="text" class="form-control" name="isEmpty" v-model="m.isEmpty">
</div>
<div class="form-group">
    <label>sort</label>
    <input type="text" class="form-control" name="sort" v-model="m.sort">
</div>
<div class="form-group">
    <label>activated</label>
    <input type="text" class="form-control" name="activated" v-model="m.activated">
</div>
<div class="form-group">
    <label>state</label>
    <input type="text" class="form-control" name="state" v-model="m.state">
</div>
<div class="form-group">
    <label>remainNumber</label>
    <input type="text" class="form-control" name="remainNumber" v-model="m.remainNumber">
</div>
<div class="form-group">
    <label>saleNumber</label>
    <input type="text" class="form-control" name="saleNumber" v-model="m.saleNumber">
</div>
<div class="form-group">
    <label>showSaleNumber</label>
    <input type="text" class="form-control" name="showSaleNumber" v-model="m.showSaleNumber">
</div>
<div class="form-group">
    <label>showPrice</label>
    <input type="text" class="form-control" name="showPrice" v-model="m.showPrice">
</div>
<div class="form-group">
    <label>updateTime</label>
    <input type="text" class="form-control" name="updateTime" v-model="m.updateTime">
</div>
<div class="form-group">
    <label>shopDetailId</label>
    <input type="text" class="form-control" name="shopDetailId" v-model="m.shopDetailId">
</div>
<div class="form-group">
    <label>articleFamilyId</label>
    <input type="text" class="form-control" name="articleFamilyId" v-model="m.articleFamilyId">
</div>
<div class="form-group">
    <label>createUserId</label>
    <input type="text" class="form-control" name="createUserId" v-model="m.createUserId">
</div>
<div class="form-group">
    <label>updateUserId</label>
    <input type="text" class="form-control" name="updateUserId" v-model="m.updateUserId">
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
		
		var C = new Controller(cid,tb);
		var vueObj = C.vueObj();
	}());
	
	

	
</script>
