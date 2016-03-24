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
	            	<form role="form" action="{{m.id?'appraise/modify':'appraise/create'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
    <label>pictureUrl</label>
    <input type="text" class="form-control" name="pictureUrl" v-model="m.pictureUrl">
</div>
<div class="form-group">
    <label>level</label>
    <input type="text" class="form-control" name="level" v-model="m.level">
</div>
<div class="form-group">
    <label>content</label>
    <input type="text" class="form-control" name="content" v-model="m.content">
</div>
<div class="form-group">
    <label>status</label>
    <input type="text" class="form-control" name="status" v-model="m.status">
</div>
<div class="form-group">
    <label>type</label>
    <input type="text" class="form-control" name="type" v-model="m.type">
</div>
<div class="form-group">
    <label>feedback</label>
    <input type="text" class="form-control" name="feedback" v-model="m.feedback">
</div>
<div class="form-group">
    <label>redMoney</label>
    <input type="text" class="form-control" name="redMoney" v-model="m.redMoney">
</div>
<div class="form-group">
    <label>customerId</label>
    <input type="text" class="form-control" name="customerId" v-model="m.customerId">
</div>
<div class="form-group">
    <label>orderId</label>
    <input type="text" class="form-control" name="orderId" v-model="m.orderId">
</div>
<div class="form-group">
    <label>articleId</label>
    <input type="text" class="form-control" name="articleId" v-model="m.articleId">
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
			<s:hasPermission name="appraise/add">
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
				url : "appraise/list_all",
				dataSrc : ""
			},
			columns : [
				{                 
	title : "pictureUrl",
	data : "pictureUrl",
},                 
{                 
	title : "level",
	data : "level",
},                 
{                 
	title : "content",
	data : "content",
},                 
{                 
	title : "status",
	data : "status",
},                 
{                 
	title : "type",
	data : "type",
},                 
{                 
	title : "feedback",
	data : "feedback",
},                 
{                 
	title : "redMoney",
	data : "redMoney",
},                 
{                 
	title : "customerId",
	data : "customerId",
},                 
{                 
	title : "orderId",
	data : "orderId",
},                 
{                 
	title : "articleId",
	data : "articleId",
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
							<s:hasPermission name="appraise/delete">
							C.createDelBtn(tdData,"appraise/delete"),
							</s:hasPermission>
							<s:hasPermission name="appraise/edit">
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
