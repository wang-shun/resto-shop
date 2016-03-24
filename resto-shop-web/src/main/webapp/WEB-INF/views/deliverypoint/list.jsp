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
	            	<form role="form" action="{{m.id?'deliverypoint/modify':'deliverypoint/create'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
    <label>name</label>
    <input type="text" class="form-control" name="name" v-model="m.name">
</div>
<div class="form-group">
    <label>province</label>
    <input type="text" class="form-control" name="province" v-model="m.province">
</div>
<div class="form-group">
    <label>city</label>
    <input type="text" class="form-control" name="city" v-model="m.city">
</div>
<div class="form-group">
    <label>district</label>
    <input type="text" class="form-control" name="district" v-model="m.district">
</div>
<div class="form-group">
    <label>detail</label>
    <input type="text" class="form-control" name="detail" v-model="m.detail">
</div>
<div class="form-group">
    <label>mapX</label>
    <input type="text" class="form-control" name="mapX" v-model="m.mapX">
</div>
<div class="form-group">
    <label>mapY</label>
    <input type="text" class="form-control" name="mapY" v-model="m.mapY">
</div>
<div class="form-group">
    <label>empPhone</label>
    <input type="text" class="form-control" name="empPhone" v-model="m.empPhone">
</div>
<div class="form-group">
    <label>empName</label>
    <input type="text" class="form-control" name="empName" v-model="m.empName">
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
			<s:hasPermission name="deliverypoint/add">
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
				url : "deliverypoint/list_all",
				dataSrc : ""
			},
			columns : [
				{                 
	title : "name",
	data : "name",
},                 
{                 
	title : "province",
	data : "province",
},                 
{                 
	title : "city",
	data : "city",
},                 
{                 
	title : "district",
	data : "district",
},                 
{                 
	title : "detail",
	data : "detail",
},                 
{                 
	title : "mapX",
	data : "mapX",
},                 
{                 
	title : "mapY",
	data : "mapY",
},                 
{                 
	title : "empPhone",
	data : "empPhone",
},                 
{                 
	title : "empName",
	data : "empName",
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
							<s:hasPermission name="deliverypoint/delete">
							C.createDelBtn(tdData,"deliverypoint/delete"),
							</s:hasPermission>
							<s:hasPermission name="deliverypoint/edit">
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
