package com.resto.shop.web.constant;

/**
 * Created by carl on 2016/11/14.
 */
public class LogBaseState {
    public final static int INTO = 1; //进入微信
    public final static int REPLACE = 2; //更换店铺
    public final static int CHOICE_D = 3;  //选择单品
    public final static int CHOICE_T = 4;  //选择套餐
    public final static int CANEL_T = 5;  //取消套餐
    public static final int EMPTY = 6; //清空菜品
    public static final int BUY = 7;  //下单
    public static final int BUY_PAY = 8;  //下单买单
    public static final int BUY_SCAN = 9;  //下单已经下单
    public static final int BUY_SCAN_PAY = 10;  //下单买单
    public static final int PAY = 11;  //买单
    public static final int BUY_ADD = 12;  //加菜
    public static final int SCAN = 13;  //扫码
    public static final int PRINT = 14;  //打印
    public static final int CANCEL_ORDER = 15;  //取消订单
    public static final int FAIL = 16;  //下单失败
    public static final int APPRAISE = 17;  //评价订单
    public static final int SHARE = 18;  //分享订单
    public static final int PRINT_TICKET = 19;  //打印总单
}