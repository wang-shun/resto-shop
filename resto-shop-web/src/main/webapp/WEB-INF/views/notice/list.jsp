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
			           			<label class="col-sm-3 control-label">通知名称：</label>
							    <div class="col-sm-8">
									<input type="text" class="form-control" name="title" v-model="m.title" required="required">
							    </div>
							</div>
			           		<div class="form-group">
			           			<label class="col-sm-3 control-label">通知内容：</label>
							    <div class="col-sm-8">
									<textarea class="form-control" name="content" v-model="m.content"></textarea>
							    </div>
							</div>
			           		<div class="form-group">
			           			<label class="col-sm-3 control-label">排序：</label>
							    <div class="col-sm-8">
									<input type="text" class="form-control" required  name="sort" v-model="m.sort">
							    </div>
							</div>
							
			           		<div class="form-group">
			           			<label class="col-sm-3 control-label">显示图片：</label>
							    <div class="col-sm-8">
							    	<img src="" id="noticeImage"/>
								    <input type="hidden" name="noticeImage" v-model="m.noticeImage">
								    <img-file-upload  class="form-control" @success="uploadSuccess" @error="uploadError"></img-file-upload>
							    </div>
							</div>
			           		<div class="form-group">
			           			<label class="col-sm-3 control-label">选择通知类型：</label>
							    <div class="col-sm-8 radio-list">
									<label class="radio-inline" v-for="(key, val) in noticeType">
										<input type="radio" name="noticeType" v-model="m.noticeType" :value="key">{{val}}
									</label>
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
					title : "排序",
					data : "sort",
				},                 
				{                 
					title : "图片",
					data : "noticeImage",
					defaultContent:'',
					createdCell:function(td,tdData){
						$(td).html("<image src='/"+tdData+"' style='height:40px;width:80px;'/>")
					}
				},                 
				{                 
					title : "通知类型",
					data : "noticeType",
					createdCell:function(td,data){
						$(td).html(vueObj.noticeType[data]);
					}
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
		
		var C = new Controller(null,tb);
		var vueObj = new Vue({
			el:"#control",
			mixins:[C.formVueMix],
			data:{
				noticeType: {
					1:"普通通知",
					3:"大图弹窗",
					2:"注册通知",
				}
			},
			methods:{
				create:function(){
					this.m ={
						sort:0,
						noticeType:1,
						title:"通知"+new Date().getTime(),
					};
					this.showform=true;
				},
				uploadSuccess:function(url){
					$("[name='noticeImage']").val(url).trigger("change");
					C.simpleMsg("上传成功");
					$("#noticeImage").attr("src","/"+url);
				},
				uploadError:function(msg){
					C.errorMsg(msg);
				},
			}
		});
		C.vue=vueObj;
	}());
</script>
