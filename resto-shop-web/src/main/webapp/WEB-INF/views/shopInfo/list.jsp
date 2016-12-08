<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>

<div id="control">
	<div class="col-md-offset-3 col-md-6">
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
								<div class="input-group">
									<input type="text" class="form-control" name="name"
										:value="m.name" placeholder="必填" required="required">
								</div>
							</div>
						</div>

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
								<div class="input-group">
									<input type="text" class="form-control"
										name="mealFeeName" v-if="!m.mealFeeName" value="餐盒费"
										required="required"> <input type="text"
										class="form-control" name="mealFeeName" v-if="m.mealFeeName"
										v-model="m.mealFeeName" required="required">
								</div>
							</div>
						</div>

						<div class="form-group" v-if="m.isMealFee==1">
							<label class="col-sm-3 control-label">餐盒费/盒：</label>
							<div class="col-sm-9">
								<div class="input-group">
									<input type="number" class="form-control"
									name="mealFeePrice" placeholder="(建议输入整数)"
									v-model="m.mealFeePrice" required="required" min="0">
								</div>
							</div>
						</div>
						
						<div class="form-group">
							<label class="col-sm-3 control-label">支付宝支付：</label>
							<div class="col-sm-9">
								<div>
									<label> <input type="radio" name="aliPay"
										v-model="m.aliPay" value="1"> 开
									</label> <label> <input type="radio" name="aliPay"
										v-model="m.aliPay" value="0"> 关
									</label>
								</div>
							</div>
						</div>
						
						<div class="form-group" v-if="m.aliPay==1">
							<label class="col-sm-3 control-label">支付折扣：</label>
							<div class="col-sm-4">
								<div class="input-group">
									<input type="number" class="form-control"
										placeholder="支付折扣" name="aliPayDiscount" min="1" max="100"
										v-model="m.aliPayDiscount" required="required">
									<div class="input-group-addon">
										<b>%</b>
									</div>
								</div>
							</div>
						</div>
					</div>
					<input class="btn green" type="submit" value="保存" /> <a
						class="btn default" @click="cancel">取消</a>
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
