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
	            	<form role="form" action="{{m.id?'notice/modify':'notice/create'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
    <label>title</label>
    <input type="text" class="form-control" name="title" v-model="m.title">
</div>
<div class="form-group">
    <label>content</label>
    <input type="text" class="form-control" name="content" v-model="m.content">
</div>
<div class="form-group">
    <label>createDate</label>
    <input type="text" class="form-control" name="createDate" v-model="m.createDate">
</div>
<div class="form-group">
    <label>sort</label>
    <input type="text" class="form-control" name="sort" v-model="m.sort">
</div>
<div class="form-group">
    <label>status</label>
    <input type="text" class="form-control" name="status" v-model="m.status">
</div>
<div class="form-group">
    <label>noticeImage</label>
    <input type="text" class="form-control" name="noticeImage" v-model="m.noticeImage">
</div>
<div class="form-group">
    <label>noticeType</label>
    <input type="text" class="form-control" name="noticeType" v-model="m.noticeType">
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
			<s:hasPermission name="notice/add">
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
				url : "notice/list_all",
				dataSrc : ""
			},
			columns : [
				{                 
	title : "title",
	data : "title",
},                 
{                 
	title : "content",
	data : "content",
},                 
{                 
	title : "createDate",
	data : "createDate",
},                 
{                 
	title : "sort",
	data : "sort",
},                 
{                 
	title : "status",
	data : "status",
},                 
{                 
	title : "noticeImage",
	data : "noticeImage",
},                 
{                 
	title : "noticeType",
	data : "noticeType",
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
							<s:hasPermission name="notice/delete">
							C.createDelBtn(tdData,"notice/delete"),
							</s:hasPermission>
							<s:hasPermission name="notice/edit">
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
