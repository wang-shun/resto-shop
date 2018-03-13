package com.resto.shop.web.service;

import java.util.List;
import java.util.Map;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.OrderItem;

public interface OrderItemService extends GenericService<OrderItem, String> {
	
	/**
	 * 根据订单ID查询订单项
	 * @param param
	 * @return
	 */
	public List<OrderItem> listByOrderId(Map<String, String> param);

	public List<OrderItem> listByOrderIdPos(Map<String, String> param);

	public List<OrderItem> listByParentId(String orderId);

	/**
	 * 更新预点餐的orderItmer到新的订单项内
	 * @param orderId
	 * @param beforeId
     */
	public void updateOrderIdByBeforeId(String orderId, String beforeId);

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
	
	public List<OrderItem> selectOrderItemByOrderIds(Map<String, Object> map);

	public List<OrderItem> selectOrderItemByOrderId(Map<String, Object> map);
	
	public List<OrderItem> selectRefundOrderItem(Map<String, Object> map);

	List<OrderItem> getListByParentId(String parentId);

	List<OrderItem> getListByRecommendId(String recommendId,String orderId);

	List<OrderItem> selectRefundArticleItem(String orderId);

	List<OrderItem> selectByArticleIds(String[] articleIds);

	/**
	 * 根据 订单ID 删除
	 * Pos 2.0 同步数据使用
	 * @param orderId
	 */
	void posSyncDeleteByOrderId(String orderId);

	List<OrderItem> getOrderBefore(String tableNumber, String shopId, String customerId);

	/**
	 * Pos 2.0 数据同步方法，根据 orderId 查询订单项
	 * @param orderId
	 * @return
	 */
	List<OrderItem> posSyncListByOrderId(String orderId);
}
