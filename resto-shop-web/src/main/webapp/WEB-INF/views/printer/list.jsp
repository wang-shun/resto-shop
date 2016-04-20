<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div id="control">
	<div class="row form-div" v-if="showform">
		<div class="col-md-offset-3 col-md-6" >
			<div class="portlet light bordered">
	            <div class="portlet-title">
	                <div class="caption">
	                    <span class="caption-subject bold font-blue-hoki">新建打印机</span>
	                </div>
	            </div>
	            <div class="portlet-body">
	            	<form role="form" class="form-horizontal" action="{{m.id?'printer/modify':'printer/create'}}" @submit.prevent="save">
						<div class="form-body">
			           		<div class="form-group">
			           			<label class="col-sm-3 control-label">打印机名称：</label>
							    <div class="col-sm-8">
							    	<input type="text" class="form-control" required name="name" v-model="m.name">
							    </div>
							</div>
			           		<div class="form-group">
			           			<label class="col-sm-3 control-label">I&nbsp;P&nbsp;地址：</label>
							    <div class="col-sm-8">
							   		<input type="text" class="form-control" required name="ip" v-model="m.ip">
							    </div>
							</div>
			           		<div class="form-group">
			           			<label class="col-sm-3 control-label">端&nbsp;口&nbsp;号：</label>
							    <div class="col-sm-8">
<!-- 							    	<input type="number" class="form-control" required placeholder="请输入数字!" name="port" v-model="m.port"> -->
							    	<input type="text" class="form-control" required  name="port" v-model="m.port">
							    </div>
							</div>
							
							<div class="form-group">
								<div class="col-sm-3 control-label">打印机类型：</div>
							    <input type="radio"  name="printType" v-model="m.printType" value=1 checked="checked">
							    <label for="printType">厨房</label>
							    <input type="radio"  name="printType" v-model="m.printType" value=2> 
							    <label for="printType">前台</label>
							    <input type="radio"  name="printType" v-model="m.printType" value=3> 
							    <label for="printType">打包</label>
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
			<s:hasPermission name="printer/add">
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
				url : "printer/list_all",
				dataSrc : ""
			},
			columns : [
				{                 
					title : "打印机名称",
					data : "name",
				},                 
				{                 
					title : "IP地址",
					data : "ip",
				},                 
				{                 
					title : "端口号",
					data : "port",
				},
				{                 
					title : "打印机类型",
					data : "printType",
					createdCell:function(td,tdData,rowData,row){
						switch(tdData){
						case 1:
							$(td).html('厨房');
						break;
						case 2:
							$(td).html('前台');
						break;
						case 3:
							$(td).html('打包');
						break;
						default:
							$(td).html('未知');
						}
					}
				},
				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
						var operator=[
							<s:hasPermission name="printer/delete">
							C.createDelBtn(tdData,"printer/delete"),
							</s:hasPermission>
							<s:hasPermission name="printer/edit">
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
