<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>
<style>
	.formBox{
        color: #5bc0de;
	}
</style>
<div id="control" class="row">
	<div class="col-md-12" >
		<div class="portlet light bordered">
			<div class="portlet-title text-center">
				<div class="caption" style="float: none;">
					<span class="caption-subject bold font-blue-hoki"><strong>店铺参数设置</strong></span>
				</div>
			</div>
			<div class="portlet-body">
				<form role="form" class="form-horizontal" action="{{'shopDetailManage/modify'}}" @submit.prevent="save">
					<div class="form-body">
						<div class="form-group">
							<label class="col-md-4 control-label">店铺名称：</label>
							<div  class="col-md-6">
								<input type="text" class="form-control" name="name" :value="m.name" placeholder="必填" required="required">
							</div>
						</div>

						<div class="form-group">
							<label class="col-md-4 control-label">打印总单：</label>
							<div class="col-sm-6 radio-list">
								<label class="radio-inline">
									<input type="radio" name="autoPrintTotal" v-model="m.autoPrintTotal" value="0"> 是
								</label>
								<label class="radio-inline">
									<input type="radio" name="autoPrintTotal" v-model="m.autoPrintTotal" value="1"> 否
								</label>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-4 control-label">套餐出单方式：</label>
							<div  class="col-md-6 radio-list">
								<label class="radio-inline">
									<input type="radio" name="printType" v-model="m.printType" value="0"> 整单出单
								</label>
								<label class="radio-inline">
									<input type="radio" name="printType" v-model="m.printType" value="1"> 分单出单
								</label>
							</div>
						</div>
                        <!--    仅   后付款模式   显示-->
						<div class="form-group" v-show="m.shopMode == 5">
							<label class="col-md-4 control-label">买单后出总单：</label>
							<div  class="col-md-6 radio-list">
								<label class="radio-inline">
									<input type="radio" name="isPrintPayAfter" v-model="m.isPrintPayAfter" value="1"> 开
								</label>
								<label class="radio-inline">
									<input type="radio" name="isPrintPayAfter" v-model="m.isPrintPayAfter" value="0"> 关
								</label>
							</div>
						</div>
                        <div class="form-group">
                            <label class="col-md-4 control-label" :class="{ formBox : m.isMealFee == 1}">启用餐盒费：</label>
                            <div  class="col-md-6 radio-list">
                                <label class="radio-inline">
                                    <input type="radio" name="isMealFee" v-model="m.isMealFee" value="1"> 是
                                </label>
                                <label class="radio-inline">
                                    <input type="radio" name="isMealFee" v-model="m.isMealFee" value="0"> 否
                                </label>
                            </div>
                        </div>
						<div class="form-group" v-if="m.isMealFee==1">
							<label class="col-md-4 control-label" :class="{ formBox : m.isMealFee == 1}">名称：</label>
							<div  class="col-md-6">
								<input type="text" class="form-control" name="mealFeeName" v-if="!m.mealFeeName" value="餐盒费" required="required">
								<input type="text" class="form-control" name="mealFeeName" v-if="m.mealFeeName" v-model="m.mealFeeName" required="required">
							</div>
						</div>
						<div class="form-group" v-if="m.isMealFee==1">
							<label class="col-md-4 control-label" :class="{ formBox : m.isMealFee == 1}">餐盒费/盒：</label>
							<div  class="col-md-6">
								<input type="number" class="form-control"
									   name="mealFeePrice" placeholder="(建议输入整数)"
									   v-model="m.mealFeePrice" required="required" min="0">
							</div>
						</div>
                        <!--    仅   混合支付模式（大Boos模式）  显示         begin-->
						<div class="form-group"  v-show="m.shopMode == 6">
							<label class="col-md-4 control-label">允许先付：</label>
							<div  class="col-md-6 radio-list">
								<label class="radio-inline">
									<input type="radio" name="allowFirstPay" v-model="m.allowFirstPay" value="0"> 允许
								</label>
								<label class="radio-inline">
									<input type="radio" name="allowFirstPay" v-model="m.allowFirstPay" value="1"> 不允许
								</label>
							</div>
						</div>
						<div class="form-group"  v-show="m.shopMode == 6">
							<label class="col-md-4 control-label">允许后付：</label>
							<div  class="col-md-6 radio-list">
								<label class="radio-inline">
									<input type="radio" name="allowAfterPay" v-model="m.allowAfterPay" value="0"> 允许
								</label>
								<label class="radio-inline">
									<input type="radio" name="allowAfterPay" v-model="m.allowAfterPay" value="1"> 不允许
								</label>
							</div>
						</div>
                        <!--    仅   混合支付模式（大Boos模式）  显示         end-->
						<div class="form-group">
							<label class="col-md-4 control-label">点餐页面滑动效果：</label>
							<div  class="col-md-6 radio-list">
								<label class="radio-inline">
									<input type="radio" name="rollingSwitch" v-model="m.rollingSwitch" value="0"> 不滑动
								</label>
								<label class="radio-inline">
									<input type="radio" name="rollingSwitch" v-model="m.rollingSwitch" value="1"> 滑动
								</label>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-4 control-label" :class="{ formBox : m.isPush == 1}">开启就餐提醒：</label>
							<div  class="col-md-6 radio-list">
								<label class="radio-inline">
									<input type="radio" name="isPush" v-model="m.isPush" value="1"> 是
								</label>
								<label class="radio-inline">
									<input type="radio" name="isPush" v-model="m.isPush" value="0"> 否
								</label>
							</div>
						</div>
						<div class="form-group" v-if="m.isPush==1">
							<label class="col-md-4 control-label" :class="{ formBox : m.isPush == 1}">消息内容：</label>
							<div  class="col-md-6">
								<input type="text" class="form-control" name="pushContext" v-if="!m.pushContext" placeholder="消息文案"
									   required="required">
								<input type="text"  class="form-control" name="pushContext" v-if="m.pushContext" v-model="m.pushContext"
									   required="required">
							</div>
						</div>
						<div class="form-group" v-if="m.isPush==1" >
							<label class="col-md-4 control-label" :class="{ formBox : m.isPush == 1}">推送时间：</label>
							<div  class="col-md-6 ">
								<input type="number" class="form-control" name="pushTime" placeholder="(建议输入整数,以秒为单位)"
									   v-model="m.pushTime" required="required" min="0">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-4 control-label">优惠券到期提醒时间：</label>
							<div  class="col-md-6 ">
								<input type="number" class="form-control"
									   name="recommendTime" placeholder="(输入整数)"
									   v-model="m.recommendTime" required="required" min="0">
							</div>
						</div>
                        <!--    Geek叫号功能    begin-->
						<div class="form-group">
							<label class="col-md-4 control-label">店铺标语：</label>
							<div  class="col-md-6">
								<input type="text" class="form-control" name="slogan" placeholder="请输入店铺标语,不填则取品牌设置的内容"
									   v-model="m.slogan">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-4 control-label">等位提示：</label>
							<div  class="col-md-6 ">
								<textarea rows="3" class="form-control" name="queueNotice" placeholder="请输入等位提示,不填则取品牌设置的内容"
										  v-model="m.queueNotice"></textarea>
							</div>
						</div>
                        <!--    Geek叫号功能    end-->
						<div class="form-group">
							<label class="col-md-4 control-label":class="{ formBox : m.isUserIdentity == 1}">开启显示用户标识功能：</label>
							<div  class="col-md-6 radio-list">
								<label class="radio-inline">
									<input type="radio" name="isUserIdentity" v-model="m.isUserIdentity" value="1"> 是
								</label>
								<label class="radio-inline">
									<input type="radio" name="isUserIdentity" v-model="m.isUserIdentity" value="0"> 否
								</label>
							</div>
						</div>
						<div  class="form-group" v-if="m.isUserIdentity==1">
							<label class="col-md-4 control-label":class="{ formBox : m.isUserIdentity == 1}">高频条件：</label>
							<div  class="col-md-6">
								<div class="form-group col-md-4" v-if="showlate">
									<div class="input-group">
										<div class="input-group-addon">近</div>
										<input type="number" min="1" name="consumeConfineTime" class="form-control"  v-model="m.consumeConfineTime">
									</div>
								</div>
								<div class="col-md-4">
									<select class="form-control" id="consumeConfineUnit"  name="consumeConfineUnit" @click="selectWaitUnit" v-model="m.consumeConfineUnit">
										<option  value="1" selected="selected">日</option>
										<option  value="2">月</option>
										<option  value="3">无限制</option>
									</select>
								</div>
								<div class="input-group col-md-4">
									<div class="input-group-addon">消费</div>
									<input type="number" class="form-control "
										   name="consumeNumber" v-model="m.consumeNumber" required="required" min="0" >
									<div class="input-group-addon">次</div>
								</div>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-4 control-label">外带是否需要扫码：</label>
							<div  class="col-md-6 radio-list">
								<label class="radio-inline">
									<input type="radio" name="continueOrderScan" v-model="m.continueOrderScan" value="0"> 不需要
								</label>
								<label class="radio-inline">
									<input type="radio" name="continueOrderScan" v-model="m.continueOrderScan" value="1"> 需要
								</label>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-4 control-label">扫码进是否堂食：</label>
							<div  class="col-md-6 radio-list">
								<label class="radio-inline">
									<input type="radio" name="sweepMode" v-model="m.sweepMode" value="0"> 默认堂食
								</label>
								<label class="radio-inline">
									<input type="radio" name="sweepMode" v-model="m.sweepMode" value="1"> 需选择就餐模式
								</label>
							</div>
						</div>

						<div class="form-group">
							<label class="col-md-4 control-label">退菜打印订单：</label>
							<div  class="col-md-6 checkbox-list">
								<label class="checkbox-inline">
									<input type="checkbox" name="printReceipt" v-model="m.printReceipt" value = "1"> 打印总单
								</label>
								<label class="checkbox-inline">
									<input type="checkbox" name="printKitchen" v-model="m.printKitchen" value = "1"> 打印厨打
								</label>
							</div>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label" :class="{ formBox : m.isOpenSms == 1}">日结短信通知：</label>
						<div  class="col-md-6 radio-list">
							<label class="radio-inline">
								<input type="radio" name="isOpenSms"v-model="m.isOpenSms" value="1">是
							</label>
							<label class="radio-inline">
								<input type="radio" name="isOpenSms" v-model="m.isOpenSms" value="0"> 否
							</label>
						</div>
					</div>
					<div  class="form-group" v-if="m.isOpenSms==1">
						<label class="col-md-4 control-label" :class="{ formBox : m.isOpenSms == 1}">手机号：</label>
						<div class="col-sm-6">
							<input type="text"  name="noticeTelephone" placeholder="多个手机号码以逗号隔开" class="form-control"  v-model="m.noticeTelephone">
						</div>
					</div>
					<div class="form-group" v-show="b.aliPay == 1">
						<label class="col-md-4 control-label">开启支付宝支付：</label>
						<div  class="col-md-6 radio-list">
							<label class="radio-inline">
								<input type="radio" name="aliPay"v-model="m.aliPay" value="1">启用
							</label>
							<label class="radio-inline">
								<input type="radio" name="aliPay" v-model="m.aliPay" value="0">不启用
							</label>
						</div>
					</div>
					<div class="form-group" v-show="b.openUnionPay == 1">
						<label class="col-md-4 control-label">开启银联支付：</label>
						<div  class="col-md-6 radio-list">
								<label class="radio-inline">
									<input type="radio" name="openUnionPay"v-model="m.openUnionPay" value="1">启用
								</label>
								<label class="radio-inline">
									<input type="radio" name="openUnionPay" v-model="m.openUnionPay" value="0">不启用
								</label>
						</div>
					</div>

					<div class="form-group" v-show="b.openMoneyPay == 1">
						<label class="col-md-4 control-label">开启现金支付：</label>
						<div  class="col-md-6 radio-list">
							<label class="radio-inline">
								<input type="radio" name="openMoneyPay"v-model="m.openMoneyPay" value="1">启用
							</label>
							<label class="radio-inline">
								<input type="radio" name="openMoneyPay" v-model="m.openMoneyPay" value="0">不启用
							</label>
						</div>
					</div>

                    <div class="form-group" v-show="b.integralPay == 1">
                        <label class="col-md-4 control-label">开启积分支付：</label>
                        <div  class="col-md-6 radio-list">
                            <label class="radio-inline">
                                <input type="radio" name="integralPay"v-model="m.integralPay" value="1">启用
                            </label>
                            <label class="radio-inline">
                                <input type="radio" name="integralPay" v-model="m.integralPay" value="0">不启用
                            </label>
                        </div>
                    </div>

					<div class="form-group">
						<label class="col-md-4 control-label">pos加菜是否开启粉丝价：</label>
						<div  class="col-md-6 radio-list">
							<label class="radio-inline">
								<input type="radio" name="posPlusType"v-model="m.posPlusType" value="0">启用
							</label>
							<label class="radio-inline">
								<input type="radio" name="posPlusType" v-model="m.posPlusType" value="1">不启用
							</label>
						</div>
					</div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" :class="{ formBox : m.isUseServicePrice == 1}">是否启用服务费：</label>
                        <div  class="col-md-6 radio-list">
                            <label class="radio-inline">
                                <input type="radio" name="isUseServicePrice"  v-model="m.isUseServicePrice" value="1">	是
                            </label>
                            <label class="radio-inline">
                                <input type="radio" name="isUseServicePrice"  v-model="m.isUseServicePrice" value="0">	否
                            </label>
                        </div>
                    </div>
                    <div v-if="m.isUseServicePrice==1">
                        <div v-if="showp" >
                            <div class="form-group">
                                <label  class="col-sm-4 control-label" :class="{ formBox : m.isUseServicePrice == 1}">名称：</label>
                                <div  class="col-md-6 radio-list">
                                    <input type="test" class="form-control" name="serviceName" v-if="!m.serviceName" value="服务费" required="required">
                                    <input type="test" class="form-control" name="serviceName" v-if="m.serviceName" v-model="m.serviceName" required="required">
                                </div>
                            </div>
                            <div class="form-group">
                                <label  class="col-sm-4 control-label" :class="{ formBox : m.isUseServicePrice == 1}">服务费/每人：</label>
                                <div  class="col-md-6 radio-list">
                                    <input type="number" class="form-control" name="servicePrice" v-model="m.servicePrice" required="required" min="0">
                                </div>
                            </div>
                        </div>
                    </div>

                    <! -- 第三方接口appid-->
                    <div  class="form-group" v-if="b.openThirdInterface==1">
                        <label class="col-md-4 control-label" :class="{ formBox : b.openThirdInterface == 1}">第三方接口appid：</label>
                        <div class="col-sm-6">
                            <input type="text"  name="thirdAppid"  class="form-control"  v-model="m.thirdAppid">
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
	$.ajax({
		url:"shopInfo/list_one",
		success:function(result){
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
						m : result.data.shop,
						b : result.data.brand,
						showa:true,
						showlate:true,
						showp:true
					},
					watch: {
						'm.consumeConfineUnit': 'hideShowa',
						'm.isUseServicePrice':'hideServiceP'
					},
					methods : {
						hideServiceP  :function (){
							if(result.data.isUseServicePrice ==0){
								this.showp = false;
							}else{
								this.showp = true;
							}
						},
						hideShowa : function(){
							if(this.m.consumeConfineUnit == 3){
								this.showa = false;
								this.showlate=false;
							}else{
								this.showa = true;
								this.showlate=true;
							}
						},
						initTime : function() {
							$(".timepicker-no-seconds").timepicker({
								autoclose : true,
								showMeridian : false,
								minuteStep : 5
							});
						},
						save : function(e) {
							var formDom = e.target;
							var allowAfterPay = $("input[name='allowAfterPay']:checked").val();
							var allowFirstPay = $("input[name='allowFirstPay']:checked").val();
							if(allowAfterPay == 1 && allowFirstPay == 1){
								toastr.clear();
								toastr.error("混合支付模式下不可以同时关闭2种支付方式！");
								return;
							}
							$.ajax({
								url : "shopInfo/modify",
								data : $(formDom).serialize(),
								success : function(result) {
									console.log(result+"----");
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
							this.initContent();

						},
						uploadSuccess:function(url){
							$("[name='photo']").val(url).trigger("change");
							toastr.success("上传成功！");
						},
						uploadError:function(msg){
							toastr.error("上传失败");
						},
						initContent:function(){
							var that = this;
							$.ajax({
								url:"shopInfo/list_one",
								type:"post",
								dataType:"json",
								success:function (resultData) {
									that.m = resultData.data.shop;
									that.b = resultData.data.brand;
								}
							});
						}
					}
				});

			}());
		}

	})

</script>
