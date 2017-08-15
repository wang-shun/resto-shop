package com.resto.shop.web.util;

import com.alibaba.fastjson.JSONObject;
import com.resto.brand.core.entity.Result;
import com.resto.brand.core.enums.BrandAccountNoticeType;
import com.resto.brand.core.util.SMSUtils;
import com.resto.brand.web.model.AccountNotice;
import com.resto.brand.web.model.AccountSetting;
import com.resto.brand.web.model.BrandAccount;

import java.util.List;

public class BrandAccountSendUtil {

	/**
	 * 需要判断是否发短信的三个地方
	 * 1.注册
	 * 2.发送短信(验证码+结店)
	 * 3.订单抽成
	 *
	 */


	/**
	 * 判断是否需要发短信 在每个扣费的方法中都会执行这个判断方法
	 * 目前强行设置 2级不能多设置
	 * @param brandAccount
	 * @param noticeList
	 */
	public static Result sendSms(BrandAccount brandAccount, List<AccountNotice> noticeList, String brandName,AccountSetting accountSetting) {

		Result r = new Result();
		r.setSuccess(false);
		r.setMessage("");



		//需要发短信的情况
		// 余额小于设置项 并且没发短信 就开始发短信
//		if((account.compareTo(little)<0||account.compareTo(middle)<0||account.compareTo(hign)<0)&&accountSetting.getType()==0){
//			JSONObject json = new JSONObject();
//			json.put("name",brandName);
//			json.put("price",brandAccount.getAccountBalance()+"");
//			JSONObject aliResult = SMSUtils.sendMessage(accountSetting.getTelephone(),json,SMSUtils.SIGN,SMSUtils.BRAND_ACCOUNT_SMS,null);
//			if(aliResult.getBoolean("success")){//如果发送短信成功
//				r.setSuccess(true);
//				if(account.compareTo(little)<0&&account.compareTo(middle)>0){
//					r.setMessage(brandName+"第一次账户余额欠费提醒");
//					r.setOpenId("one");
//				}else if(account.compareTo(middle)<0&&account.compareTo(hign)>0){
//					r.setOpenId("two");
//					r.setMessage(brandName+"第二次账户余额欠费提醒");
//				}else if(account.compareTo(hign)<0){
//					r.setOpenId("three");
//					r.setMessage(brandName+"第三次账户余额欠费提醒");
//				}
//			}
//		}
		if(accountSetting.getType()==0){//说明未发送
			if(!noticeList.isEmpty()){
			for(int i =0 ;i<noticeList.size();i++){
				if(brandAccount.getAccountBalance().compareTo(noticeList.get(i).getNoticePrice())<0){//如果账户余额小于设置则发短信
					JSONObject json = new JSONObject();
					json.put("brandName",brandName);
					json.put("price",brandAccount.getAccountBalance().toString());
					JSONObject aliResult = new JSONObject();
					if(noticeList.get(i).getType()== BrandAccountNoticeType.NOT_ENOUGH){
						aliResult = SMSUtils.sendMessage(accountSetting.getTelephone(),json,SMSUtils.SIGN,SMSUtils.ACCOUNT_NOT_ENOUGH,null);
					}else if(noticeList.get(i).getType()==BrandAccountNoticeType.ARREARS){
						aliResult = SMSUtils.sendMessage(accountSetting.getTelephone(),json,SMSUtils.SIGN,SMSUtils.ACCOUNT_NOT_USED,null);
					}
					if(aliResult.getBoolean("success")){
						r.setSuccess(true);
						break;
					}
					}

				}
			}

		}

		return  r;
	}

	public static void main(String[] args) {
		String str = "-150,-100,-50";
		String[] arr = str.split(",");
		for(int i=0;i<arr.length;i++){
			System.out.println(arr[i]);
		}
	}
}
