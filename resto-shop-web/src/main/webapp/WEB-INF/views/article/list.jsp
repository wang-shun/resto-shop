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
	            	<form class="form-hr" role="form " action="{{m.id?'article/modify':'article/create'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
							    <label>餐品类别</label>
							    <select class="form-control" name="articleFamilyId" v-model="m.articleFamilyId">
							    	<option :value="f.id" v-for="f in articlefamilys">
							    		{{f.name}}
							    	</option>
							    </select>
							</div>
							<div class="form-group">
							    <label>餐品名称</label>
							    <input type="text" class="form-control" name="name" v-model="m.name">
							</div>
							<div class="form-group">
							    <label>价格</label>
							    <input type="text" class="form-control" name="price" v-model="m.price" required="required">
							</div>
							<div class="form-group">
							    <label>粉丝价</label>
							    <input type="text" class="form-control" name="fansPrice" v-model="fansPrice">
							</div>
							<div class="form-group">
							    <label>描述</label>
							    <textarea rows="3" class="form-control" name="description" v-model="m.description"></textarea>
							</div>
							<div class="form-group">
							    <label>排序</label>
							    <input type="number" class="form-control" name="sort" v-model="m.sort">
							</div>
							<div class="form-group">
							    <label>供应时间</label>
							    <br />
							    <label v-for="time in supporttimes">
							    	<input type="checkbox" name="supporttimes" :value="time.id"  v-model="checkedTimes"> {{time.name}} &nbsp;&nbsp;
							    </label>
							</div>
							<div class="form-group">
							    <label>餐品图片</label>
							    <img src="" id="photoSmall"/>
							    <input type="hidden" class="form-control" name="photoSmall" v-model="m.photoSmall">
							    <img-file-upload @success="uploadSuccess" @error="uploadError"></img-file-upload>
							</div>
							
							<div class="form-group">
							 	<div class="control-label">是否上架</div>
							    <label>
							    	<input type="radio" name="activated" value="1"  v-model="m.activated">是
							    </label>
							    
							    <label for="useWithAccount">
							    	 <input type="radio" name="activated" value="0"  v-model="m.activated">否
						    	</label>
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
			    	title:"餐品类别",
			    	data:"articleFamilyId",
			    },
				{                 
					title : "餐品名称",
					data : "name",
				},                 
				{                 
					title : "餐品图片",
					data : "photoSmall",
				},                 
				{                 
					title : "餐品描述",
					data : "description",
				},                 
				{                 
					title : "餐品排序",
					data : "sort",
				},                 
				{                 
					title : "是否上架",
					data : "activated",
					createdCell:function(td,tdData){
						$(td).html(tdData?"是":"否");
					}
				},                 
				{                 
					title : "所属店铺",
					data : "shopDetailId",
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
		
		var C = new Controller(null,tb);
		
		var vueObj = new Vue({
			el:"#control",
			mixins:[C.formVueMix],
			data:{
				articlefamilys:[],
				supporttimes:[],
				checkedTimes:[],
			},
			methods:{
				uploadSuccess:function(url){
					$("[name='photoSmall']").val(url).trigger("change");
					C.simpleMsg("上传成功");
					$("#photoSmall").attr("src","/"+url);
				},
				uploadError:function(msg){
					C.errorMsg(msg);
				}
			},
			created:function(){
				var that = this;
				$.post("articlefamily/list_all",null,function(data){
					that.articlefamilys = data;
				});
				$.post("supporttime/list_all",null,function(data){
					console.log(data);
					that.supporttimes=data;
				});
			}
		});
		C.vue=vueObj;
		
	}());
	
	

	
</script>
