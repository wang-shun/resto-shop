package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.ChargeOrder;

public interface ChargeOrderService extends GenericService<ChargeOrder, String> {
    
	void createChargeOrder(String settingId,String customerId);
}
