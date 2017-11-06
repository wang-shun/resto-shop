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
	            	<form role="form" action="{{m.id?'scmDocPmsPoHeader/modify':'scmDocPmsPoHeader/create'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
    <label>supplierId</label>
    <input type="text" class="form-control" name="supplierId" v-model="m.supplierId">
</div>
<div class="form-group">
    <label>supPriceHeadId</label>
    <input type="text" class="form-control" name="supPriceHeadId" v-model="m.supPriceHeadId">
</div>
<div class="form-group">
    <label>shopDetailId</label>
    <input type="text" class="form-control" name="shopDetailId" v-model="m.shopDetailId">
</div>
<div class="form-group">
    <label>shopName</label>
    <input type="text" class="form-control" name="shopName" v-model="m.shopName">
</div>
<div class="form-group">
    <label>orderName</label>
    <input type="text" class="form-control" name="orderName" v-model="m.orderName">
</div>
<div class="form-group">
    <label>orderCode</label>
    <input type="text" class="form-control" name="orderCode" v-model="m.orderCode">
</div>
<div class="form-group">
    <label>orderStatus</label>
    <input type="text" class="form-control" name="orderStatus" v-model="m.orderStatus">
</div>
<div class="form-group">
    <label>createrId</label>
    <input type="text" class="form-control" name="createrId" v-model="m.createrId">
</div>
<div class="form-group">
    <label>createrName</label>
    <input type="text" class="form-control" name="createrName" v-model="m.createrName">
</div>
<div class="form-group">
    <label>gmtCreate</label>
    <input type="text" class="form-control" name="gmtCreate" v-model="m.gmtCreate">
</div>
<div class="form-group">
    <label>gmtModified</label>
    <input type="text" class="form-control" name="gmtModified" v-model="m.gmtModified">
</div>
<div class="form-group">
    <label>auditTime</label>
    <input type="text" class="form-control" name="auditTime" v-model="m.auditTime">
</div>
<div class="form-group">
    <label>auditName</label>
    <input type="text" class="form-control" name="auditName" v-model="m.auditName">
</div>
<div class="form-group">
    <label>updaterId</label>
    <input type="text" class="form-control" name="updaterId" v-model="m.updaterId">
</div>
<div class="form-group">
    <label>updaterName</label>
    <input type="text" class="form-control" name="updaterName" v-model="m.updaterName">
</div>
<div class="form-group">
    <label>note</label>
    <input type="text" class="form-control" name="note" v-model="m.note">
</div>
<div class="form-group">
    <label>isDelete</label>
    <input type="text" class="form-control" name="isDelete" v-model="m.isDelete">
</div>
<div class="form-group">
    <label>tax</label>
    <input type="text" class="form-control" name="tax" v-model="m.tax">
</div>
<div class="form-group">
    <label>totalAmount</label>
    <input type="text" class="form-control" name="totalAmount" v-model="m.totalAmount">
</div>
<div class="form-group">
    <label>expectTime</label>
    <input type="text" class="form-control" name="expectTime" v-model="m.expectTime">
</div>
<div class="form-group">
    <label>payStatus</label>
    <input type="text" class="form-control" name="payStatus" v-model="m.payStatus">
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
			<s:hasPermission name="scmDocPmsPoHeader/add">
			<button class="btn green pull-right" @click="create">新建</button>
			</s:hasPermission>
		</div>
		<div class="clearfix"></div>
		<div class="table-filter"></div>
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
				url : "scmDocPmsPoHeader/list_all",
				dataSrc : ""
			},
			columns : [
				{                 
	title : "supplierId",
	data : "supplierId",
},                 
{                 
	title : "supPriceHeadId",
	data : "supPriceHeadId",
},                 
{                 
	title : "shopDetailId",
	data : "shopDetailId",
},                 
{                 
	title : "shopName",
	data : "shopName",
},                 
{                 
	title : "orderName",
	data : "orderName",
},                 
{                 
	title : "orderCode",
	data : "orderCode",
},                 
{                 
	title : "orderStatus",
	data : "orderStatus",
},                 
{                 
	title : "createrId",
	data : "createrId",
},                 
{                 
	title : "createrName",
	data : "createrName",
},                 
{                 
	title : "gmtCreate",
	data : "gmtCreate",
},                 
{                 
	title : "gmtModified",
	data : "gmtModified",
},                 
{                 
	title : "auditTime",
	data : "auditTime",
},                 
{                 
	title : "auditName",
	data : "auditName",
},                 
{                 
	title : "updaterId",
	data : "updaterId",
},                 
{                 
	title : "updaterName",
	data : "updaterName",
},                 
{                 
	title : "note",
	data : "note",
},                 
{                 
	title : "isDelete",
	data : "isDelete",
},                 
{                 
	title : "tax",
	data : "tax",
},                 
{                 
	title : "totalAmount",
	data : "totalAmount",
},                 
{                 
	title : "expectTime",
	data : "expectTime",
},                 
{                 
	title : "payStatus",
	data : "payStatus",
},                 

				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
						var operator=[
							<s:hasPermission name="scmDocPmsPoHeader/delete">
							C.createDelBtn(tdData,"scmDocPmsPoHeader/delete"),
							</s:hasPermission>
							<s:hasPermission name="scmDocPmsPoHeader/modify">
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
			mixins:[C.formVueMix]
		});
		C.vue=vueObj;
	}());
	
	

	
</script>
