<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>
<div id="control">
	<div class="row form-div" v-if="showform">
		<div class="col-md-offset-3 col-md-6">
			<div class="portlet light bordered">
				<div class="portlet-title">
					<div class="caption">
						<span class="caption-subject bold font-blue-hoki">新建通知</span>
					</div>
				</div>
				<div class="portlet-body">
					<form role="form" class="form-horizontal" action="{{m.id?'notice/modify':'notice/create'}}" @submit.prevent="save">
						<div class="form-body">
			           		<div class="form-group">
			           			<label class="col-sm-3 control-label">通知标题：</label>
							    <div class="col-sm-8">
									<input type="text" class="form-control" required  name="title" v-model="m.title">
							    </div>
							</div>
			           		<div class="form-group">
			           			<label class="col-sm-3 control-label">通知内容：</label>
							    <div class="col-sm-8">
									<textarea class="form-control" name="content" required v-model="m.content"></textarea>
							    </div>
							</div>
			           		<div class="form-group">
			           			<label class="col-sm-3 control-label">排序方式：</label>
							    <div class="col-sm-8">
									<select  class="form-control" name="sort" required  v-model="m.sort">
										<option value="1" selected="selected">1</option>
									</select>
							    </div>
							</div>
			           		<div class="form-group">
			           			<label class="col-sm-3 control-label">通知状态：</label>
							    <div class="col-sm-8"> 
									<input type="number" class="form-control" required placeholder="请输入数字！" name="status" v-model="m.status">
							    </div>
							</div>
			           		<div class="form-group">
			           			<label class="col-sm-3 control-label">显示图片：</label>
							    <div class="col-sm-8">
									<input type="text" class="form-control" required name="noticeImage" v-model="m.noticeImage">
							    </div>
							</div>
			           		<div class="form-group">
			           			<label class="col-sm-3 control-label">通知类型：</label>
							    <div class="col-sm-8">
									<select class="form-control" name="noticeType" required v-model="m.noticeType">
										<option value="1" selected="selected">1</option>
									</select>
							    </div>
							</div>
						</div>
						<div class="text-center">
							<input type="hidden" name="id" v-model="m.id" />
							<input class="btn green" type="submit" value="保存" />
							<a class="btn default" @click="cancel">取消</a>
						</div>
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
					title : "通知标题",
					data : "title",
				},                 
				{                 
					title : "通知内容",
					data : "content",
				},                 
				{                 
					title : "创建时间",
					data : "createDate",
					createdCell:function(td,tdData,rowData,row){
						var temp = new Date(tdData);
						temp  = temp.format("yyyy-MM-dd hh:mm:ss");
						$(td).html(temp);
					}
				},                 
				{                 
					title : "排序方式",
					data : "sort",
				},                 
				{                 
					title : "通知状态",
					data : "status",
				},                 
				{                 
					title : "图片",
					data : "noticeImage",
				},                 
				{                 
					title : "通知类型",
					data : "noticeType",
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
