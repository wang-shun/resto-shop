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

                    <div class="form-group" v-show="b.posOpenTable == 1">
                        <label class="col-md-4 control-label" :class="{ formBox : m.posOpenTable == 1 && m.shopMode == 2}">开启pos点单：</label>
                        <div  class="col-md-6 radio-list">
                            <label class="radio-inline">
                                <input type="radio" name="posOpenTable"v-model="m.posOpenTable" value="1">启用
                            </label>
                            <label class="radio-inline">
                                <input type="radio" name="posOpenTable" v-model="m.posOpenTable" value="0">不启用
                            </label>
                        </div>
                    </div>

                    <div class="form-group" v-show="m.shopMode == 2 && b.posOpenTable == 1 && m.posOpenTable == 1">
                        <label class="col-md-4 control-label formBox">pos端支付项：</label>
                        <div  class="col-md-6 radio-list checkbox">
                            <label style="margin-left: 16px;" class="formBox">
                                <input type="checkbox" checked="checked" disabled="disabled">
                                &nbsp;&nbsp;微信支付
                            </label>
                            <label style="margin-left: 16px;" v-show="b.aliPay == 1" :class="{ formBox : m.openPosAliPay == 1}">
                                <input type="checkbox" id="openPosAliPay" @change="posPaySetting('openPosAliPay')" v-model="m.openPosAliPay" value="1">
                                <input type="hidden" name="openPosAliPay" v-model="m.openPosAliPay">
                                &nbsp;&nbsp;支付宝支付
                            </label>
                            <label style="margin-left: 16px;" v-show="b.openUnionPay == 1" :class="{ formBox : m.openPosUnionPay == 1}">
                                <input type="checkbox" id="openPosUnionPay" @change="posPaySetting('openPosUnionPay')" v-model="m.openPosUnionPay" value="1">
                                <input type="hidden" name="openPosUnionPay" v-model="m.openPosUnionPay">
                                &nbsp;&nbsp;银联支付
                            </label>
                            <label style="margin-left: 16px;" v-show="b.openMoneyPay == 1" :class="{ formBox : m.openPosMoneyPay == 1}">
                                <input type="checkbox" id="openPosMoneyPay" @change="posPaySetting('openPosMoneyPay')" v-model="m.openPosMoneyPay" value="1">
                                <input type="hidden" name="openPosMoneyPay" v-model="m.openPosMoneyPay">
                                &nbsp;&nbsp;现金支付
                            </label>
                            <label style="margin-left: 16px;" v-show="b.openShanhuiPay == 1" :class="{ formBox : m.openPosShanhuiPay == 1}">
                                <input type="checkbox" id="openPosShanhuiPay" @change="posPaySetting('openPosShanhuiPay')" v-model="m.openPosShanhuiPay" value="1">
                                <input type="hidden" name="openPosShanhuiPay" v-model="m.openPosShanhuiPay">
                                &nbsp;&nbsp;美团闪惠支付
                            </label>
                            <label style="margin-left: 16px;" v-show="b.integralPay == 1" :class="{ formBox : m.openPosIntegralPay == 1}">
                                <input type="checkbox" id="openPosIntegralPay" @change="posPaySetting('openPosIntegralPay')" v-model="m.openPosIntegralPay" value="1">
                                <input type="hidden" name="openPosIntegralPay" v-model="m.openPosIntegralPay">
                                &nbsp;&nbsp;会员支付
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

                    <div class="form-group" v-show="b.openPosCharge == 1">
                        <label class="col-md-4 control-label">开启pos账户充值：</label>
                        <div  class="col-md-6 radio-list">
                            <label class="radio-inline">
                                <input type="radio" name="openPosCharge"v-model="m.openPosCharge" value="1">启用
                            </label>
                            <label class="radio-inline">
                                <input type="radio" name="openPosCharge" v-model="m.openPosCharge" value="0">不启用
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
                            this.daySmsTypeWx=2
                            this.daySmsTypeSms=false;
                        }
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
