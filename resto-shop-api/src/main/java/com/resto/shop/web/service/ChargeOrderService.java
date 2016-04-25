package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.ChargeOrder;

public interface ChargeOrderService extends GenericService<ChargeOrder, String> {
	
	/**
	 * 创建微信充值订单
	 * @param settingId
	 * @param customerId
	 * @return
	 */
	ChargeOrder createChargeOrder(String settingId,String customerId,String shopId,String brandId);
}
