package com.resto.shop.web.constant;

public class ProductionStatus {
	/**
	 * 已付款未下单
	 */
	public static final int NOT_ORDER = 0;

	/**
	 * 已支付，并且已下单
	 */
	public final static int HAS_ORDER=1;
	
	/**
	 * 已打印
	 */
	public final static int PRINTED=2; 
	
	/**
	 * 已叫号
	 */
	public final static int HAS_CALL=3;
	
	/**
	 * 已取餐
	 */
	public final static int GET_IT=4;

	/**
	 * 已下单未打印
	 */
	public final static int NOT_PRINT = 5;
	
}
