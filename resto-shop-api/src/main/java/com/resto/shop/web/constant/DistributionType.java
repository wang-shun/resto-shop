package com.resto.shop.web.constant;

public class DistributionType {
	public final static int RESTAURANT_MODE_ID = 1;  //堂吃
	public final static int DELIVERY_MODE_ID = 2;    //自提外卖
	public static final int TAKE_IT_SELF = 3;		//外带
	public static final int REFUND_ORDER = 4;		//退菜
	
	
	public static String getModeText(int mid){
		switch (mid) {
		case RESTAURANT_MODE_ID:
			return "堂吃";
		case DELIVERY_MODE_ID:
			return "自提外卖";
		case TAKE_IT_SELF:
			return "外带";
		case REFUND_ORDER:
			return "退菜";
		default:
			return "未知";
		}
	}
}
