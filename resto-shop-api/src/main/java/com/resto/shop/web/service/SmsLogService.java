package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.SmsLog;

public interface SmsLogService extends GenericService<SmsLog, Long> {

	String sendCode(String phone, String code, String brandId, String shopId);
    /**
     * 根据店铺ID查询短信记录
     * @param shopId
     * @return
     */
	List<SmsLog> selectListByShopId(String shopId);
	
	/**
	 * 根据店铺ID查询两天内的短信记录
	 * @param currentShopId
	 * @return
	 */
	List<SmsLog> selectListByShopIdAndDate(String currentShopId);
}
