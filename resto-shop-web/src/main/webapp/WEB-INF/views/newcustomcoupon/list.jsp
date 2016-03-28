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
			    <label>活动名称</label>
			    <input type="text" class="form-control" name="name" v-model="m.name">
			</div>
			<div class="form-group">
			    <label>优惠券的价值</label>
			    <input type="text" class="form-control" name="couponValue" v-model="m.couponValue">
			</div>
			<div class="form-group">
			    <label>优惠券使用日期</label>
			    <input type="text" class="form-control" name="couponValiday" v-model="m.couponValiday">
			</div>
			<div class="form-group">
			    <label>优惠券的数量</label>
			    <input type="text" class="form-control" name="couponNumber" v-model="m.couponNumber">
			</div>
			
			 <div class="form-group">
			 	<div class="control-label">是否可以和余额一起使用</div>
			    <input type="radio" name="useWithAccount" v-model="m.useWithAccount" value=1>
			    <label for="useWithAccount">是</label>
			    <input type="radio" name="useWithAccount" v-model="m.useWithAccount" value=0>
			    <label for="useWithAccount">否</label>
			</div> 
			
			
			<div class="form-group">
			    <label>优惠券名字</label>
			    <input type="text" class="form-control" name="couponName" v-model="m.couponName">
			</div>
			<div class="form-group">
			    <label>最低消费额度</label>
			    <input type="text" class="form-control" name="couponMinMoney" v-model="m.couponMinMoney">
			</div>
			
			        
			<div class="form-group">
			    <label>开始时间</label>
			    <div class="input-group">
			    <input type="text" class="form-control timepicker timepicker-no-seconds" name="beginTime" v-model="m.beginTime" @focus="initTime">
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
			    <input type="text" class="form-control timepicker timepicker-no-seconds" name="endTime" v-model="m.endTime" @focus="initTime">
			    <span class="input-group-btn">
							<button class="btn default" type="button">
								<i class="fa fa-clock-o"></i>
							</button>
				</span>
			    </div>
			</div>
			
			
			
			<div class="form-group">
				<div class="control-label">选择是否启动优惠券</div>
			    <input type="radio"  name="isActivty" v-model="m.isActivty" value=1>
			    <label for="isActivty">是</label>
			    <input type="radio"  name="isActivty" v-model="m.isActivty" value=0> 
			    <label for="isActivty">否</label>
			</div>
			
			<div class="form-group">
			   <div class="control-label">选择店铺模式</div>
			   <div>
			   		<select class="form-control" name="distributionModeId">
			   			<option v-for="distributionMode in allDistributionMode" :value="distributionMode.id">{{distributionMode.name}}</option>
			   		</select>
			   
			   </div>
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
				$(document).ready(function(){ 
					var C;
					var vueObj;
					var cid="#control";
					var $table = $(".table-body>table");
					var tb = $table.DataTable({
						ajax : {
							url : "newcustomcoupon/list_all",
							dataSrc : ""
						},
						columns : [
							{                 
								title : "活动名称",
								data : "name",
							},                 
							{                 
								title : "优惠券价值",
								data : "couponValue",
							},                 
							{                 
								title : "优惠券有效期(天)",
								data : "couponValiday",
							},                 
							{                 
								title : "优惠券数量",
								data : "couponNumber",
							},                 
							{                 
								title : "是否可以和余额一起使用",
								data : "useWithAccount",
							},                 
							{                 
								title : "优惠券名称",
								data : "couponName",
							},                 
							{                 
								title : "优惠券最低消费额度",//默认0.00
								data : "couponMinMoney",
							},                 
							{                 
								title : "优惠券开始时间",//00:01:00
								data : "beginTime",
							},                 
							{                 
								title : "优惠券结束时间",//'23:59:00'
								data : "endTime",
							},                 
							{                 
								title : "是否启动",
								data : "isActivty",
							},                 
							{                 
								title : "品牌",
								data : "brandId",
							},                 
							{                 
								title : "配送模式",
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
					
					C = new Controller(cid,tb);
					var option = {
							el:cid,
							data:{
								m:{},
								shopModeInfo:{},
								showform:false,
								showtest:false,
							},
							methods:{
								openForm:function(){
									this.showform = true;
								},
								closeForm:function(){
									this.m={};
									this.showform = false;
								},
								cancel:function(){
									this.m={};
									this.closeForm();
								},
								create:function(){
									this.m={};
									this.openForm();
									Vue.nextTick(function () {
										vueObj.initdistributionMode();
									})
								},
								edit:function(model){
									this.m= model;
									this.openForm();
									Vue.nextTick(function(){
										vueObj.initdistributionMode();
									})
								},
								save:function(e){
									var that =this;
									 $.ajax({
										url:"newcustomcoupon/create",
										type:"post",
										data:$("form").serialize(),
										dataType:"json",
										success:function(data){
											that.cancel();
											tb.ajax.reload();
										}
									}) 
								},
								initdistributionMode :function(){
									$.ajax({
										url:"newcustomcoupon/distributionmode/list_all",
										type:"post",
										dataType:"json",
										success:function(result){
											if(result){
												var allDistributionMode=[];
												for(var i=0;i<result.length;i++){
													allDistributionMode[i]={"id":result[i].id,"name":result[i].name};
												}
												vueObj.$set("allDistributionMode",allDistributionMode)
											}
											
										}
										
									})
									
								},
								
								
								initTime :function(){
									$(".timepicker-no-seconds").timepicker({
										 autoclose: true,
							                minuteStep: 5
									    });
								},
								
							},
						};
					
					//var option
					vueObj = C.vueObj(option);
					
				}); 
				
	
</script>
