package com.resto.shop.web.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.model.SmsLog;

public interface SmsLogService extends GenericService<SmsLog, Long> {

	JSONObject sendCode(String phone, String code, String brandId, String shopId, int smsLogType, Map<String,String> logMap);

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
}
