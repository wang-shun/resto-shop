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
	            	<form role="form" action="{{m.id?'newcustomcoupon/modify':'newcustomcoupon/create'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
    <label>name</label>
    <input type="text" class="form-control" name="name" v-model="m.name">
</div>
<div class="form-group">
    <label>couponValue</label>
    <input type="text" class="form-control" name="couponValue" v-model="m.couponValue">
</div>
<div class="form-group">
    <label>couponValiday</label>
    <input type="text" class="form-control" name="couponValiday" v-model="m.couponValiday">
</div>
<div class="form-group">
    <label>couponNumber</label>
    <input type="text" class="form-control" name="couponNumber" v-model="m.couponNumber">
</div>
<div class="form-group">
    <label>useWithAccount</label>
    <input type="text" class="form-control" name="useWithAccount" v-model="m.useWithAccount">
</div>
<div class="form-group">
    <label>couponName</label>
    <input type="text" class="form-control" name="couponName" v-model="m.couponName">
</div>
<div class="form-group">
    <label>couponMinMoney</label>
    <input type="text" class="form-control" name="couponMinMoney" v-model="m.couponMinMoney">
</div>
<div class="form-group">
    <label>beginTime</label>
    <input type="text" class="form-control" name="beginTime" v-model="m.beginTime">
</div>
<div class="form-group">
    <label>endTime</label>
    <input type="text" class="form-control" name="endTime" v-model="m.endTime">
</div>
<div class="form-group">
    <label>isActivty</label>
    <input type="text" class="form-control" name="isActivty" v-model="m.isActivty">
</div>
<div class="form-group">
    <label>brandId</label>
    <input type="text" class="form-control" name="brandId" v-model="m.brandId">
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
			<s:hasPermission name="newcustomcoupon/add">
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
				url : "newcustomcoupon/list_all",
				dataSrc : ""
			},
			columns : [
				{                 
	title : "name",
	data : "name",
},                 
{                 
	title : "couponValue",
	data : "couponValue",
},                 
{                 
	title : "couponValiday",
	data : "couponValiday",
},                 
{                 
	title : "couponNumber",
	data : "couponNumber",
},                 
{                 
	title : "useWithAccount",
	data : "useWithAccount",
},                 
{                 
	title : "couponName",
	data : "couponName",
},                 
{                 
	title : "couponMinMoney",
	data : "couponMinMoney",
},                 
{                 
	title : "beginTime",
	data : "beginTime",
},                 
{                 
	title : "endTime",
	data : "endTime",
},                 
{                 
	title : "isActivty",
	data : "isActivty",
},                 
{                 
	title : "brandId",
	data : "brandId",
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
							<s:hasPermission name="newcustomcoupon/delete">
							C.createDelBtn(tdData,"newcustomcoupon/delete"),
							</s:hasPermission>
							<s:hasPermission name="newcustomcoupon/edit">
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
