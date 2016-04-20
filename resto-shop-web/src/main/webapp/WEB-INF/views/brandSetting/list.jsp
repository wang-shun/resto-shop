<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>

<div id="control">
	<div class="row form-div">
		<div class="col-md-offset-3 col-md-6">
			<div class="portlet light bordered">
				<div class="portlet-title">
					<div class="caption">
						<span class="caption-subject bold font-blue-hoki"> 表单</span>
					</div>
				</div>
				<div class="portlet-body">
					<form role="form" action="{{m.id?'brandsetting/modify':'brandsetting/create'}}" @submit.prevent="save">
			<div class="form-body">
				<div class="form-group">
				    <label>短信签名</label>
				    <input type="text" class="form-control" name="smsSign" v-model="m.smsSign">
				</div>
				<div class="form-group">
				    <label>评论最小金额</label>
				    <input type="text" class="form-control" name="appraiseMinMoney" v-model="m.appraiseMinMoney">
				</div>
				<div class="form-group">
				    <label>新用户注册提醒标题</label>
				    <input type="text" class="form-control" name="customerRegisterTitle" v-model="m.customerRegisterTitle">
				</div>
				<div class="form-group">
				    <label>微信欢迎图片</label>
				    <input type="text" class="form-control" name="wechatWelcomeImg" v-model="m.wechatWelcomeImg">
				</div>
				<div class="form-group">
				    <label>微信欢迎标题</label>
				    <input type="text" class="form-control" name="wechatWelcomeTitle" v-model="m.wechatWelcomeTitle">
				</div>
				<div class="form-group">
				    <label>微信欢迎地址</label>
				    <input type="text" class="form-control" name="wechatWelcomeUrl" v-model="m.wechatWelcomeUrl">
				</div>
				<div class="form-group">
				    <label>微信欢迎文本</label>
				    <input type="text" class="form-control" name="wechatWelcomeContent" v-model="m.wechatWelcomeContent">
				</div>
				<div class="form-group">
				    <label>微信首页名称</label>
				    <input type="text" class="form-control" name="wechatHomeName" v-model="m.wechatHomeName">
				</div>
				<div class="form-group">
				    <label>微信堂食名称</label>
				    <input type="text" class="form-control" name="wechatTangshiName" v-model="m.wechatTangshiName">
				</div>
				<div class="form-group">
				    <label>微信我的名称</label>
				    <input type="text" class="form-control" name="wechatMyName" v-model="m.wechatMyName">
				</div>
				<div class="form-group">
				    <label>微信外卖名称</label>
				    <input type="text" class="form-control" name="wechatWaimaiName" v-model="m.wechatWaimaiName">
				</div>
				<div class="form-group">
				    <label>微信自定义样式</label>
<!-- 				    <input type="text" class="form-control" name="wechatCustomoStyle" v-model="m.wechatCustomoStyle"> -->
				    <textarea class="form-control" name="wechatCustomoStyle" v-model="m.wechatCustomoStyle"></textarea>
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
</div>

<script>
	
	$(document).ready(function(){
		
		initcontent();
		
		toastr.options = {
				  "closeButton": true,
				  "debug": false,
				  "positionClass": "toast-top-right",
				  "onclick": null,
				  "showDuration": "500",
				  "hideDuration": "500",
				  "timeOut": "3000",
				  "extendedTimeOut": "500",
				  "showEasing": "swing",
				  "hideEasing": "linear",
				  "showMethod": "fadeIn",
				  "hideMethod": "fadeOut"
				}
		var temp;
		var vueObj = new Vue({
			el:"#control",
			data:{
				m:{},
			},
			methods:{
				save:function(e){
					var formDom = e.target;
					$.ajax({
						url:"brandSetting/modify",
						data:$(formDom).serialize(),
						success:function(result){
							if(result.success){
								toastr.clear();
								toastr.success("保存成功！");
							}else{
								toastr.clear();
								toastr.error("保存失败");
							}
						},
						error:function(){
							toastr.clear();
							toastr.error("保存失败");
						}
					})
					
				},
				cancel:function(){
					initcontent();
				}
			}
		});
		
		function initcontent(){
			$.ajax({
				url:"brandSetting/list_one",
				success:function(result){
					console.log(result.data);
	 				vueObj.m=result.data;
				}
			})
		}
		
	}());
	
</script>
