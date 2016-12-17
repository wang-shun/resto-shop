<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>

<div id="control" class="row">
	<div class="col-md-offset-3 col-md-6" v-show="b.aliPay == 1">
		<div class="portlet light bordered">
			<div class="portlet-title">
				<div class="caption">
					<span class="caption-subject bold font-blue-hoki"> 表单</span>
				</div>
			</div>
			<div class="portlet-body">
				<form role="form" class="form-horizontal" @submit.prevent="save">
					<div class="form-body">
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
							<div class="col-sm-3">
								<div class="input-group">
									<input type="number" class="form-control" placeholder="请输入数字"
										placeholder="支付折扣" name="aliPayDiscount" min="1" max="100"
										v-model="m.aliPayDiscount">
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
	<div v-show="b.aliPay == 0">
		<h1 align="center">尚未开启此功能，请联系管理员开通此功能!</h1>
	</div>
</div>

<script>
	$(document).ready(function() {
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
				b : {}
			},
			created: function () {
				initContent();
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
						url : "aliPay/modify",
						data : $(formDom).serialize(),
						success : function(result) {
							if (result.success) {
								toastr.clear();
								toastr.success("保存成功!");
							} else {
								toastr.clear();
								toastr.error("保存失败!");
							}
						},
						error : function() {
							toastr.clear();
							toastr.error("保存失败!");
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
				url : "aliPay/list_one",
				success : function(result) {
					console.log(result.data);
					vueObj.m = result.data[0];
					vueObj.b = result.data[1];
				}
			})
		}

	}());
</script>
