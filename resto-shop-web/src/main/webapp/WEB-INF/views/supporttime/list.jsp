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
		            <form role="form" action="{{m.id?'supporttime/modify':'supporttime/create'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
							    <label>名称</label>
							    <input type="text" class="form-control" name="name" v-model="m.name">
							</div>
							
							<!-- <div class="form-group">
							    <label>开始时间</label>
							    <input type="text" class="form-control" name="beginTime" v-model="m.beginTime">
							</div> -->
							<div class="form-group">
							    <label>开始时间</label>
							    <div class="input-group">
							    	<input type="text" class="form-control timepicker timepicker-no-seconds" name="beginTime" @focus="initTime" v-model="m.beginTime">
							    	<span class="input-group-btn">
										<button class="btn default" type="button">
											<i class="fa fa-clock-o"></i>
										</button>
									</span>
							    </div>
							</div>
							
							<div class="form-group">
							    <label>结束时间</label>
							    <div class="input-group">
							    	<input type="text" class="form-control timepicker timepicker-no-seconds" name="endTime" @focus="initTime" v-model="m.endTime">
							    	<span class="input-group-btn">
										<button class="btn default" type="button">
											<i class="fa fa-clock-o"></i>
										</button>
									</span>
							    </div>
							</div>
							<!-- <div class="form-group">
							    <label>供应时间</label>
							    <input type="text" class="form-control" name="supportWeekBin" v-model="m.supportWeekBin">
							</div> -->
							
							<div class="form-group">
								<label>供应时间</label>
								<br>
								<input type="checkbox" id="Sunday" value="64" v-model="checkedNames"  v-bind:true-value="a" v-bind:false-value="b">
									<label for="Sunday">周日</label>
								<input type="checkbox" id="Monday" value="1" v-model="checkedNames">
									<label for="Monday">周一</label>
								<input type="checkbox" id="Tuesday" value="2" v-model="checkedNames">
									<label for="Tuesday">周二</label>
								<input type="checkbox" id="Wednesday" value="4" v-model="checkedNames">
									<label for="Wednesday">周三</label>
								<input type="checkbox" id="Thursday" value="8" v-model="checkedNames">
									<label for="Thursday">周四</label>
								<input type="checkbox" id="Friday" value="16" v-model="checkedNames">
									<label for="Friday">周五</label>
								<input type="checkbox" id="Saturday" value="32" v-model="checkedNames">
									<label for="Saturday">周六</label>
									
								<input type="checkbox" id="workday" value="128" v-model="checkedNames">
									<label for="workday">工作日</label>
								<input type="checkbox" id="nonworkdays" value="256" v-model="checkedNames">
									<label for="nonworkdays">非工作日</label>
								<span>{{getSum}}</span>
								<input type="test" class="form-control" name="supportWeekBin" id="supportWeekBin" value="{{getSum}}">
							</div>
								
							<div class="form-group">
							    <label>描述</label>
							    <input type="text" class="form-control" name="remark" v-model="m.remark">
							</div>
							<!-- <div class="form-group">
							    <label>店铺id</label>
							    <input type="text" class="form-control" name="shopDetailId" v-model="m.shopDetailId">
							</div> -->
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
			<s:hasPermission name="supporttime/add">
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
	var vueObj;
	var C
	(function(){
		var cid="#control";
		var $table = $(".table-body>table");
		var tb = $table.DataTable({
			ajax : {
				url : "supporttime/list_all",
				dataSrc : ""
			},
			columns : [
				{                 
					title : "名称",
					data : "name",
				},                 
				{                 
					title : "开始时间",
					data : "beginTime",
				},                 
				{                 
					title : "结束时间",
					data : "endTime",
				},                 
				{                 
					title : "供应时间",
					data : "supportWeekBin",
				},                 
				{                 
					title : "描述",
					data : "remark",
				},                 
				{                 
					title : "门店",
					data : "shopName",
					
				},                 

				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
						var operator=[
							<s:hasPermission name="supporttime/delete">
							C.createDelBtn(tdData,"supporttime/delete"),
							</s:hasPermission>
							<s:hasPermission name="supporttime/edit">
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
			data:{
				checkedNames: [],
			},
			computed: {
			 	getSum: function () {
			 		var s=0;
			 		for(var i in this.checkedNames){
			 			var week = this.checkedNames[i];
			 			s+=parseInt(week);
			 		}
			      return s;
			    },
			},
			mixins:[C.formVueMix],
			methods:{ 
				initTime :function(){
					$(".timepicker-no-seconds").timepicker({
						 autoclose: true,
						 showMeridian:false,
			             minuteStep: 5
					  });
				},
				
				edit:function(model){
					// 选中
					vueObj.m= model;
					vueObj.openForm();
					Vue.nextTick(function(){
						var sb = vueObj.m.supportWeekBin;
						var i=sb.toString(2);
						//$("#supportWeekBin").val(i);
					
						
					})
				},
			}
		});
		C.vue=vueObj;
	}());
	
</script>
