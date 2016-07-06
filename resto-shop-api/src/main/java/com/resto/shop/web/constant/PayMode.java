package com.resto.shop.web.constant;

public class PayMode {

	public static final int WEIXIN_PAY = 1;  //微信支付
	public static final int ACCOUNT_PAY =2;  //红包支付
	public static final int COUPON_PAY=3;    //优惠券支付
	public static final int MONEY_PAY = 4;	//现金支付
	public static final int BANK_CART_PAY=5; //银行卡支付
	public static final int CHARGE_PAY = 6; //充值金额支付
	public static final int REWARD_PAY = 7; //充值赠送的金额支付
	
	public static String getPayModeName(int state){
	    switch (state) {
            case WEIXIN_PAY:
                return "微信支付";
            case ACCOUNT_PAY:
                return "红包支付";
            case COUPON_PAY:
                return "优惠券支付";
            case MONEY_PAY:
                return "现金支付";
            case BANK_CART_PAY:
                return "银行卡支付";
            case CHARGE_PAY:
            	return "充值账户支付";
            case REWARD_PAY:
            	return "充值赠送支付";
            default:
                return "未知";
            }
	}

}
