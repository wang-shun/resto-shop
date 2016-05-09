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
	            	<form role="form" action="{{'branduser/create'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
						    <label>用户名</label>
						    <input type="text" class="form-control" name="username" v-model="m.username" @blur="checkname(m.username)" required="required">
						</div>
						<div class="form-group">
						    <label>密码</label>
						    <input type="password" class="form-control" name="password" v-model="m.password" required="required">
						</div>
						
						<div class="form-group">
							<div label for="shopDetailId" class="control-label">选择店铺</div>
							<div>
								<select class="form-control" name="shopDetailId" :value="shop.id">
									<option v-for="shop in  shops" :value="shop.id">
										{{shop.name}}
									</option>
								</select>
							</div>
						</div>
						
						<div class="form-group">
							<div label for="roleId" class="control-label">选择角色</div>
							<div>
								<select class="form-control" name="roleId" :value="role.id">
									<option v-for="role in  roles" :value="role.id">
										{{role.name}}
									</option>
								</select>
							</div>
						</div>
						</div>
						<input type="hidden" name="id" v-model="m.id" />
						<input class="btn green"  type="submit"  value="保存" id="saveBrandUser" >
						<a class="btn default" @click="cancel" >取消</a>
					</form>
	            </div>
	        </div>
		</div>
	</div>
	
	<div class="table-div">
		<div class="table-operator">
			<s:hasPermission name="branduser/add">
			<button class="btn green pull-right" @click="create">添加用户</button>
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
				url : "branduser/list_all",
				dataSrc : ""
			},
			columns : [
							{                 
				title : "用户名",
				data : "username",
			},                 
			{                 
				title : "店铺名称",
				data : "shopName",
			}, 
			{                 
				title : "角色名字",
				data : "roleName",
			}, 
			
			],
		});
		
	 var C = new Controller(null,tb);
		 var vueObj = new Vue({
			el:"#control",
			data:{
				shops:{},
				roles:{},
			},
			mixins:[C.formVueMix],
			methods:{
				 create:function(){
					this.m={};
					this.openForm();
					Vue.nextTick(function(){
						vueObj.initShopName();
						vueObj.initRoleName();
					}) 
				}, 
				initShopName:function(){
					$.ajax({
						url:"shopDetail/list_all",
						success:function(result){
							if(result.success==true){
								var data = result.data;
								var temps=[];
								for(var i=0;i<data.length;i++){
									temps[i]={"id":data[i].id,"name":data[i].name};
								}
 								vueObj.$set("shops",temps);
							}
						}
					});
				},
				initRoleName:function(){
					$.ajax({
						url:"usergroup/list_all",
						success:function(result){
							if(result.success==true){
								var data = result.data;
								var temps = [];
								for(var i=0;i<data.length;i++){
									temps[i]={"id":data[i].id,"name":data[i].roleName}
								}
								vueObj.$set("roles",temps);
							}
						}
						
					});
				},
				
				checkname:function(username){
					Vue.nextTick(function(){
						$.ajax({
							url:"branduser/checkusername",
							data:{"userName":username},
							success:function(result){
								if(result.data!=null){
									toastr.clear();
									toastr.error("用户名已经存在请重新输入");
									//disabled="disabled"
									$("#saveBrandUser").attr("disabled","disabled");
								}else{
									$("#saveBrandUser").removeAttr("disabled");
									
								}
								
							}
						})
					}) 
				}
			}
			
		});
		
		 C.vue=vueObj; 
	}());
	
</script>