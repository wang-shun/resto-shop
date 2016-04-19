package com.resto.shop.web.exception;

public class AppException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ErrorMsg msg=new ErrorMsg(0, "");
	public static final ErrorMsg CUSTOMER_NOT_EXISTS= new ErrorMsg(1,"用户不存在！");
	public static final ErrorMsg NOT_BIND_PHONE = new ErrorMsg(2,"未绑定手机");
	public static final ErrorMsg ORDER_ITEMS_EMPTY = new ErrorMsg(3,"没有订单项");
	
	public AppException(ErrorMsg msg) {
		this.msg=msg;
	}
	
	@Override
	public String getMessage() {
		return this.msg.toString();
	}
	
	
	static class ErrorMsg{
		private int code;
		private String msg;
		ErrorMsg(int code, String msg){
			this.code=code;
			this.msg=msg;
		}
		public int getCode() {
			return code;
		}
		public String getMsg() {
			return msg;
		}
		public void setCode(int code) {
			this.code = code;
		}
		public void setMsg(String msg) {
			this.msg = msg;
		}
		@Override
		public String toString() {
			return "{code:"+code+",msg:\""+msg+"\"}";
		}
	}
}
