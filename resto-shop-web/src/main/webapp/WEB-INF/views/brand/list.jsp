<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div>
	<!-- 添加 Brand 信息  Modal  start -->
	<div class="modal fade" id="addBrandModal" tabindex="-1" role="dialog">
	  <div class="modal-dialog" role="document">
	    <div class="modal-content">
	      <div class="modal-header text-center">
	        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
	        <h3 class="modal-title" id="BrandModalTitle"><strong>新建商家信息</strong></h3>
	      </div>
	      <div class="modal-body">
           	<form role="form" class="form-horizontal" id="brandForm">
           		<h4 class="text-center"><strong>品牌基本信息</strong></h4>
           		<div class="form-group">
           			<label class="col-sm-3 control-label">品牌名称：</label>
				    <div class="col-sm-8">
				      <input type="text" class="form-control" name="brandName">
				    </div>
				</div>
           		<div class="form-group">
           			<label class="col-sm-3 control-label">品牌标识：</label>
				    <div class="col-sm-8">
						<input type="text" class="form-control" name="brandSign">
				    </div>
				</div>
           		<div class="form-group">
				    <label class="col-sm-3 control-label">品牌管理员ID：</label>
				    <div class="col-sm-8">
				    	<input type="text" class="form-control" name="brandUserId">
				    </div>
				</div>
				<h4 class="text-center"><strong>微信配置信息</strong></h4>
           		<div class="form-group">
				    <label class="col-sm-3 control-label">App Id：</label>
				    <div class="col-sm-8">
						<input type="text" class="form-control" name="appid">
				    </div>
				</div>
				<div class="form-group">
				    <label class="col-sm-3 control-label">App Secret：</label>
				    <div class="col-sm-8">
						<input type="text" class="form-control" name="appsecret">
				    </div>
				</div>
				<div class="form-group">
				    <label class="col-sm-3 control-label">支付ID：</label>
				    <div class="col-sm-8">
						<input type="text" class="form-control" name="mchid">
				    </div>
				</div>
				<div class="form-group">
				    <label class="col-sm-3 control-label">支付秘钥：</label>
				    <div class="col-sm-8">
						<input type="text" class="form-control" name="mchkey">
				    </div>
				</div>
				<h4 class="text-center"><strong>数据库配置信息</strong></h4>
           		<div class="form-group">
				    <label class="col-sm-3 control-label">数据库名称：</label>
				    <div class="col-sm-8">
						<input type="text" class="form-control" name="name">
				    </div>
				</div>
           		<div class="form-group">
				    <label class="col-sm-3 control-label">数据库URL：</label>
				    <div class="col-sm-8">
						<input type="text" class="form-control" name="url">
				    </div>
				</div>
           		<div class="form-group">
				    <label class="col-sm-3 control-label">数据库驱动名称：</label>
				    <div class="col-sm-8">
						<input type="text" class="form-control" name="driverClassName">
				    </div>
				</div>
           		<div class="form-group">
				    <label class="col-sm-3 control-label">数据库用户名：</label>
				    <div class="col-sm-8">
						<input type="text" class="form-control" name="username">
				    </div>
				</div>
           		<div class="form-group">
				    <label class="col-sm-3 control-label">数据库密码：</label>
				    <div class="col-sm-8">
						<input type="text" class="form-control" name="password">
				    </div>
				</div>
				<div class="text-center">
					<input type="hidden" name="brandId"/>
					<input type="hidden" name="databaseConfigId"/>
					<input type="hidden" name="wechatConfigId"/>
					<input type="hidden" name="currentUser" value="$(USER_INFO.id)"/>
					<button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
<!-- 	        	<button type="submit" class="btn btn-primary" id="btn_save" onclick="saveBrand('create')">保存</button> -->
					<button type="submit" class="btn btn-primary" id=btn_save value="create">保存</button>
				</div>
           	</form>
	      </div>
	    </div>
	  </div>
	</div>
	<!-- 添加 Brand 信息  Modal  end -->
	
	<!-- 查看 配置 详细信息  Modal  start-->
	<div class="modal fade" id="configDetail" tabindex="-1" role="dialog">
	  <div class="modal-dialog" role="document">
	    <div class="modal-content">
	      <div class="modal-header">
	        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
	        <h4 class="modal-title text-center" id="configTitle"></h4>
	      </div>
	      <div class="modal-body" id="configBody">
	      </div>
	      <div class="modal-footer">
	        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
	      </div>
	    </div>
	  </div>
	</div>
	<!-- 查看 配置 详细信息  Modal  end-->
	
	<!-- 表格   start-->
	<div class="table-div">
		<div class="table-operator">
			<s:hasPermission name="brand/add">
			<button class="btn green pull-right" id="createNew" onclick="clearForm();"  data-toggle="modal" data-target="#addBrandModal">新建</button>
			</s:hasPermission>
		</div>
		<div class="clearfix"></div>
		<div class="table-filter">&nbsp;</div>
		<div class="table-body">
			<table class="table table-striped table-hover table-bordered "></table>
		</div>
	</div>
	<!-- 表格   end-->
</div>


<script>
	//全局变量  保存 表格对象
	var tb;
	$(document).ready( function () {
		//初始化 表单控件
		formData();
		//载入 表格数据
	    tb = $('.table-body>table').DataTable({
			ajax : {
				url : "brand/list_all",
				dataSrc : ""
			},
			columns : [
				{                 
					title : "品牌名称",
					data : "brandName",
				},                 
				{                 
					title : "品牌标识",
					data : "brandSign",
				},                 
				{                 
					title : "微信配置ID",
					data : "wechatConfigId",
					createdCell:function(td,tdData,rowData,row){
						$(td).html("<button class='btn' onclick='configDetail(\""+tdData+"\",\"wechatconfig\")'>点击查看详情</button>");
					}
				},                 
				{                 
					title : "数据库配置ID",
					data : "databaseConfigId",
					createdCell:function(td,tdData,rowData,row){
						$(td).html("<button class='btn' onclick='configDetail(\""+tdData+"\",\"databaseconfig\")'>点击查看详情</button>");
					}
				},                 
				{                 
					title : "品牌管理员ID",
					data : "brandUserId",
				},                 
				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
						var operator=[
							"<s:hasPermission name='brand/delete'>"+
							"<button class='btn btn-xs red' onclick='deleteBrand(\""+tdData+"\")'>删除</button>"+
							"</s:hasPermission><s:hasPermission name='brand/edit'>"+
							"<button class='btn btn-xs btn-primary' onclick='editBrand(\""+tdData+"\")'>编辑</button>"+
							"</s:hasPermission>"
						];
						$(td).html(operator);
					}
				}],
	    });
	} );
	
	//品牌信息的 Form 表单 包括 验证， 提交
	function formData(){
		$("#brandForm").validate({ 
            focusInvalid: true, //当为false时，验证无效时，没有焦点响应  
            onkeyup: false,		//键盘监听   
            rules:{
            	brandName	:	{ required : true },
                brandSign	:	{ required : true },
                brandUserId	:	{ required : true },
                appid		:	{ required : true },
                appsecret	:	{ required : true },
                mchid		:	{ required : true },
                mchkey		:	{ required : true },
                name		:	{ required : true },
                url			:	{ required : true },
                driverClassName	:	{ required : true },
                username	:	{ required : true },
                password	:	{ required : true },
            },
            messages:{
            	brandName	:	{ required:"<small>此项不能为空</small>" },
                brandSign	:	{ required:"<small>此项不能为空</small>" },
                brandUserId	:	{ required:"<small>此项不能为空</small>" },
                appid		:	{ required:"<small>此项不能为空</small>" },
                appsecret	:	{ required:"<small>此项不能为空</small>" },
                mchid		:	{ required:"<small>此项不能为空</small>" },
                mchkey		:	{ required:"<small>此项不能为空</small>" },
                name		:	{ required:"<small>此项不能为空</small>" },
                url			:	{ required:"<small>此项不能为空</small>" },
                driverClassName	:	{ required:"<small>此项不能为空</small>" },
                username	:	{ required:"<small>此项不能为空</small>" }, 
                password	:	{ required:"<small>此项不能为空</small>" }   
            },
            highlight : function(element) {	// 验证未通执行的方法
                $(element).closest('.form-group').addClass('has-error');  
            },  
            success : function(label) {  //验证通过之后执行的方法
                label.closest('.form-group').removeClass('has-error');  
                label.remove();
            },  
            errorPlacement : function(error, element) {	//错误信息追加的位置
                element.parent('div').append(error);
            },
            submitHandler: function(form){   //表单提交句柄,为一回调函数，带一个参数：form 
                var url = "brand/"+$("#btn_save").val();
				var data = $(form).serialize();
				ajaxTemplate(url,data,null);
		    	$('#addBrandModal').modal('hide');//隐藏Modal
            }
		});
	}
	
	//删除事件
	function deleteBrand(brandId){
		var cDialog = new dialog({
			title:"提示",
			content:"确定要删除么",
			width:200,
			ok:function(){
				var url = "brand/delete";
				var data = "id="+brandId;
				ajaxTemplate(url,data,null);
			},
			cancel:function(){}
		});
		cDialog.showModal();
	}
	
	//编辑 Brand 信息
	function editBrand(brandId){
		//自定义方法
		var operation = function(brandData){
			var WechatConfig = brandData.wechatConfig;
			var DatabaseConfig = brandData.databaseConfig;
			//品牌 信息
			$("#brandForm input[name='brandId']").val(brandData.id);
			$("#brandForm input[name='brandName']").val(brandData.brandName);
			$("#brandForm input[name='brandSign']").val(brandData.brandSign);
			$("#brandForm input[name='brandUserId']").val(brandData.brandUserId);
			//微信配置信息
			$("#brandForm input[name='wechatConfigId']").val(WechatConfig.id);
			$("#brandForm input[name='appid']").val(WechatConfig.appid);
			$("#brandForm input[name='appsecret']").val(WechatConfig.appsecret);
			$("#brandForm input[name='mchid']").val(WechatConfig.mchid);
			$("#brandForm input[name='mchkey']").val(WechatConfig.mchkey);
			//数据库配置信息
			$("#brandForm input[name='databaseConfigId']").val(DatabaseConfig.id);
			$("#brandForm input[name='name']").val(DatabaseConfig.name);
			$("#brandForm input[name='url']").val(DatabaseConfig.url);
			$("#brandForm input[name='driverClassName']").val(DatabaseConfig.driverClassName);
			$("#brandForm input[name='username']").val(DatabaseConfig.username);
			$("#brandForm input[name='password']").val(DatabaseConfig.password);
		}
		
		clearForm();//清空 Form 表单的文本
		$("#BrandModalTitle").html("<strong>修改品牌信息</strong>");	//修改 Modal 标题
		$("#btn_save").attr("value","modify");	//修改 Modal 的保存按钮参数
		var url = "brand/list_one";
		var data = "id="+brandId;
		ajaxTemplate(url,data,operation);
		$("#addBrandModal").modal("show");
	}
	
	//查看 配置 详情
	function configDetail(id,type){
		//自定义 方法
		var operation = function(configData){
			//判断是否有返回的对象
 			if(configData != ""){
 				var configBody ; //写入 Modal 的值
 				var configTitle; //Modal 的标题
 				if(type == "wechatconfig"){
 					configBody = [
							"<dl class='dl-horizontal'>"+
							"<dt>微信ID：</dt><dd>"+configData.id+"</dd><br/>"+
							"<dt>AppID：</dt><dd>"+configData.appid+"</dd><br/>"+
							"<dt>AppSecret：</dt><dd>"+configData.appsecret+"</dd><br/>"+
							"<dt>支付ID：</dt><dd>"+configData.mchid+"</dd><br/>"+
							"<dt>支付秘钥：</dt><dd>"+configData.mchkey+"</dd><br/>"+
							"</dl>"
							];
					configTitle = "<strong>微信配置</strong>";
 				}else{
 					configBody = [
							"<dl class='dl-horizontal'>"+
							"<dt>数据库ID：</dt><dd>"+configData.id+"</dd><br/>"+
							"<dt>数据库名称：</dt><dd>"+configData.name+"</dd><br/>"+
							"<dt>数据库URL：</dt><dd>"+configData.url+"</dd><br/>"+
							"<dt>数据库驱动名称：</dt><dd>"+configData.driverClassName+"</dd><br/>"+
							"<dt>数据库用户名：</dt><dd>"+configData.username+"</dd><br/>"+
							"<dt>数据库密码：</dt><dd>"+configData.password+"</dd><br/>"+
							"<dt>创建时间：</dt><dd>"+formatTime(configData.createTime)+"</dd><br/>"+
							"<dt>上次修改时间：</dt><dd>"+formatTime(configData.updateTime)+"</dd><br/>"+
							"</dl>"
							];
 					configTitle = "<strong>数据库配置</strong>";
 				}
 				$("#configTitle").html(configTitle);
 				$("#configBody").html(configBody);
 				$("#configDetail").modal('show');
 			}
		}
		var url = type+"/list_one";
		var data = "id="+id;
		ajaxTemplate(url,data,operation);
	}
	
	// ajax 模板
	function ajaxTemplate(url,data,operation){
		$.ajax({
 		   type: "POST",
 		   url: url,
 		   data: data,
 		   success: function(result){
 			toastr.clear();//清空消息提示
 			//判断是否有自定义函数
 			if(typeof operation== "function"){
 				operation(result.data);
 			}else{
				 if(result.success){
	    			 toastr.success("操作成功");
				 }else{
					 toastr.error(result.message);
				 }
				tb.ajax.reload();//重新加载表格
 			}
 		   }
	   });
	}
	
	//清空上次表单的内容
	function clearForm(){
		//还原 品牌信息的基本信息
		$("#BrandModalTitle").html("<strong>新建品牌信息</strong>");//修改 Modal 标题
		$("#btn_save").attr("value","create");
		//品牌信息
		$("#brandForm input[name='brandId']").val("");
		$("#brandForm input[name='brandName']").val("");
		$("#brandForm input[name='brandSign']").val("");
		$("#brandForm input[name='brandUserId']").val("");
		//微信配置信息
		$("#brandForm input[name='wechatConfigId']").val("");
		$("#brandForm input[name='appid']").val("");
		$("#brandForm input[name='appsecret']").val("");
		$("#brandForm input[name='mchid']").val("");
		$("#brandForm input[name='mchkey']").val("");
		//数据库配置信息
		$("#brandForm input[name='databaseConfigId']").val("");
		$("#brandForm input[name='name']").val("");
		$("#brandForm input[name='url']").val("");
		$("#brandForm input[name='driverClassName']").val("");
		$("#brandForm input[name='username']").val("");
		$("#brandForm input[name='password']").val("");
	}
	
	//格式化时间
	function formatTime(temp){
		if(temp == null || temp ==""){
			return "暂无记录";
		}
		temp = new Date(temp);
		var hours = temp.getHours() < 10 ? "0" + temp.getHours() : temp.getHours();
		var minutes = temp.getMinutes() < 10 ? "0" + temp.getMinutes() : temp.getMinutes();
		var seconds = temp.getSeconds() < 10 ? "0" + temp.getSeconds() : temp.getSeconds();
		temp = temp.getFullYear()+"/"+(temp.getMonth()+1)+"/"+temp.getDate()+" "+hours+":"+minutes+":"+seconds;
		return temp ;
	}
	
</script>
