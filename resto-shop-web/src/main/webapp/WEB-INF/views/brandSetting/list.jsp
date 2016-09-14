<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>

<div id="control" class="row">
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
				<!--<div class="form-group">
				    <label>短信签名</label>
				    <input type="text" class="form-control" name="smsSign" v-model="m.smsSign">
				</div>-->
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
				    <input type="hidden" name="wechatWelcomeImg" v-model="m.wechatWelcomeImg">
					<img-file-upload  class="form-control" @success="uploadSuccess" @error="uploadError"></img-file-upload>
				</div>
				<div class="form-group">
				    <label>红包Logo</label>
				    <input type="hidden" name="redPackageLogo" v-model="m.redPackageLogo">
					<img-file-upload  class="form-control" @success="setRedPackage" @error="uploadError"></img-file-upload>
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
				<%--<div class="form-group">--%>
				    <%--<label>微信自定义样式</label>--%>
				    <%--<textarea class="form-control" name="wechatCustomoStyle" v-model="m.wechatCustomoStyle"></textarea>--%>
				<%--</div>--%>
				
				<div class="form-group">
				    <label>红包提醒倒计时(秒)</label>
				    <input type="number" class="form-control" name="autoConfirmTime" v-model="m.autoConfirmTime" required="required"> 
				</div>
				<div class="form-group">
				    <label>最迟加菜时间(秒)</label>
				    <input type="number" class="form-control" name="closeContinueTime" v-model="m.closeContinueTime" required="required">
				</div>
				
				<div class="form-group">
					<div class="control-label">是否选择配送模式</div>
					<label >
						<input type="radio" name="isChoiceMode" v-model="m.isChoiceMode" value="1">
						是
					</label>
					<label>
						<input type="radio" name="isChoiceMode" v-model="m.isChoiceMode" value="0"> 
						否
					</label>
				</div>
				
				<div class="form-group">
					<div class="control-label">是否红包弹窗</div>
					<label >
						<input type="radio" name="autoAlertAppraise" v-model="m.autoAlertAppraise" value="1">
						是
					</label>
					<label>
						<input type="radio" name="autoAlertAppraise" v-model="m.autoAlertAppraise" value="0"> 
						否
					</label>
				</div>
				
				<div class="form-group">
				    <label>好评最少字数</label>
				    <input type="number" class="form-control" name="goodAppraiseLength" v-model="m.goodAppraiseLength">
				</div>
				
				<div class="form-group">
				    <label>差评最少字数</label>
				    <input type="number" class="form-control" name="badAppraiseLength" v-model="m.badAppraiseLength">
				</div>
				<div class="form-group">
					<div class="control-label">是否打印总单</div>
					<label >
						<input type="radio" name="autoPrintTotal" v-model="m.autoPrintTotal" value="0">
						是
					</label>
					<label>
						<input type="radio" name="autoPrintTotal" v-model="m.autoPrintTotal" value="1">
						否
					</label>
				</div>
				<div class="form-group">
					<div class="control-label">是否启用推荐餐包</div>
					<label >
						<input type="radio" name="isUseRecommend" v-model="m.isUseRecommend" value="1">
						是
					</label>
					<label>
						<input type="radio" name="isUseRecommend" v-model="m.isUseRecommend" value="0">
						否
					</label>
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
				},
				uploadSuccess:function(url){
					$("[name='wechatWelcomeImg']").val(url).trigger("change");
					toastr.success("上传成功！");
				},
				setRedPackage:function(url){
					$("[name='redPackageLogo']").val(url).trigger("change");
					toastr.success("上传logo成功！");
				},
				uploadError:function(msg){
					toastr.error("上传失败");
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
