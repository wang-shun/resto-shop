<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>
<style>
	.formBox{
        color: #5bc0de;
	}
	.gray{
		color: #5e6672;
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

                        <div class="form-group">
                            <label class="col-md-4 control-label">POS更新订单打印：</label>
                            <div  class="col-md-6 checkbox-list">
                                <label class="checkbox-inline">
                                    <input type="checkbox" name="modifyOrderPrintReceipt" v-model="m.modifyOrderPrintReceipt" value = "1"> 打印总单
                                </label>
                                <label class="checkbox-inline">
                                    <input type="checkbox" name="modifyOrderPrintKitchen" v-model="m.modifyOrderPrintKitchen" value = "1"> 打印厨打
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

                    <%--<div class="form-group" v-if="m.isOpenSms==1">--%>
                        <%--<label class="col-md-4 control-label" :class="{ formBox : m.isOpenSms == 1}">通知方式：</label>--%>
                        <%--<div  class="col-md-6 radio-list">--%>
                            <%--<label class="radio-inline">--%>
                                <%--<input type="radio" name="smsType"v-model="m.daySmsType" value="1">短信推送--%>
                            <%--</label>--%>
                            <%--<label class="radio-inline">--%>
                                <%--<input type="radio" name="smsType" v-model="m.daySmsType" value="2">微信推送--%>
                            <%--</label>--%>
                        <%--</div>--%>
                    <%--</div>--%>


                    <div class="form-group" v-show="m.isOpenSms==1">
                        <label class="col-md-4 control-label" :class="{ formBox : m.isOpenSms == 1}">通知方式：</label>
                        <input type="hidden" name="daySmsType" v-model="getDaySmsType">
                        <%--day_sms_type--%>
                        <div  class="col-md-6 radio-list checkbox">
                            <label style="margin-left: 16px;">
                                <input type="checkbox"  :true-value="2"  v-model="daySmsTypeWx" disabled>
                                &nbsp;&nbsp;微信推送
                            </label>
                            <label style="margin-left: 16px;">
                                <input type="checkbox" :true-value="1"  v-model="daySmsTypeSms" >
                                &nbsp;&nbsp;短信推送
                            </label>
                        </div>
                    </div>

					<div class="form-group">
						<label class="col-md-4 control-label">微信端支付项：</label>
						<div  class="col-md-6 radio-list checkbox">
                            <label style="margin-left: 16px;">
                                <input type="checkbox" checked="checked" disabled="disabled">
                                &nbsp;&nbsp;微信支付
                            </label>
                            <label style="margin-left: 16px;" v-show="b.aliPay == 1">
                                <input type="checkbox" id="aliPay" @change="weChatPaySetting('aliPay')" v-model="m.aliPay" value="1">
                                <input type="hidden" name="aliPay" v-model="m.aliPay">
                                &nbsp;&nbsp;支付宝支付
                            </label>
                            <label style="margin-left: 16px;" v-show="b.openUnionPay == 1 && m.shopMode == 6">
                                <input type="checkbox" id="openUnionPay" @change="weChatPaySetting('openUnionPay')" v-model="m.openUnionPay" value="1">
                                <input type="hidden" name="openUnionPay" v-model="m.openUnionPay">
                                &nbsp;&nbsp;银联支付
                            </label>
                            <label style="margin-left: 16px;" v-show="b.openMoneyPay == 1 && m.shopMode == 6">
                                <input type="checkbox" id="openMoneyPay" @change="weChatPaySetting('openMoneyPay')" v-model="m.openMoneyPay" value="1">
                                <input type="hidden" name="openMoneyPay" v-model="m.openMoneyPay">
                                &nbsp;&nbsp;现金支付
                            </label>
                            <label style="margin-left: 16px;" :class="{ formBox : m.openShanhuiPay == 1}" v-show="b.openShanhuiPay == 1 && m.shopMode == 6">
                                <input type="checkbox" id="openShanhuiPay" @change="weChatPaySetting('openShanhuiPay')" v-model="m.openShanhuiPay" value="1">
                                <input type="hidden" name="openShanhuiPay" v-model="m.openShanhuiPay">
                                &nbsp;&nbsp;美团闪惠支付
                                <div class="form-group" v-if="b.openShanhuiPay == 1 && m.openShanhuiPay==1">
                                    <label class="col-md-3 control-label" :class="{ formBox : m.openShanhuiPay == 1}">大众点评店铺ID：</label>
                                    <div  class="col-md-6">
                                        <input type="text" class="form-control" name="dazhongShopId" v-model="m.dazhongShopId" required="required">
                                    </div>
                                </div>
                            </label>
                            <label style="margin-left: 16px;" v-show="b.integralPay == 1 && m.shopMode == 6">
                                <input type="checkbox" id="integralPay" @change="weChatPaySetting('integralPay')" v-model="m.integralPay" value="1">
                                <input type="hidden" name="integralPay" v-model="m.integralPay">
                                &nbsp;&nbsp;会员支付
                            </label>
						</div>
					</div>

                    <div class="form-group">
                        <label class="col-md-4 control-label">POS端支付项：</label>
                        <div  class="col-md-6 radio-list checkbox">
                            <label style="margin-left: 16px;">
                                <input type="checkbox" id="openPosWeChatPay" @change="posPaySetting('openPosWeChatPay')" v-model="m.openPosWeChatPay" value="1">
                                <input type="hidden" name="openPosWeChatPay" v-model="m.openPosWeChatPay">
                                &nbsp;&nbsp;微信支付
                            </label>
                            <label style="margin-left: 16px;" v-show="b.aliPay == 1">
                                <input type="checkbox" id="openPosAliPay" @change="posPaySetting('openPosAliPay')" v-model="m.openPosAliPay" value="1">
                                <input type="hidden" name="openPosAliPay" v-model="m.openPosAliPay">
                                &nbsp;&nbsp;支付宝支付
                            </label>
                            <label style="margin-left: 16px;" v-show="b.openUnionPay == 1">
                                <input type="checkbox" id="openPosUnionPay" @change="posPaySetting('openPosUnionPay')" v-model="m.openPosUnionPay" value="1">
                                <input type="hidden" name="openPosUnionPay" v-model="m.openPosUnionPay">
                                &nbsp;&nbsp;银联支付
                            </label>
                            <label style="margin-left: 16px;" v-show="b.openMoneyPay == 1">
                                <input type="checkbox" id="openPosMoneyPay" @change="posPaySetting('openPosMoneyPay')" v-model="m.openPosMoneyPay" value="1">
                                <input type="hidden" name="openPosMoneyPay" v-model="m.openPosMoneyPay">
                                &nbsp;&nbsp;现金支付
                            </label>
                            <label style="margin-left: 16px;" v-show="b.openShanhuiPay == 1">
                                <input type="checkbox" id="openPosShanhuiPay" @change="posPaySetting('openPosShanhuiPay')" v-model="m.openPosShanhuiPay" value="1">
                                <input type="hidden" name="openPosShanhuiPay" v-model="m.openPosShanhuiPay">
                                &nbsp;&nbsp;美团闪惠支付
                            </label>
                            <label style="margin-left: 16px;" v-show="b.integralPay == 1">
                                <input type="checkbox" id="openPosIntegralPay" @change="posPaySetting('openPosIntegralPay')" v-model="m.openPosIntegralPay" value="1">
                                <input type="hidden" name="openPosIntegralPay" v-model="m.openPosIntegralPay">
                                &nbsp;&nbsp;会员支付
                            </label>
                        </div>
                    </div>

					<div class="form-group" v-show="b.posOpenTable == 1">
						<label class="col-md-4 control-label">开启POS点单：</label>
						<div  class="col-md-6 radio-list">
							<label class="radio-inline">
								<input type="radio" name="posOpenTable"v-model="m.posOpenTable" value="1">启用
							</label>
							<label class="radio-inline">
								<input type="radio" name="posOpenTable" v-model="m.posOpenTable" value="0">不启用
							</label>
						</div>
					</div>

					<div class="form-group">
						<label class="col-md-4 control-label">pos点单菜品价格：</label>
						<div  class="col-md-6 radio-list">
							<label class="radio-inline">
								<input type="radio" name="posPlusType"v-model="m.posPlusType" value="0">粉丝价
							</label>
							<label class="radio-inline">
								<input type="radio" name="posPlusType" v-model="m.posPlusType" value="1">原价
							</label>
						</div>
					</div>

					<div class="form-group">
						<label class="col-md-4 control-label">是否开启pos折扣：</label>
						<div  class="col-md-6 radio-list">
							<label class="radio-inline">
								<input type="radio" name="openPosDiscount"v-model="m.openPosDiscount" value="1">开启
							</label>
							<label class="radio-inline">
								<input type="radio" name="openPosDiscount" v-model="m.openPosDiscount" value="0">不启用
							</label>
						</div>
					</div>

                    <div class="form-group" v-show="m.shopMode == 6 && m.allowAfterPay == 0">
                        <label class="col-md-4 control-label">开启POS端订单结算功能：</label>
                        <div  class="col-md-6 radio-list">
                            <label class="radio-inline">
                                <input type="radio" name="openPosPayOrder"v-model="m.openPosPayOrder" value="1">启用
                            </label>
                            <label class="radio-inline">
                                <input type="radio" name="openPosPayOrder" v-model="m.openPosPayOrder" value="0">不启用
                            </label>
                        </div>
                    </div>

                    <div class="form-group" v-show="b.openPosCharge == 1">
                        <label class="col-md-4 control-label">开启POS账户充值：</label>
                        <div  class="col-md-6 radio-list">
                            <label class="radio-inline">
                                <input type="radio" name="openPosCharge"v-model="m.openPosCharge" value="1">启用
                            </label>
                            <label class="radio-inline">
                                <input type="radio" name="openPosCharge" v-model="m.openPosCharge" value="0">不启用
                            </label>
                        </div>
                    </div>

                    <div class="form-group" v-show="b.openOrderRemark == 1">
                        <label class="col-md-4 control-label">开启订单备注：</label>
                        <div  class="col-md-6 radio-list">
                            <label class="radio-inline">
                                <input type="radio" name="openOrderRemark"v-model="m.openOrderRemark" value="1">启用
                            </label>
                            <label class="radio-inline">
                                <input type="radio" name="openOrderRemark" v-model="m.openOrderRemark" value="0">不启用
                            </label>
                        </div>
                    </div>

					<div class="form-group">
						<label class="col-md-4 control-label">日结小票模板类型：</label>
						<div  class="col-md-6 radio-list">
							<label class="radio-inline">
								<input type="radio" name="templateType"v-model="m.templateType" value="0">经典版
							</label>
							<label class="radio-inline">
								<input type="radio" name="templateType" v-model="m.templateType" value="1">升级版
							</label>
						</div>
					</div>

					<div class="form-group">
						<label class="col-md-4 control-label">是否开启美团外卖自动出单：</label>
						<div  class="col-md-6 radio-list">
							<label class="radio-inline">
								<input type="radio" name="printMeituan"v-model="m.printMeituan" value="1">启用
							</label>
							<label class="radio-inline">
								<input type="radio" name="printMeituan" v-model="m.printMeituan" value="0">不启用
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

					<div class="form-group" v-show="m.isUseServicePrice == 1">
						<label class="col-md-4 control-label" :class="{ formBox : m.isUseServicePrice == 1}">服务费版本：</label>
						<div  class="col-md-6 radio-list">
							<label class="radio-inline">
								<input type="radio" name="serviceType"  v-model="m.serviceType" value="0">	经典版
							</label>
							<label class="radio-inline">
								<input type="radio" name="serviceType"  v-model="m.serviceType" value="1">	升级版
							</label>
						</div>
					</div>

					<!-- 服务费经典版begin -->
                    <div v-if="m.isUseServicePrice==1 && m.serviceType == 0">
                        <div v-show="showp" >
                            <div class="form-group">
                                <label  class="col-sm-4 control-label formBox">名称：</label>
                                <div  class="col-md-6 radio-list">
                                    <input type="test" class="form-control" name="serviceName" v-if="!m.serviceName" value="服务费" required="required">
                                    <input type="test" class="form-control" name="serviceName" v-if="m.serviceName" v-model="m.serviceName" required="required">
                                </div>
                            </div>
                            <div class="form-group">
                                <label  class="col-sm-4 control-label formBox">服务费/每人：</label>
                                <div  class="col-md-6 radio-list">
                                    <input type="number" class="form-control" name="servicePrice" v-model="m.servicePrice" required="required" min="0">
                                </div>
                            </div>
                        </div>
                    </div>
					<!-- 服务费经典版end -->

					<!-- 服务费升级版begin -->
					<div class="form-group" v-if="m.isUseServicePrice == 1 && m.serviceType == 1">
						<label  class="col-sm-4 control-label" :class="{ formBox : m.isOpenTablewareFee == 1, gray : m.isOpenTablewareFee == 0}" style="margin-top: 20px;">餐具费：</label>
						<div  class="col-md-6">
							<div class="row">
								<div class="col-md-4">
									<p style="text-align: center;margin: 5px 0;font-weight: bold">名称</p>
								</div>
								<div class="col-md-4">
									<p style="text-align: center;margin: 5px 0;font-weight: bold">价格</p>
								</div>
								<div class="col-md-4">
									<p style="text-align: center;margin: 5px 0;font-weight: bold">是否启用(勾选启用)</p>
								</div>
							</div>
							<div class="row">
								<div class="col-md-4">
									<input v-show="m.isOpenTablewareFee == 0" type="text" class="form-control" disabled>
									<input v-else type="text" class="form-control" name="tablewareFeeName" v-model="m.tablewareFeeName" required>
								</div>
								<div class="col-md-4">
									<input v-show="m.isOpenTablewareFee == 0" type="text" class="form-control" disabled>
									<input v-else type="number" class="form-control" name="tablewareFeePrice" v-model="m.tablewareFeePrice" required>
								</div>
								<div class="col-md-4" style="text-align: center;margin-top: 8px;">
									<input type="checkbox" class="form-control" value="1" name="isOpenTablewareFee" v-model="m.isOpenTablewareFee">
								</div>
							</div>
						</div>
					</div>

					<div class="form-group" v-if="m.isUseServicePrice == 1 && m.serviceType == 1">
						<label  class="col-sm-4 control-label" :class="{ formBox : m.isOpenTowelFee == 1, gray : m.isOpenTowelFee == 0}" style="margin-top: 20px;">纸巾费：</label>
						<div  class="col-md-6">
							<div class="row">
								<div class="col-md-4">
									<p style="text-align: center;margin: 5px 0;font-weight: bold">名称</p>
								</div>
								<div class="col-md-4">
									<p style="text-align: center;margin: 5px 0;font-weight: bold">价格</p>
								</div>
								<div class="col-md-4">
									<p style="text-align: center;margin: 5px 0;font-weight: bold">是否启用(勾选启用)</p>
								</div>
							</div>
							<div class="row">
								<div class="col-md-4">
									<input v-show="m.isOpenTowelFee == 0" type="text" class="form-control" disabled>
									<input v-else type="text" class="form-control" name="towelFeeName" v-model="m.towelFeeName" required>
								</div>
								<div class="col-md-4">
									<input v-show="m.isOpenTowelFee == 0" type="text" class="form-control" disabled>
									<input v-else type="number" class="form-control" name="towelFeePrice" v-model="m.towelFeePrice" required>
								</div>
								<div class="col-md-4" style="text-align: center;margin-top: 8px;">
									<input type="checkbox" class="form-control" value="1" name="isOpenTowelFee" v-model="m.isOpenTowelFee">
								</div>
							</div>
						</div>
					</div>

					<div class="form-group" v-if="m.isUseServicePrice == 1 && m.serviceType == 1">
						<label  class="col-sm-4 control-label" :class="{ formBox : m.isOpenSauceFee == 1, gray : m.isOpenSauceFee == 0}" style="margin-top: 20px;">酱料费：</label>
						<div  class="col-md-6">
							<div class="row">
								<div class="col-md-4">
									<p style="text-align: center;margin: 5px 0;font-weight: bold">名称</p>
								</div>
								<div class="col-md-4">
									<p style="text-align: center;margin: 5px 0;font-weight: bold">价格</p>
								</div>
								<div class="col-md-4">
									<p style="text-align: center;margin: 5px 0;font-weight: bold">是否启用(勾选启用)</p>
								</div>
							</div>
							<div class="row">
								<div class="col-md-4">
									<input v-show="m.isOpenSauceFee == 0" type="text" class="form-control" disabled>
									<input v-else type="text" class="form-control" name="sauceFeeName" v-model="m.sauceFeeName" required>
								</div>
								<div class="col-md-4">
									<input v-show="m.isOpenSauceFee == 0" type="text" class="form-control" disabled>
									<input v-else type="number" class="form-control" name="sauceFeePrice" v-model="m.sauceFeePrice" required>
								</div>
								<div class="col-md-4" style="text-align: center;margin-top: 8px;">
									<input type="checkbox" class="form-control" value="1" name="isOpenSauceFee" v-model="m.isOpenSauceFee">
								</div>
							</div>
						</div>
					</div>
					<!-- 服务费升级版end -->

                    <!-- 第三方接口appid-->
                    <div  class="form-group" v-if="b.openThirdInterface==1">
                        <label class="col-md-4 control-label">第三方接口appid：</label>
                        <div class="col-sm-6">
                            <input type="text"  name="thirdAppid"  class="form-control"  v-model="m.thirdAppid">
                        </div>
                    </div>

					<div class="form-group">
						<label class="col-md-4 control-label" >是否选择配送模式：</label>
						<div  class="col-md-6 radio-list">
							<label class="radio-inline">
								<input type="radio" name="isChoiceMode"v-model="m.isChoiceMode" value="1">是
							</label>
							<label class="radio-inline">
								<input type="radio" name="isChoiceMode" v-model="m.isChoiceMode" value="0">否
							</label>
						</div>
					</div>

					<div class="form-group">
						<label class="col-md-4 control-label">本地电视IP：</label>
						<div  class="col-md-6">
							<input type="text" class="form-control" name="tvIp" :value="m.tvIp" >
						</div>
					</div>

					<div class="form-group">
						<label class="col-md-4 control-label">等位叫号本地电视IP：</label>
						<div  class="col-md-6">
							<input type="text" class="form-control" name="waitIp" :value="m.waitIp" >
						</div>
					</div>


					<div class="form-group">
						<label class="col-md-4 control-label" >厨打是否拆分打印：</label>
						<div  class="col-md-6 radio-list">
							<label class="radio-inline">
								<input type="radio" name="splitKitchen"v-model="m.splitKitchen" value="1">是
							</label>
							<label class="radio-inline">
								<input type="radio" name="splitKitchen" v-model="m.splitKitchen" value="0">否
							</label>
						</div>
					</div>

					<div class="form-group">
						<label class="col-md-4 control-label" >推荐菜品是否开启：</label>
						<div  class="col-md-6 radio-list">
							<label class="radio-inline">
								<input type="radio" name="isRecommendCategory"v-model="m.isRecommendCategory" value="1">是
							</label>
							<label class="radio-inline">
								<input type="radio" name="isRecommendCategory" v-model="m.isRecommendCategory" value="0">否
							</label>
						</div>
					</div>

					<div class="form-group">
						<label class="col-md-4 control-label" :class="{ formBox : m.isTurntable == 1}">换桌是否开启：</label>
						<div  class="col-md-6 radio-list">
							<label class="radio-inline">
								<input type="radio" name="isTurntable"v-model="m.isTurntable" value="1">是
							</label>
							<label class="radio-inline">
								<input type="radio" name="isTurntable" v-model="m.isTurntable" value="0">否
							</label>
						</div>
					</div>
					<div class="form-group" v-show="m.isTurntable == 1">
						<label class="col-md-4 control-label" :class="{ formBox : m.isTurntable == 1}">打印方式：</label>
						<div  class="col-md-6 radio-list checkbox">
							<label style="margin-left: 16px;">
								<input type="checkbox" name="turntablePrintReceipt" :true-value="1" v-model="m.turntablePrintReceipt">
								&nbsp;&nbsp;前台打印
							</label>
							<label style="margin-left: 16px;">
								<input type="checkbox" name="turntablePrintKitchen" :true-value="1"  v-model="m.turntablePrintKitchen" >
								&nbsp;&nbsp;厨房打印
							</label>
						</div>
					</div>

					<div class="form-group">
						<label class="col-md-4 control-label" :class="{ formBox : m.openBadAppraisePrintOrder == 1}">差评是否打印通知：</label>
						<div  class="col-md-6 radio-list">
							<label class="radio-inline">
								<input type="radio" name="openBadAppraisePrintOrder"v-model="m.openBadAppraisePrintOrder" value="1">是
							</label>
							<label class="radio-inline">
								<input type="radio" name="openBadAppraisePrintOrder" v-model="m.openBadAppraisePrintOrder" value="0"> 否
							</label>
						</div>
					</div>

					<div class="form-group" v-show="m.openBadAppraisePrintOrder == 1">
						<label class="col-md-4 control-label" :class="{ formBox : m.openBadAppraisePrintOrder == 1}">打印方式：</label>
						<input type="hidden" name="daySmsType" v-model="getDaySmsType">
						<div  class="col-md-6 radio-list checkbox">
							<label style="margin-left: 16px;">
								<input type="checkbox" name="badAppraisePrintReceipt" :true-value="1" v-model="m.badAppraisePrintReceipt">
								&nbsp;&nbsp;前台打印
							</label>
							<label style="margin-left: 16px;">
								<input type="checkbox" name="badAppraisePrintKitchen" :true-value="1"  v-model="m.badAppraisePrintKitchen" >
								&nbsp;&nbsp;厨房打印
							</label>
						</div>
					</div>

					<%--<div class="form-group">--%>
						<%--<label class="col-md-4 control-label" :class="{ formBox : m.openConsumerRebate == 1}">消费返利功能：</label>--%>
						<%--<div  class="col-md-6 radio-list">--%>
							<%--<label class="radio-inline">--%>
								<%--<input type="radio" name="openConsumerRebate"v-model="m.openConsumerRebate" value="1">开启--%>
							<%--</label>--%>
							<%--<label class="radio-inline">--%>
								<%--<input type="radio" name="openConsumerRebate" v-model="m.openConsumerRebate" value="0">关闭--%>
							<%--</label>--%>
						<%--</div>--%>
					<%--</div>--%>

					<%--<div class="form-group" v-show="m.openConsumerRebate == 1">--%>
						<%--<label class="col-md-4 control-label" :class="{ formBox : m.openConsumerRebate == 1}">用户参与次数：</label>--%>
						<%--<div class="col-sm-6">--%>
							<%--<input type="number"  name="rebateParticipation"  class="form-control"  v-model="m.rebateParticipation" required="required" min="0">--%>
						<%--</div>--%>
					<%--</div>--%>

					<%--<div class="form-group" v-show="m.openConsumerRebate == 1">--%>
						<%--<label class="col-md-4 control-label" :class="{ formBox : m.openConsumerRebate == 1}">返利延迟发放时间：</label>--%>
						<%--<div class="col-sm-6">--%>
							<%--<input type="number"  name="rebateDelayDeliveryTime"  class="form-control"  v-model="m.rebateDelayDeliveryTime" required="required" min="0">--%>
						<%--</div>--%>
					<%--</div>--%>

					<%--<div class="form-group">--%>
						<%--<label class="col-md-4 control-label" >菜品图片展现：</label>--%>
						<%--<div  class="col-md-6 radio-list">--%>
							<%--<label class="radio-inline">--%>
								<%--<input type="radio" name="articlePhoto"v-model="m.articlePhoto" value="0">大图--%>
							<%--</label>--%>
							<%--<label class="radio-inline">--%>
								<%--<input type="radio" name="articlePhoto" v-model="m.articlePhoto" value="1">小图--%>
							<%--</label>--%>
						<%--</div>--%>
					<%--</div>--%>

                    <div class="form-group">
                        <label class="col-md-4 control-label" >菜品图片展现：</label>
                        <div  class="col-md-6 radio-list">
                            <label class="radio-inline">
                                <input type="radio" name="articlePhoto"v-model="m.articlePhoto" value="0">大图
                            </label>
                            <label class="radio-inline">
                                <input type="radio" name="articlePhoto" v-model="m.articlePhoto" value="1">小图
                            </label>
                        </div>
                    </div>
					<div class="form-group">
						<label class="col-md-4 control-label">R+外卖最大配送范围(单位km)：</label>
						<div  class="col-md-6">
							<input type="text" class="form-control" name="apart" :value="m.apart" >
						</div>
					</div>

					<div class="form-group" v-if="b.consumptionRebate==1">
						<label class="col-md-4 control-label">是否开启消费返利(1:1)：</label>
						<div  class="col-md-6 radio-list">
							<label class="radio-inline">
								<input type="radio" name="consumptionRebate"v-model="m.consumptionRebate" value="1">是
							</label>
							<label class="radio-inline">
								<input type="radio" name="consumptionRebate" v-model="m.consumptionRebate" value="0"> 否
							</label>
						</div>
					</div>

					<div class="form-group" v-if="m.consumptionRebate==1 && b.consumptionRebate==1">
						<label class="col-md-4 control-label">消费返利解冻时间:</label>
						<div class="col-md-6 radio-list">
							<div class="input-group date form_datetime">
								<input type="text" readonly class="form-control" name="rebateTime" v-model="m.rebateTime" @focus="initCouponTime"> <span class="input-group-btn">
											<button class="btn default date-set" type="button">
												<i class="fa fa-calendar" @click="initCouponTime"></i>
											</button>
										</span>
							</div>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label" :class="{ formBox : m.orderBefore == 1}">餐品预点餐：</label>
						<div  class="col-md-6 radio-list">
							<label class="radio-inline">
								<input type="radio" name="orderBefore"v-model="m.orderBefore" value="1">开启
							</label>
							<label class="radio-inline">
								<input type="radio" name="orderBefore" v-model="m.orderBefore" value="0"> 未开启
							</label>
						</div>
					</div>

					<div class="form-group">
						<label class="col-md-4 control-label" :class="{ formBox : m.openBadWarning == 1}">开启差评预警：</label>
						<div  class="col-md-6 radio-list">
							<label class="radio-inline">
								<input type="radio" name="openBadWarning"v-model="m.openBadWarning" value="1">是
							</label>
							<label class="radio-inline">
								<input type="radio" name="openBadWarning" v-model="m.openBadWarning" value="0"> 否
							</label>
						</div>
					</div>

					<div class="form-group" v-show="m.openBadWarning==1">
						<label class="col-md-4 control-label" :class="{ formBox : m.openBadWarning == 1}">预警通知方式：</label>
						<div  class="col-md-6 radio-list">
							<label class="radio-inline">
								<input type="checkbox" name="warningSms"v-model="m.warningSms" value="1">短信推送
							</label>
							<label class="radio-inline">
								<input type="checkbox" name="warningWechat" v-model="m.warningWechat" value="1">微信推送
							</label>
						</div>
					</div>

					<div class="form-group" v-show="m.openBadWarning == 1">
						<label class="col-md-4 control-label" :class="{ formBox : m.openBadWarning == 1}">差评预警关键词：</label>
						<div  class="col-md-6">
							<textarea class="form-control" v-model="m.warningKey" name="warningKey">
							</textarea><font color="red">*多个关键词中间以英文状态下的逗号(,)隔开</font>
						</div>
					</div>

					<div class="form-group">
						<label class="col-md-4 control-label">是否开启多人点餐：</label>
						<div  class="col-md-6 radio-list">
							<label class="radio-inline">
								<input type="radio" name="openManyCustomerOrder"v-model="m.openManyCustomerOrder" value="1">是
							</label>
							<label class="radio-inline">
								<input type="radio" name="openManyCustomerOrder" v-model="m.openManyCustomerOrder" value="0">否
							</label>
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
						showp:true,
                        getDaySmsType:null,
                        daySmsTypeWx : 2,
                        daySmsTypeSms : 0
					},
					watch: {
						'm.consumeConfineUnit': 'hideShowa',
						'm.isUseServicePrice':'hideServiceP',
					},
                    computed : {
                        getDaySmsType : function () {
                            return this.daySmsTypeSms + this.daySmsTypeWx;
                        }
                    },
                    created : function () {
					    //初始化 日结短信   通知方式
                        if(this.m.daySmsType==3){
                            this.daySmsTypeWx = 2;
                            this.daySmsTypeSms = 1;
                        }else if(this.m.daySmsType==2){
                            this.daySmsTypeWx=2;
                            this.daySmsTypeSms=false;
                        }
						this.m.rebateTime = new Date(this.m.rebateTime).format("yyyy-MM-dd");
                    },
					methods : {
                        weChatPaySetting: function (name) {
                            var that = this;
                            var checked = $("#"+name).prop("checked");
                            if (checked){
                                that.setWeChatSetting(name,1);
                            }else{
                                that.setWeChatSetting(name,0);
                            }
                        },
                        posPaySetting: function (name) {
                            var that = this;
                            var checked = $("#"+name).prop("checked");
                            if (checked){
                                that.setPosPaySetting(name,1);
                            }else{
                                that.setPosPaySetting(name,0);
                            }
                        },
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
									debugger;
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
						initCouponTime: function(){
							$('.form_datetime').datetimepicker({
								format: "yyyy-mm-dd",
								autoclose: true,
								todayBtn: true,
								todayHighlight: true,
								showMeridian: true,
								language: 'zh-CN',//中文，需要引用zh-CN.js包
								startView: 2,//月视图
								minView: 2//日期时间选择器所能够提供的最精确的时间选择视图
							});
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
						},
                        setWeChatSetting : function (name,value) {
                            switch (name){
                                case "aliPay":
                                    this.m.aliPay = value;
                                    break;
                                case "openUnionPay":
                                    this.m.openUnionPay = value;
                                    break;
                                case "openMoneyPay":
                                    this.m.openMoneyPay = value;
                                    break;
                                case "openShanhuiPay":
                                    this.m.openShanhuiPay = value;
                                    break;
                                case "integralPay":
                                    this.m.integralPay = value;
                                    break;
                            }
                        },
                        setPosPaySetting : function (name,value) {
                            switch (name){
                                case "openPosWeChatPay":
                                    this.m.openPosWeChatPay = value;
                                    break;
                                case "openPosAliPay":
                                    this.m.openPosAliPay = value;
                                    break;
                                case "openPosUnionPay":
                                    this.m.openPosUnionPay = value;
                                    break;
                                case "openPosMoneyPay":
                                    this.m.openPosMoneyPay = value;
                                    break;
                                case "openPosShanhuiPay":
                                    this.m.openPosShanhuiPay = value;
                                    break;
                                case "openPosIntegralPay":
                                    this.m.openPosIntegralPay = value;
                                    break;
                            }
                        }
					}
				});

			}());
		}

	})
</script>
