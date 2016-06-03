package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.ChargePayment;

public interface ChargePaymentService extends GenericService<ChargePayment, String> {
	
	List<ChargePayment> selectPayList();
    
}
