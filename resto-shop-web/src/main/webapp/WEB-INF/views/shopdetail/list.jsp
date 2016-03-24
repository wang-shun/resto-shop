<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>
<div id="control">
	<div class="row form-div" v-if="showform">
		<div class="col-md-offset-3 col-md-6">
			<div class="portlet light bordered">
				<div class="portlet-title">
					<div class="caption">
						<span class="caption-subject bold font-blue-hoki"> 表单</span>
					</div>
				</div>
				<div class="portlet-body">
					<form role="form"
						action="{{m.id?'shopdetail/modify':'shopdetail/create'}}"
						@submit.prevent="save">
						<!-- <div class="form-group">
						    <label class="col-sm-2 control-label">店铺名称</label>
						    <div class="col-sm-8">
						     <input type="text" class="form-control"
									name="name" v-model="m.name">
						    </div>
						</div> -->
						<div class="form-body">
						
						<div class="form-group">
									<div label for="brandName" class="control-label">请选择品牌模式</div>
									<div>
									<select class="form-control">
										<option v-for="brand in brands" value="brand.id">
											{{brand.brandName}}
										</option>
									</select>
									</div>
							</div>
							
							<div class="form-group">
								<div label for="shopeMode" class="control-label">选择店铺模式</div>
								<div>
									<select id="shopMode" class="form-control">
										<option v-for="mode in  allMode" value="mode.id">
											{{mode.name}}</option>
									</select>
								</div>
							</div>
						
							<div class="form-group">
								<label>店铺名称</label> 
								<input type="text" class="form-control" name="name" v-model="m.name">
							</div>
							
							<div class="form-group">
								<label>店铺描述</label> <input type="text" class="form-control"
									name="remark" v-model="m.remark">
							</div>
							
							<div class="form-group">
								<label>店铺电话</label> <input type="text" class="form-control"
									name="phone" v-model="m.phone">
							</div>
							
							<div class="form-group">
								<label>店铺地址</label> 
								<input type="text" class="form-control" name="address" v-model="m.address" @blur="showjwd">
							</div>
							
							<div class="form-group">
								<label>经度</label> <input type="text" class="form-control"
									name="longitude" v-model="m.longitude">
							</div>
							<div class="form-group">
								<label>纬度</label> <input type="text" class="form-control"
									name="latitude" v-model="m.latitude">
							</div>
							
							<%-- <div class="well">
							  <div id="datetimepicker3" class="input-append">
							    <input data-format="hh:mm:ss" type="text"></input>
							    <span class="add-on">
							      <i data-time-icon="icon-time" data-date-icon="icon-calendar">
							      </i>
							    </span>
							  </div>
							</div> --%>
							
							<div class="form-group">
								<label>营业开始时间</label> 
							  	<div id="datetimepicker3" class="input-append">
									    <input data-format="hh:mm:ss" type="text" class="form-control" name="openTime" v-model="m.openTime" @focus="initTime"></input>
									   <span class="input-group-btn">
                                              <button class="btn default" type="button" @click="initTime">
                                                  <i class="fa fa-clock-o"></i>
                                         </button>
                                      </span>
							 </div>
							</div>
							
							
							<div class="form-group">
								<label>结束时间</label> <input type="text" class="form-control"
									name="closeTime" v-model="m.closeTime">
							</div>
							
						</div>
						<input type="hidden" name="id" v-model="m.id" /> <input
							class="btn green" type="submit" value="保存" /> <a
							class="btn default" @click="cancel">取消</a>
					</form>
				</div>
			</div>
		</div>
	</div>

	<div class="table-div">
		<div class="table-operator">
			<s:hasPermission name="shopdetail/add">
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
	(function(){
		var cid="#control";
		var $table = $(".table-body>table");
		var tb = $table.DataTable({
			ajax : {
				url : "shopdetail/list_all",
				dataSrc : ""
			},
			columns : [
				{                 
					title : "店铺名称",
					data : "name",
				},                 
				{                 
					title : "店铺地址",
					data : "address",
				},                 
				{                 
					title : "经度",
					data : "longitude",
				},                 
				{                 
					title : "纬度",
					data : "latitude",
				},                 
				{                 
					title : "店铺电话",
					data : "phone",
				},                 
				{                 
					title : "店铺开门时间",
					data : "openTime",
				},                 
				{                 
					title : "店铺关门时间",
					data : "closeTime",
				},                 
				{                 
					title : "店铺状态",
					data : "status",
				},                 
				{                 
					title : "店铺描述",
					data : "remark",
				},                 
				{                 
					title : "店铺模式",
					data : "shopMode",
				},                 
				{                 
					title : "添加人",
					data : "addUser",
				},                 
				{                 
					title : "添加时间",
					data : "addTime",
				},
				{                 
					title : "更新人",
					data : "updateUser",
				},
				
				{                 
					title : "更新时间",
					data : "updateTime",
				},                 
				{                 
					title : "品牌",
					data : "brandId",
				},                 

				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
						var operator=[
										<s:hasPermission name="shopdetail/delete">
										C.createDelBtn(tdData,"shopdetail/delete"),
										</s:hasPermission>
										<s:hasPermission name="shopdetail/edit">
										C.createEditBtn(rowData),
										</s:hasPermission>
									];
						$(td).html(operator);
					}
				}],
		});
		
		var C = new Controller(cid,tb);
		
		var option = {
				el:cid,
				data:{
					m:{},
					showform:false
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
						alert();
						this.m={};
						this.openForm();
						//店铺模式下拉框
						initShopMode();
						//品牌下拉框
						initBrandMode();
					},
					edit:function(model){
						this.m= model;
						this.openForm();
					},
					save:function(e){
						/* var that = this;
						var formDom = e.target;
						_C.ajaxFormEx(formDom,function(){
							that.cancel();
							tb.ajax.reload();
						}); */
						alert("正在保存")
						$.ajax({
							url:"shopdetail/create",
							type:"post",
							data:$("form").serialize(),
							dataType:"json",
							success:function(data){
								tb.ajax.reload();
							}
						})
					},
					showjwd:function(){
						
						 // 百度地图API功能
						var map = new BMap.Map("allmap");
						var point = new BMap.Point(116.331398,39.897445);
						map.centerAndZoom(point,12);
						// 创建地址解析器实例
						var myGeo = new BMap.Geocoder();
						// 将地址解析结果显示在地图上,并调整地图视野
						myGeo.getPoint("北京市海淀区上地10街", function(point){
							if (point) {
								map.centerAndZoom(point, 16);
								map.addOverlay(new BMap.Marker(point));
								
							}else{
								alert("您选择地址没有解析到结果!");
							}
						}, "北京市");
						console.log(point);
						 this.m.longitude=point.lng;
						this.m.latitude=point.lat; 
						alert(this.m.longitude);
					},
					/*  $('.timepicker-no-seconds').timepicker({
			                autoclose: true,
			                minuteStep: 5
			            }); */

					initTime :function(){
						$('#datetimepicker3').timepicker({
							 autoclose: true,
				                minuteStep: 5
						    });
					} 
					
					
					
				},
			};
		
		vueObj = C.vueObj(option);
		
	}());
	
	function initShopMode(){
		$.ajax({
			url:"shopmode/list_all",
			type:"post",
			dataType:"json",
			success:function(data){
				if(data){
					var allMode = [];
					 for(var i=0;i<data.length;i++){
						allMode[i]= {"id":data[i].id,"name":data[i].name};
					} 
					 vueObj.$set("allMode",allMode);
				}
			}
			
		});
	
	}
	
	function initBrandMode(){
		$.ajax({
			url:"brand/list_all",
			type:"post",
		    dataType:"json",
		    success:function(data){
		    	if(data){
		    		var brands=[];
		    		for(var i=0;i<data.length;i++){
		    		brands[i]={"id":data[i].id,"brandName":data[i].brandName}
		    		}
		    		vueObj.$set("brands",brands)
		    		
		    	}
		    }
			
			
		})
		
	}
	
	
</script>
