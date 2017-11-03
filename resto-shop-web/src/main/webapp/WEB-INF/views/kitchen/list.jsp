<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div id="control">
	<div class="row form-div" v-if="showform">
		<div class="col-md-offset-3 col-md-6" >
			<div class="portlet light bordered">
	            <div class="portlet-title">
	                <div class="caption">
	                    <span class="caption-subject bold font-blue-hoki">新建厨房配置</span>
	                </div>
	            </div>
	            <div class="portlet-body">
	            	<form role="form" class="form-horizontal" action="{{m.id?'kitchen/modify':'kitchen/create'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
			           			<label class="col-sm-3 control-label">厨房名称：</label>
							    <div class="col-sm-8">
						    		<input type="text" class="form-control" required name="name" v-model="m.name">
							    </div>
							</div>
							<div class="form-group">
			           			<label class="col-sm-3 control-label">备注信息：</label>
							    <div class="col-sm-8">
							    	<input type="text" class="form-control" name="remark" v-model="m.remark">
							    </div>
							</div>
							<div class="form-group">
			           			<label class="col-sm-3 control-label">打印机名称：</label>
							    <div class="col-sm-8">
						    		<select class="form-control" name="printerId" required v-if="printerList" v-model="m.printerId?m.printerId:selected">
						    			<option v-for="temp in printerList" v-bind:value="temp.id">
						    				{{ temp.name }}
						    			</option>
						    		</select>
						    		<input class="form-control" value="暂无可用打印机" disabled="disabled" v-else />
							    </div>
							</div>
							<div class="text-center">
								<input type="hidden" name="id" v-model="m.id" />
								<input class="btn green"  type="submit" value="保存"/>
								<a class="btn default" @click="cancel" >取消</a>
							</div>
						</div>
					</form>
	            </div>
	        </div>
		</div>
	</div>
	
	<div class="table-div">
		<div class="table-operator">
			<s:hasPermission name="kitchen/add">
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
				url : "kitchen/list_all",
				dataSrc : ""
			},
			columns : [
				{                 
					title : "厨房名称",
					data : "name",
				},                 
				{                 
					title : "备注",
					data : "remark",
				},                 
				{                 
					title : "打印机名称",
					data : "printerName",
				},
				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
						var operator=[
							<s:hasPermission name="kitchen/delete">
							C.createDelBtn(tdData,"kitchen/delete"),
							</s:hasPermission>
							<s:hasPermission name="kitchen/edit">
							C.createEditBtn(rowData),
							</s:hasPermission>
						];
						$(td).html(operator);
					}
				}],
		});
		
		var C = new Controller(cid,tb);
		var vueObj = C.vueObj();
		
		//获取 就餐模式
		$.ajax({
			type:"post",
			url:"printer/list_all",
			dataType:"json",
			success:function(data){
			    console.log(JSON.stringify(data));
				if(data.length > 0){
					vueObj.$set("printerList",data);
					vueObj.$set("selected",data[0].id);
				}
			}
		})
	}());
	
	

	
</script>
