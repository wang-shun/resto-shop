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

						<div class="form-group">
							<label class="col-sm-3 control-label">打印总单：</label>
							<div class="col-sm-9">
								<div>
									<label >
										<input type="radio" name="autoPrintTotal" v-model="m.autoPrintTotal" value="0">
										是
									</label>
									<label>
										<input type="radio" name="autoPrintTotal" v-model="m.autoPrintTotal" value="1">
										否
									</label>
								</div>
							</div>
						</div>

						<div class="form-group">
							<label class="col-sm-3 control-label">套餐出单方式：</label>
							<div class="col-sm-9">
								<div>
									<label >
										<input type="radio" name="printType" v-model="m.printType" value="0">
										整单出单
									</label>
									<label>
										<input type="radio" name="printType" v-model="m.printType" value="1">
										分单出单
									</label>
								</div>
							</div>
						</div>




						<div class="form-group">
							<label class="col-sm-3 control-label">买单后出总单（后付款模式）：</label>
							<div class="col-sm-9">
								<div>
									<label>
										<input type="radio" name="isPrintPayAfter" v-model="m.isPrintPayAfter" value="1">
										开
									</label>
									<label>
										<input type="radio" name="isPrintPayAfter"
											   v-model="m.isPrintPayAfter" value="0">
										关
									</label>
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

						<div class="form-group">
							<label class="col-sm-3 control-label">允许先付（仅混合支付模式有效）：</label>
							<div class="col-sm-9">
								<div>
									<label> <input type="radio" name="allowFirstPay"
												   v-model="m.allowFirstPay" value="0"> 允许
									</label> <label> <input type="radio" name="allowFirstPay"
															v-model="m.allowFirstPay" value="1"> 不允许
								</label>
								</div>
							</div>
						</div>

						<div class="form-group">
							<label class="col-sm-3 control-label">允许后付（仅混合支付模式有效）：</label>
							<div class="col-sm-9">
								<div>
									<label> <input type="radio" name="allowAfterPay"
												   v-model="m.allowAfterPay" value="0"> 允许
									</label> <label> <input type="radio" name="allowAfterPay"
															v-model="m.allowAfterPay" value="1"> 不允许
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



						<div class="form-group">
							<label class="col-sm-3 control-label">红包到期提醒时间：</label>
							<div class="col-sm-9">
								<input type="number" class="form-control"
									   name="recommendTime" placeholder="(输入整数)"
									   v-model="m.recommendTime" required="required" min="0">
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

						<div class="form-group">
							<label class="col-sm-3 control-label">开启显示用户标识功能：</label>
							<div class="col-sm-9">
								<div>
									<label> <input type="radio" name="isUserIdentity"
												   v-model="m.isUserIdentity" value="1"> 是
									</label> <label> <input type="radio" name="isUserIdentity"
															v-model="m.isUserIdentity" value="0"> 否
								</label>
								</div>
							</div>
						</div>


						<div class="form-group">
							<label class="col-sm-3 control-label">外带是否需要扫码：</label>
							<div class="col-sm-9">
								<div>
									<label> <input type="radio" name="continueOrderScan"
												   v-model="m.continueOrderScan" value="0"> 不需要
									</label> <label> <input type="radio" name="continueOrderScan"
															v-model="m.continueOrderScan" value="1"> 需要
								</label>
								</div>
							</div>
						</div>


						<div  class="form-group" v-if="m.isUserIdentity==1">
							<label class="col-sm-3 control-label">高频条件：<span v-if="showlate" >近</span></label>
							<input v-if="showa" type="number" min="1" name="consumeConfineTime" class="form-control"  v-model="m.consumeConfineTime" style="width:12%;float:left;margin-right: 5px;margin-left: 19px;" >
							<select class="form-control" style="width:12%;position: relative;left: 15px;" id="consumeConfineUnit"  name="consumeConfineUnit" @click="selectWaitUnit" v-model="m.consumeConfineUnit">
								<option  value="1" selected="selected">日</option>
								<option  value="2">月</option>
								<option  value="3">无限制</option>
							</select>
							<span style="float: right;margin-top: -25px; margin-right: 42%;">消费</span>
							<div class="col-sm-3" style="width: 12%; float:right;margin-top: -35px;margin-right:30%;">
								<input type="number" class="form-control"
									   name="consumeNumber" v-model="m.consumeNumber" required="required" min="0" >
								<span style="position: relative;left:70px;bottom: 25px;">次</span>
							</div>
						</div>

						<div class="form-group">
							<label class="col-sm-3 control-label">退菜打印订单：</label>
							<div class="col-sm-9">
								<div>
									<label class="checkbox-inline">
										<input type="checkbox" name="printReceipt" v-model="m.printReceipt" value = "1"> 打印总单
									</label>
									<label class="checkbox-inline">
										<input type="checkbox" name="printKitchen" v-model="m.printKitchen" value = "1"> 打印厨打
									</label>
								</div>
							</div>
						</div>
					</div>


                    <div class="form-group">
                        <label class="col-sm-3 control-label">日短信通知：</label>
                        <div class="col-sm-9">
                            <div>
                                <label> <input type="radio" name="isOpenSms"v-model="m.isOpenSms" value="1">是
                                </label>
                                <label> <input type="radio" name="isOpenSms" v-model="m.isOpenSms" value="0"> 否
                            </label>
                            </div>
                        </div>
                    </div>


                    <div  class="form-group" v-if="m.isOpenSms==1">
                            <label class="col-sm-3 control-label">手机号：</label>
                            <div class="col-sm-8">
                                <input type="text"  name="noticeTelephone" placeholder="多个手机号码以逗号隔开" class="form-control"  v-model="m.noticeTelephone">
                            </div>
                    </div>

					<div class="form-group" style="margin-left: 55px;">
						<div class="control-label" style="text-align: inherit">是否启用服务费</div>
						<label style="position: relative;left: 195px;bottom: 21px;">
							<input type="radio" name="isUseServicePrices"  v-model="m.isUseServicePrices" value="0">
							否
						</label>
						<label style="position: relative;left: 105px;bottom: 21px;">
							<input type="radio" name="isUseServicePrices"  v-model="m.isUseServicePrices" value="1">
							是
						</label>
					</div>
					<div v-if="m.isUseServicePrices==1">
					<div v-if="showp" >
					<div class="form-group" id="serviceDivOne"  >
						<label>名称</label>
						<input type="test" class="form-control" name="serviceNames" v-if="!m.serviceNames" value="服务费" required="required">
						<input type="test" class="form-control" name="serviceNames" v-if="m.serviceNames" v-model="m.serviceNames" required="required">
					</div>
						<div class="form-group" id="serviceDivTwo">
							<label>服务费/每人</label>
							<input type="number" class="form-control" name="servicePrices" v-model="m.servicePrices" required="required" min="0">
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
						m : result.data,
						showa:true,
						showlate:true,
						showp:true
					},
					watch: {
						'm.consumeConfineUnit': 'hideShowa',
						'm.isUseServicePrices':'hideServiceP'
					},
					methods : {
						hideServiceP  :function (){
						if(result.data.isUseServicePrices ==0){
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
                                    that.m = resultData.data;
                                }
                            });
                        }
					}
				});

			}());
		}

	})

</script>
