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
	            	<form role="form" action="{{m.id?'customer/modify':'customer/create'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
    <label>wechatId</label>
    <input type="text" class="form-control" name="wechatId" v-model="m.wechatId">
</div>
<div class="form-group">
    <label>nickname</label>
    <input type="text" class="form-control" name="nickname" v-model="m.nickname">
</div>
<div class="form-group">
    <label>telephone</label>
    <input type="text" class="form-control" name="telephone" v-model="m.telephone">
</div>
<div class="form-group">
    <label>headPhoto</label>
    <input type="text" class="form-control" name="headPhoto" v-model="m.headPhoto">
</div>
<div class="form-group">
    <label>defaultDeliveryPoint</label>
    <input type="text" class="form-control" name="defaultDeliveryPoint" v-model="m.defaultDeliveryPoint">
</div>
<div class="form-group">
    <label>isBindPhone</label>
    <input type="text" class="form-control" name="isBindPhone" v-model="m.isBindPhone">
</div>
<div class="form-group">
    <label>regiestTime</label>
    <input type="text" class="form-control" name="regiestTime" v-model="m.regiestTime">
</div>
<div class="form-group">
    <label>firstOrderTime</label>
    <input type="text" class="form-control" name="firstOrderTime" v-model="m.firstOrderTime">
</div>
<div class="form-group">
    <label>lastLoginTime</label>
    <input type="text" class="form-control" name="lastLoginTime" v-model="m.lastLoginTime">
</div>
<div class="form-group">
    <label>accountId</label>
    <input type="text" class="form-control" name="accountId" v-model="m.accountId">
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
			<s:hasPermission name="customer/add">
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
				url : "customer/list_all",
				dataSrc : ""
			},
			columns : [
				{                 
	title : "wechatId",
	data : "wechatId",
},                 
{                 
	title : "nickname",
	data : "nickname",
},                 
{                 
	title : "telephone",
	data : "telephone",
},                 
{                 
	title : "headPhoto",
	data : "headPhoto",
},                 
{                 
	title : "defaultDeliveryPoint",
	data : "defaultDeliveryPoint",
},                 
{                 
	title : "isBindPhone",
	data : "isBindPhone",
},                 
{                 
	title : "regiestTime",
	data : "regiestTime",
},                 
{                 
	title : "firstOrderTime",
	data : "firstOrderTime",
},                 
{                 
	title : "lastLoginTime",
	data : "lastLoginTime",
},                 
{                 
	title : "accountId",
	data : "accountId",
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
							<s:hasPermission name="customer/delete">
							C.createDelBtn(tdData,"customer/delete"),
							</s:hasPermission>
							<s:hasPermission name="customer/edit">
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
