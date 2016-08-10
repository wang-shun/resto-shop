package com.resto.shop.web.constant;

/**
 * 短信通知的类型
 * @author Administrator
 *
 */
public class SmsLogType {
	
	public static final int AUTO_CODE = 1;  //校验码
	
	public static String getSmsLogTypeName(int state){
	    switch (state) {
            case AUTO_CODE:
                return "验证码";
            default:
                return "未知";
            }
	    
	    
	}
}
