<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>

<div id="control" class="row">
	<div class="col-md-offset-3 col-md-6" >
		<div class="portlet light bordered">
			<div class="portlet-title">
				<div class="caption">
					<span class="caption-subject bold font-blue-hoki"> 表单</span>
				</div>
			</div>
			<div class="portlet-body">
				<form role="form" class="form-horizontal" 
					action="{{'shopDetailManage/modify'}}" @submit.prevent="save">
					<div class="form-body">
						<div class="form-group">
							<label class="col-sm-3 control-label">店铺名称：</label>
							<div class="col-sm-9">
								<input type="text" class="form-control" name="name"
										:value="m.name" placeholder="必填" required="required">
							</div>
						</div>
						
						<%--<div class="form-group">--%>
							<%--<label class="col-sm-3 control-label">店铺图片：</label>--%>
							<%--<div class="col-sm-5">--%>
								<%--<div class="input-group">--%>
									<%--<input type="hidden" name="photo" v-model="m.photo">--%>
									<%--<img-file-upload  class="form-control" @success="uploadSuccess" @error="uploadError"></img-file-upload>--%>
								<%--</div>--%>
							<%--</div>--%>
						<%--</div>--%>

						<%--<div class="form-group">--%>
							<%--<label class="col-sm-3 control-label">评论最小金额：</label>--%>
							<%--<div class="col-sm-9">--%>
								<%--<div class="input-group">--%>
									<%--<input type="text" class="form-control" name="appraiseMinMoney"--%>
                                       <%--v-model="m.appraiseMinMoney">--%>
								<%--</div>--%>
							<%--</div>--%>
						<%--</div>--%>
						<%----%>
						<%--<div class="form-group">--%>
							<%--<label class="col-sm-3 control-label">红包提醒倒计时(秒)：</label>--%>
							<%--<div class="col-sm-9">--%>
								<%--<div class="input-group">--%>
									<%--<input type="number" class="form-control" name="autoConfirmTime" --%>
									<%--v-model="m.autoConfirmTime" required="required" placeholder="(建议输入整数)"--%>
									<%--min="0">--%>
								<%--</div>--%>
							<%--</div>--%>
						<%--</div>--%>
						<%----%>
						<%--<div class="form-group">--%>
							<%--<label class="col-sm-3 control-label">最迟加菜时间(秒)：</label>--%>
							<%--<div class="col-sm-9">--%>
								<%--<div class="input-group">--%>
									<%--<input type="number" class="form-control" name="closeContinueTime" --%>
									<%--v-model="m.closeContinueTime" required="required" placeholder="(建议输入整数)"--%>
									<%--min="0">--%>
								<%--</div>--%>
							<%--</div>--%>
						<%--</div>--%>
						<%----%>
						<%--<div class="form-group">--%>
							<%--<label class="col-sm-3 control-label">选择配送模式：</label>--%>
							<%--<div class="col-sm-9">--%>
								<%--<div>--%>
									<%--<label >--%>
										<%--<input type="radio" name="isChoiceMode" v-model="m.isChoiceMode" value="1">--%>
										<%--是--%>
									<%--</label>--%>
									<%--<label>--%>
										<%--<input type="radio" name="isChoiceMode" v-model="m.isChoiceMode" value="0"> --%>
										<%--否--%>
									<%--</label>--%>
								<%--</div>--%>
							<%--</div>--%>
						<%--</div>--%>
						<%----%>
						<%--<div class="form-group">--%>
							<%--<label class="col-sm-3 control-label">红包弹窗：</label>--%>
							<%--<div class="col-sm-9">--%>
								<%--<div>--%>
									<%--<label >--%>
										<%--<input type="radio" name="autoAlertAppraise" v-model="m.autoAlertAppraise" value="1">--%>
										<%--是--%>
									<%--</label>--%>
									<%--<label>--%>
										<%--<input type="radio" name="autoAlertAppraise" v-model="m.autoAlertAppraise" value="0"> --%>
										<%--否--%>
									<%--</label>--%>
								<%--</div>--%>
							<%--</div>--%>
						<%--</div>--%>

						<%--<div class="form-group">--%>
							<%--<label class="col-sm-3 control-label">好评最少字数：</label>--%>
							<%--<div class="col-sm-9">--%>
								<%--<div class="input-group">--%>
									<%--<input type="number" class="form-control" name="goodAppraiseLength" --%>
									<%--v-model="m.goodAppraiseLength" placeholder="(建议输入整数)" min= "0">--%>
								<%--</div>--%>
							<%--</div>--%>
						<%--</div>--%>
						<%----%>
						<%--<div class="form-group">--%>
							<%--<label class="col-sm-3 control-label">差评最少字数：</label>--%>
							<%--<div class="col-sm-9">--%>
								<%--<div class="input-group">--%>
									<%--<input type="number" class="form-control" name="badAppraiseLength" --%>
									<%--v-model="m.badAppraiseLength" placeholder="(建议输入整数)" min = "0">--%>
								<%--</div>--%>
							<%--</div>--%>
						<%--</div>--%>

						<%--<div class="form-group">--%>
							<%--<label class="col-sm-3 control-label">打印总单：</label>--%>
							<%--<div class="col-sm-9">--%>
								<%--<div>--%>
									<%--<label >--%>
										<%--<input type="radio" name="autoPrintTotal" v-model="m.autoPrintTotal" value="0">--%>
										<%--是--%>
									<%--</label>--%>
									<%--<label>--%>
										<%--<input type="radio" name="autoPrintTotal" v-model="m.autoPrintTotal" value="1">--%>
										<%--否--%>
									<%--</label>--%>
								<%--</div>--%>
							<%--</div>--%>
						<%--</div>--%>
						<%----%>
						<%--<div class="form-group">--%>
							<%--<label class="col-sm-3 control-label">启用推荐餐包：</label>--%>
							<%--<div class="col-sm-9">--%>
								<%--<div>--%>
									<%--<label >--%>
										<%--<input type="radio" name="isUseRecommend" v-model="m.isUseRecommend" value="1">--%>
										<%--是--%>
									<%--</label>--%>
									<%--<label>--%>
										<%--<input type="radio" name="isUseRecommend" v-model="m.isUseRecommend" value="0">--%>
										<%--否--%>
									<%--</label>--%>
								<%--</div>--%>
							<%--</div>--%>
						<%--</div>--%>
						<%----%>
						<%--<div class="form-group">--%>
							<%--<label class="col-sm-3 control-label">套餐出单方式：</label>--%>
							<%--<div class="col-sm-9">--%>
								<%--<div>--%>
									<%--<label >--%>
										<%--<input type="radio" name="printType" v-model="m.printType" value="0">--%>
										<%--整单出单--%>
									<%--</label>--%>
									<%--<label>--%>
										<%--<input type="radio" name="printType" v-model="m.printType" value="1">--%>
										<%--分单出单--%>
									<%--</label>--%>
								<%--</div>--%>
							<%--</div>--%>
						<%--</div>--%>

						<%--<div class="form-group">--%>
							<%--<label class="col-sm-3 control-label">买单后出总单（后付款模式）：</label>--%>
							<%--<div class="col-sm-9">--%>
								<%--<div>--%>
									<%--<label>--%>
										<%--<input type="radio" name="isPrintPayAfter" v-model="m.isPrintPayAfter" value="1">--%>
										<%--开--%>
									<%--</label>--%>
									<%--<label>--%>
										<%--<input type="radio" name="isPrintPayAfter"--%>
											   <%--v-model="m.isPrintPayAfter" value="0">--%>
										<%--关--%>
									<%--</label>--%>
								<%--</div>--%>
							<%--</div>--%>
						<%--</div>--%>

						<%--<div class="form-group">--%>
							<%--<label class="col-sm-3 control-label">启用服务费：</label>--%>
							<%--<div class="col-sm-9">--%>
								<%--<div>--%>
									<%--<label>--%>
										<%--<input type="radio" name="isUseServicePrice" --%>
										<%--v-model="m.isUseServicePrice" value="1">--%>
										<%--是--%>
									<%--</label>--%>
									<%--<label >--%>
										<%--<input type="radio" name="isUseServicePrice"  --%>
										<%--v-model="m.isUseServicePrice" value="0"">--%>
										<%--否--%>
									<%--</label>--%>
								<%--</div>--%>
							<%--</div>--%>
						<%--</div>--%>
						<%----%>
						<%--<div class="form-group" v-if="m.isUseServicePrice==1">--%>
							<%--<label class="col-sm-3 control-label">名称：</label>--%>
							<%--<div class="col-sm-9">--%>
								<%--<div class="input-group">--%>
									<%--<input type="text" class="form-control"--%>
										<%--name="serviceName" v-if="!m.serviceName" value="服务费" required="required"> --%>
								   <%--<input type="text"--%>
										<%--class="form-control" name="serviceName" v-if="m.serviceName" v-model="m.serviceName" --%>
										<%--required="required">--%>
								<%--</div>--%>
							<%--</div>--%>
						<%--</div>--%>

						<%--<div class="form-group" v-if="m.isUseServicePrice==1">--%>
							<%--<label class="col-sm-3 control-label">服务费/每人：</label>--%>
							<%--<div class="col-sm-9">--%>
								<%--<div class="input-group">--%>
									<%--<input type="number" placeholder="(建议输入整数)" class="form-control" name="servicePrice" --%>
									<%--v-model="m.servicePrice" required="required" min="0">--%>
								<%--</div>--%>
							<%--</div>--%>
						<%--</div>--%>

						<div class="form-group">
							<label class="col-sm-3 control-label">启用餐盒费：</label>
							<div class="col-sm-9">
								<div>
									<label> <input type="radio" name="isMealFee"
										v-model="m.isMealFee" value="1"> 是
									</label> <label> <input type="radio" name="isMealFee"
										v-model="m.isMealFee" value="0"> 否
									</label>
								</div>
							</div>
						</div>
						
						<div class="form-group" v-if="m.isMealFee==1">
							<label class="col-sm-3 control-label">名称：</label>
							<div class="col-sm-9">
								<input type="text" class="form-control"
									name="mealFeeName" v-if="!m.mealFeeName" value="餐盒费"
									required="required"> <input type="text"
									class="form-control" name="mealFeeName" v-if="m.mealFeeName"
									v-model="m.mealFeeName" required="required">
							</div>
						</div>

						<div class="form-group" v-if="m.isMealFee==1">
							<label class="col-sm-3 control-label">餐盒费/盒：</label>
							<div class="col-sm-9">
								<input type="number" class="form-control"
									name="mealFeePrice" placeholder="(建议输入整数)"
									v-model="m.mealFeePrice" required="required" min="0">
							</div>
						</div>
						
						<div class="form-group">
							<label class="col-sm-3 control-label">开启就餐提醒：</label>
							<div class="col-sm-9">
								<div>
									<label> <input type="radio" name="isPush"
										v-model="m.isPush" value="1"> 是
									</label> <label> <input type="radio" name="isPush"
										v-model="m.isPush" value="0"> 否
									</label>
								</div>
							</div>
						</div>
						
						<div class="form-group" v-if="m.isPush==1">
							<label class="col-sm-3 control-label">消息内容：</label>
							<div class="col-sm-9">
								<input type="text" class="form-control"
									name="pushContext" v-if="!m.pushContext" placeholder="消息文案"
									required="required"> <input type="text"
									class="form-control" name="pushContext" v-if="m.pushContext"
									v-model="m.pushContext" required="required">
							</div>
						</div>

						<div class="form-group" v-if="m.isPush==1">
							<label class="col-sm-3 control-label">推送时间：</label>
							<div class="col-sm-9">
								<input type="number" class="form-control"
									name="pushTime" placeholder="(建议输入整数,以秒为单位)"
									v-model="m.pushTime" required="required" min="0">
							</div>
						</div>

						<div class="form-group">
							<label class="col-sm-3 control-label">是否开启二维码加密：</label>
							<div class="col-sm-9">
								<div>
									<label> <input type="radio" name="isNewQrcode"
												   v-model="m.isNewQrcode" value="1"> 是
									</label> <label> <input type="radio" name="isNewQrcode"
															v-model="m.isNewQrcode" value="0"> 否
								</label>
								</div>
							</div>
						</div>
						
						<%--<div class="form-group">--%>
							<%--<label class="col-sm-3 control-label">支付宝支付：</label>--%>
							<%--<div class="col-sm-9">--%>
								<%--<div>--%>
									<%--<label> <input type="radio" name="aliPay"--%>
										<%--v-model="m.aliPay" value="1"> 开--%>
									<%--</label> <label> <input type="radio" name="aliPay"--%>
										<%--v-model="m.aliPay" value="0"> 关--%>
									<%--</label>--%>
								<%--</div>--%>
							<%--</div>--%>
						<%--</div>--%>
						<%----%>
						<%--<div class="form-group" v-if="m.aliPay==1">--%>
							<%--<label class="col-sm-3 control-label">支付折扣：</label>--%>
							<%--<div class="col-sm-4">--%>
								<%--<div class="input-group">--%>
									<%--<input type="number" class="form-control" placeholder="(建议输入整数)"--%>
										<%--placeholder="支付折扣" name="aliPayDiscount" min="1" max="100"--%>
										<%--v-model="m.aliPayDiscount">--%>
									<%--<div class="input-group-addon">--%>
										<%--<b>%</b>--%>
									<%--</div>--%>
								<%--</div>--%>
							<%--</div>--%>
						<%--</div>--%>
						<div class="form-group">
							<label class="col-sm-3 control-label">店铺标语：</label>
							<div class="col-sm-9">
								<input type="text" class="form-control"
									name="slogan" placeholder="请输入店铺标语,不填则取品牌设置的内容"
									v-model="m.slogan">
							</div>
						</div>
						<div class="form-group">
							<label class="col-sm-3 control-label">等位提示：</label>
							<div class="col-sm-9">
								<textarea rows="3" class="form-control"
									name="queueNotice" placeholder="请输入等位提示,不填则取品牌设置的内容"
									v-model="m.queueNotice"></textarea>
							</div>
						</div>
					</div>
					<div class="text-center">
						<input class="btn green" type="submit" value="保存" />&nbsp;&nbsp;&nbsp;
						<a class="btn default" @click="cancel">取消</a>
					</div>
				</form>
			</div>
		</div>
	</div>
</div>

<script>
	$(document).ready(function() {
		initContent();
		toastr.options = {
			"closeButton" : true,
			"debug" : false,
			"positionClass" : "toast-top-right",
			"onclick" : null,
			"showDuration" : "500",
			"hideDuration" : "500",
			"timeOut" : "3000",
			"extendedTimeOut" : "500",
			"showEasing" : "swing",
			"hideEasing" : "linear",
			"showMethod" : "fadeIn",
			"hideMethod" : "fadeOut"
		}
		var temp;
		var vueObj = new Vue({
			el : "#control",
			data : {
				m : {},
			},
			methods : {
				initTime : function() {
					$(".timepicker-no-seconds").timepicker({
						autoclose : true,
						showMeridian : false,
						minuteStep : 5
					});
				},
				save : function(e) {
					var formDom = e.target;
					$.ajax({
						url : "shopInfo/modify",
						data : $(formDom).serialize(),
						success : function(result) {
							if (result.success) {
								toastr.clear();
								toastr.success("保存成功！");
							} else {
								toastr.clear();
								toastr.error("保存失败");
							}
						},
						error : function() {
							toastr.clear();
							toastr.error("保存失败");
						}
					})

				},
				cancel : function() {
					initContent();

				},
				uploadSuccess:function(url){
					$("[name='photo']").val(url).trigger("change");
					toastr.success("上传成功！");
				},
				uploadError:function(msg){
					toastr.error("上传失败");
				}
			}
		});

		function initContent() {
			$.ajax({
				url : "shopInfo/list_one",
				success : function(result) {
					console.log(result.data);
					vueObj.m = result.data;
				}
			})
		}

	}());
</script>
