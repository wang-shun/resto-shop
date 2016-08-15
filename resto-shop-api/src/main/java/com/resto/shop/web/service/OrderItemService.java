package com.resto.shop.web.service;

import java.util.List;
import java.util.Map;

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

	/**
	 * 根据时间查询 当前店铺的 菜品销售记录
	 * @param beginDate
	 * @param endDate
	 * @param shopId
	 * @return
	 */
	public List<OrderItem> selectSaleArticleByDate( String shopId,String beginDate, String endDate,String sort);

	public List<OrderItem> listByOrderIds(List<String> childIds);
	
	
	/**
	 * 用于统计 订单菜品 等信息，用于中间数据库同步时使用
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	public List<Map<String, Object>> selectOrderItems(String beginDate,String endDate);
}
