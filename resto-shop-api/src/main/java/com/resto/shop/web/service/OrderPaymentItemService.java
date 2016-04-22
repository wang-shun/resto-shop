package com.resto.shop.web.service;

import java.util.Date;
import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.OrderItem;
import com.resto.shop.web.model.OrderPaymentItem;

public interface OrderPaymentItemService extends GenericService<OrderPaymentItem, String> {

	List<OrderPaymentItem> selectByOrderId(String orderId);

	OrderPaymentItem selectpaymentByPaymentMode(Date beginDate, Date endDate, String currentShopId);

    
}
