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
	            	<form role="form" action="{{m.id?'smslog/modify':'smslog/create'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
    <label>phone</label>
    <input type="text" class="form-control" name="phone" v-model="m.phone">
</div>
<div class="form-group">
    <label>content</label>
    <input type="text" class="form-control" name="content" v-model="m.content">
</div>
<div class="form-group">
    <label>smsType</label>
    <input type="text" class="form-control" name="smsType" v-model="m.smsType">
</div>
<div class="form-group">
    <label>smsResult</label>
    <input type="text" class="form-control" name="smsResult" v-model="m.smsResult">
</div>
<div class="form-group">
    <label>shopDetailId</label>
    <input type="text" class="form-control" name="shopDetailId" v-model="m.shopDetailId">
</div>
<div class="form-group">
    <label>brandId</label>
    <input type="text" class="form-control" name="brandId" v-model="m.brandId">
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
			<s:hasPermission name="smslog/add">
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
				url : "smslog/list_all",
				dataSrc : ""
			},
			columns : [
				{                 
	title : "phone",
	data : "phone",
},                 
{                 
	title : "content",
	data : "content",
},                 
{                 
	title : "smsType",
	data : "smsType",
},                 
{                 
	title : "smsResult",
	data : "smsResult",
},                 
{                 
	title : "shopDetailId",
	data : "shopDetailId",
},                 
{                 
	title : "brandId",
	data : "brandId",
},                 

				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
						var operator=[
							<s:hasPermission name="smslog/delete">
							C.createDelBtn(tdData,"smslog/delete"),
							</s:hasPermission>
							<s:hasPermission name="smslog/edit">
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
