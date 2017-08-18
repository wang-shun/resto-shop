package com.resto.shop.web.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.resto.brand.core.generic.GenericService;
import com.resto.brand.web.model.AccountSetting;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.SmsLog;

public interface SmsLogService extends GenericService<SmsLog, Long> {

	/**
	 * 发送注册验证码
	 * 这个里面只做法短信验证码功能 把发短信之前做什么 和之后做什么功能抽出来做成 切面
 	 * @param phone
	 * @param code
	 * @param brandId
	 * @param shopId
	 * @param smsLogType
	 * @return
	 */
	JSONObject sendCode(String phone, String code, String brandId, String shopId, int smsLogType);

    /**
     * 根据店铺ID查询短信记录
     * @param shopId
     * @return
     */
	List<SmsLog> selectListByShopId(String shopId);

    /**
     * 根据店铺ID查询两天内的短信记录
     * @param shopId
     * @return
     */
	List<SmsLog> selectListByShopIdAndDate(String shopId);

    /**
     * 根据时间和店铺查询记录
     * @param begin
     * @param end
     * @param shopIds
     * @return
     */
	List<SmsLog> selectListWhere(String begin, String end,String shopIds);

	/**
	 * 根据品牌
	 * 查询全部短信记录
	 * @return
	 */
	List<SmsLog> selecByBrandId(String brandId);


    SmsLog selectByMap(Map<String, Object> selectMap);

	JSONObject sendMessage(String telephone, JSONObject sms, String sign, String code_temp);
}
