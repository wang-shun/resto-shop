package com.resto.shop.web.service;

import java.util.List;
import java.util.Map;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderPaymentItem;

public interface OrderService extends GenericService<Order, String> {
    
	/**
     * 根据当前 店铺ID 和 用户ID 分页查询 其订单列表
     * @param start
     * @param datalength
     * @param shopId
     * @param customerId
     * @return
     */
	public List<Order> listOrder(Integer start, Integer datalength, String shopId, String customerId,String ORDER_STATE);
	
	/**
	 * 根据订单ID查询订单状态和生产状态
	 * @param orderId
	 * @return
	 */
	public Order selectOrderStatesById(String orderId);

	public Order createOrder(Order order)throws AppException;

	public Order findCustomerNewOrder(String customerId,String shopId,String orderId);

	public boolean cancelOrder(String string);

	public Order orderWxPaySuccess(OrderPaymentItem item);

	public Order pushOrder(String orderId);
	
	public Order callNumber(String orderId);
	
	public List<Map<String,Object>> getPrintData(String order);
	
	public Order printSuccess(String orderId);
	
	 

}
