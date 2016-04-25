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
	            	<form role="form" id="newcustomcoupn" class="newcustomform" action="{{m.id?'newcustomcoupon/modify':'newcustomcoupon/create'}}" @submit.prevent="save">
	            	
		<div class="form-body">
			<div class="form-group">
			    <label>活动名称</label>
			    <input type="text" class="form-control"  name="name" v-model="m.name">
			</div>
			<div class="form-group">
			    <label>优惠券价值</label>
			    	<div class="input-group">
			    		 <input type="text" class="form-control" name="couponValue" v-model="m.couponValue" placeholder="请输入数字" required  min="0">
			    		 <div class="input-group-addon">
			    		 	<span class="glyphicon glyphicon-yen" aria-hidden="true"></span>
			    		 </div>
			    </div>
			   
			</div>
			<div class="form-group">
			    <label>优惠券有效日期</label>
			    <input type="number" class="form-control" name="couponValiday" v-model="m.couponValiday" placeholder="请输入数字" required min="0">
			</div>
			<div class="form-group">
			    <label>优惠券的数量</label>
			    <input type="text" class="form-control" name="couponNumber" v-model="m.couponNumber" placeholder="请输入数字" required min="0">
			</div>
			
			 <div class="form-group">
			 	<div class="control-label">是否可以和余额一起使用</div>
			    <input type="radio" name="useWithAccount" v-model="m.useWithAccount" value=1 v-if="m.id">
			    <input type="radio" name="useWithAccount"  value=1 v-if="!m.id" checked="checked">
			    <label for="useWithAccount">是</label>
			    <input type="radio" name="useWithAccount" v-model="m.useWithAccount" value=0 v-if="m.id">
			    <input type="radio" name="useWithAccount"  value=0 v-if="!m.id">
			    <label for="useWithAccount">否</label>
			</div> 
			
			<div class="form-group">
			    <label>优惠券名字</label>
			    <input type="text" class="form-control" name="couponName" v-model="m.couponName">
			</div>
			<div class="form-group">
			    <label>最低消费额度</label>
			    <input type="text" class="form-control" placeholder="请输入数字"  min="0" required name="couponMinMoney" v-model="m.couponMinMoney">
			</div>
			
			<div class="form-group">
			    <label>开始时间</label>
			    <div class="input-group">
			    <input type="text" class="form-control timepicker timepicker-no-seconds" name="beginTime" v-model="m.beginTime" @focus="initTime" readonly>
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
			    <input type="text" class="form-control timepicker timepicker-no-seconds" name="endTime" v-model="m.endTime" @focus="initTime" readonly>
			    <span class="input-group-btn">
					<button class="btn default" type="button">
						<i class="fa fa-clock-o"></i>
					</button>
				</span>
			    </div>
			</div>
			
			<div class="form-group">
				<div class="control-label">选择是否启动优惠券</div>
			    <input type="radio"  name="isActivty" v-model="m.isActivty" value=1 v-if="m.id">
			    <input type="radio"  name="isActivty"  value=1  v-if="!m.id" checked="checked">
			    <label for="isActivty">是</label>
			    <input type="radio"  name="isActivty" v-model="m.isActivty" value=0 v-if="m.id"> 
			    <input type="radio"  name="isActivty" value=0 v-if="!m.id"> 
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
								"render":function(data){
									return (new Date(data).format('hh:mm:ss'))
								}
							},                 
							{                 
								title : "优惠券结束时间",//'23:59:00'
								data : "endTime",
								"render":function(data){
									return (new Date(data)).format('hh:mm:ss');
								}
							},                 
							{                 
								title : "是否启动",
								data : "isActivty",
								"render":function(data){
									if(data==1){
										data='是';
									}else if(data==0){
										data='否';
									}else{
										data='未知';
									}
									return data;
								}
							},                 
							{                 
								title : "配送模式",
								data : "distributionModeId",
								createdCell:function(td,tdData,rowData,row){
									$.ajax({
										data:{"id":tdData},
										type:"post",
										dataType:"json",
										url:"newcustomcoupon/distributionMode/list_one",
										success:function(result){
											console.log(result);
											$(td).html(result.name);
										}
										
										
									})
									
								}
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
								showform:false,
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
									//格式时间
									var tem1 = this.m.beginTime;
									var tem2 = this.m.endTime;
									var begin;
									var end;
									begin=new Date(tem1).format("hh:mm");
									end = new Date(tem2).format("hh:mm");
									if(begin=='aN:aN'){
										begin = tem1;
									}
									if(end=='aN:aN'){
										end=tem2;
									}
									this.m.beginTime = begin;
									this.m.endTime = end;
									console.log(this.m.openTime);
									this.openForm();
									Vue.nextTick(function(){
										vueObj.initdistributionMode();
									})
								},
								save:function(e){
									var that = this;
										var formDom = e.target;
										C.ajaxFormEx(formDom,function(){
											that.cancel();
											tb.ajax.reload();
										});
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
										 showMeridian:false,
										 showInputs:false,
							             minuteStep: 5
									    });
								},
								
							},
						};
					
					//var option
					vueObj = C.vueObj(option);
					
				}); 
				
</script>
