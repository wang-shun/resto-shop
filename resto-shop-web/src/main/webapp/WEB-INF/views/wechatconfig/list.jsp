<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>
<div id="control">
	<div class="row form-div" v-if="showform">
		<div class="col-md-offset-3 col-md-6">
			<div class="portlet light bordered">
				<div class="portlet-title">
					<div class="caption">
						<span class="caption-subject bold font-blue-hoki">微信配置信息</span>
					</div>
				</div>
				<div class="portlet-body">
					<form class="form-horizontal" role="form" action="{{m.id?'wechatconfig/modify':'wechatconfig/create'}}" @submit.prevent="save">
				  		<div class="form-body">
							<div class="form-group">
							  <label class="col-sm-3 control-label">AppID：</label>
							  <div class="col-sm-8">
							    <input type="text" class="form-control" name="appid" v-model="m.appid">
							  </div>
							</div>
							<div class="form-group">
							  <label class="col-sm-3 control-label">AppSecret：</label>
							  <div class="col-sm-8">
							    <input type="text" class="form-control" name="appsecret" v-model="m.appsecret">
							  </div>
							</div>
							<div class="form-group">
							  <label class="col-sm-3 control-label">支付ID：</label>
							  <div class="col-sm-8">
							    <input type="text" class="form-control" name="mchid" v-model="m.mchid">
							  </div>
							</div>
							<div class="form-group">
							  <label class="col-sm-3 control-label">支付秘钥：</label>
							  <div class="col-sm-8">
							   	<input type="text" class="form-control" name="mchkey" v-model="m.mchkey">
							  </div>
							</div>
							<div class="form-group text-center" v-if="errorShow">
								<label class="control-label"><font color="red">{{ errorMsg }}</font></label>
							</div>
							<div class="form-group text-center">
								<input type="hidden" name="id" v-model="m.id" />
							  	<input class="btn green" type="submit" value="保存"/>
							  	<a class="btn default" @click="cancel">取消</a>
							</div>
						</div>
					</form>
				</div>
			</div>
		</div>
	</div>

	<div class="table-div">
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
				url : "wechatconfig/list_all",
				dataSrc : ""
			},
			columns : [                 
				{                 
					title : "品牌名称",
					data : "brandName",
				},
				{                 
					title : "AppID",
					data : "appid",
				},                 
				{                 
					title : "AppSecret",
					data : "appsecret",
				},                 
				{                 
					title : "支付ID",
					data : "mchid",
				},                 
				{                 
					title : "支付秘钥",
					data : "mchkey",
				},
				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
						var operator=[
							<s:hasPermission name="wechatconfig/edit">
								C.createEditBtn(rowData),
							</s:hasPermission>
						];
						$(td).html(operator);
					}
				}],
		});
		
		var C = new Controller(cid,tb);
		//重写 option
		var option = {
				el:cid,
				data:{
					m:{},
					showform:false,
					errorMsg:"请录入完整信息！",
					errorShow:false
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
						this.errorMsg = "" ;
						this.errorShow = false ;
						this.m={};
						this.closeForm();
					},
					create:function(){
						this.m={};
						this.openForm();
					},
					edit:function(model){
						this.m= model;
						this.openForm();
					},
					save:function(e){
						var result = isNullCheck(this.m);
						if(result == true){
							var that = this;
							var formDom = e.target;
							C.ajaxFormEx(formDom,function(){
								that.cancel();
								tb.ajax.reload();
							});
						}else{
							
							this.errorMsg = result ;
							this.errorShow = true ;
						}
					},
				},
			};
		
		var vueObj = C.vueObj(option);
	}());
	
	//判断表单中是否有空值
	function isNullCheck(obj){
		var flag = true;
		if(isNull(obj.appid)){
			flag = "AppID 不能为空！" ;
		}else if(isNull(obj.appsecret)){
			flag = "AppSecret 不能为空！" ;
		}else if(isNull(obj.mchid)){
			flag = "支付ID 不能为空！" ;
		}else if(isNull(obj.mchkey)){
			flag = "支付秘钥 不能为空！" ;
		}
		return flag;
	}
	//验证  字符串 是否为空
	function isNull(str){
		return str.replace(/(^s*)|(s*$)/g, "").length == 0 ;
	}
</script>
