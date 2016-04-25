package com.resto.shop.web.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.resto.brand.core.entity.Result;
import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.exception.AppException;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderItem;
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

	public Order pushOrder(String orderId) throws AppException;
	
	public Order callNumber(String orderId);
	
	public List<Map<String,Object>> getPrintData(String order);
	
	public Order printSuccess(String orderId);

	/**
	 * 查询当天某些状态的订单
	 * @param shopId
	 * @return
	 */
	public List<Order> selectTodayOrder(String shopId, int[] is);

	public List<Order> selectReadyOrder(String currentShopId,Long lastTime);

	public List<Order> selectPushOrder(String currentShopId,Long lastTime);

	public List<Order> selectCallOrder(String currentBrandId,Long lastTime);

	public Map<String, Object> printReceipt(String orderId);

	/**
	 * 打印厨房的小票
	 * @param order			订单信息
	 * @param articleList	订单菜品集合
	 * @return
	 */
	public List<Map<String,Object>> printKitchen(Order order, List<OrderItem> articleList);

	 
	public Order confirmOrder(Order order);

	public Order getOrderInfo(String orderId);

	public List<Order> selectHistoryOrderList(String currentShopId, Date date);

	public boolean cancelOrderPos(String orderId);

	public void changePushOrder(Order order);

	public List<Map<String, Object>> printOrderAll(String orderId);

	public void setTableNumber(String orderId, String tableNumber);

}
