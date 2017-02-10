package com.resto.shop.web.constant;

public class PayMode {

	public static final int WEIXIN_PAY = 1;  //微信支付
	public static final int ACCOUNT_PAY =2;  //红包支付
	public static final int COUPON_PAY=3;    //优惠券支付
	public static final int MONEY_PAY = 4;	//其他方式支付
	public static final int BANK_CART_PAY=5; //银行卡支付
	public static final int CHARGE_PAY = 6; //充值金额支付
	public static final int REWARD_PAY = 7; //充值赠送的金额支付
    public static final int WAIT_MONEY = 8; //等位红包
    public static final int HUNGER_MONEY = 9; //饿了吗
    public static final int ALI_PAY = 10; //支付宝
    public static final int ARTICLE_BACK_PAY = 11; //菜品退款支付
    public static final int CRASH_PAY= 12; //现金支付
    public static final int APPRAISE_RED_PAY = 13; //评论红包支付
    public static final int SHARE_RED_PAY = 14; //分享返利红包支付
    public static final int REFUND_ARTICLE_RED_PAY = 15; //退菜红包支付

    public static String getPayModeName(int state){
	    switch (state) {
            case WEIXIN_PAY:
                return "微信支付";
            case ACCOUNT_PAY:
                return "红包支付";
            case COUPON_PAY:
                return "优惠券支付";
            case MONEY_PAY:
                return "其他方式支付";
            case BANK_CART_PAY:
                return "银行卡支付";
            case CHARGE_PAY:
            	return "充值账户支付";
            case REWARD_PAY:
            	return "充值赠送支付";
            case WAIT_MONEY:
                return "等位红包支付";
            case HUNGER_MONEY:
                return "饿了吗支付";
            case ALI_PAY:
                return "支付宝支付";
            case ARTICLE_BACK_PAY:
                return "退菜返还金额";
            case CRASH_PAY:
                return "现金支付";
            case APPRAISE_RED_PAY:
                return "评论红包支付";
            case SHARE_RED_PAY:
                return "分享返利红包支付";
            case REFUND_ARTICLE_RED_PAY:
                return "退菜红包支付";
            default:
                return "未知";
            }
	}

}
