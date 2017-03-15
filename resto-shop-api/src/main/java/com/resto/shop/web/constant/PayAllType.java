package com.resto.shop.web.constant;

/**
 * Created by carl on 2017/3/15.
 */
public class PayAllType {

    //判断订单及其自订单是否存在未付款的订单

    /**
     * 存在未付款
     */
    public static final int NOT_SUBMIT = 0;
    /**
     * 存在使用微信支付宝现金银联支付  正在支付中  待买单或者待确认的订单
     */
    public static final int SUBMIT = 1;
    /**
     * 所有订单全部支付
     */
    public static final int PAYMENT= 2;

}
