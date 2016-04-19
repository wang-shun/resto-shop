package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.OrderItem;

public interface OrderItemService extends GenericService<OrderItem, String> {
	
	/**
	 * 根据订单ID查询订单项
	 * @param orderId
	 * @return
	 */
	public List<OrderItem> listByOrderId(String orderId);

	public void insertItems(List<OrderItem> orderItems);
}
