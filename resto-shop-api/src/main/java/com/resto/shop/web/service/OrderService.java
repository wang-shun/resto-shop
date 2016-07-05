package com.resto.shop.web.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.resto.brand.core.generic.GenericService;
import com.resto.brand.web.dto.ArticleSellDto;
import com.resto.brand.web.dto.SaleReportDto;
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
	
	public Order printSuccess(String orderId) throws AppException;

	/**
	 * 查询当天某些状态的订单
	 * @param shopId
	 * @return
	 */
	public List<Order> selectTodayOrder(String shopId, int[] is);

	public List<Order> selectReadyOrder(String currentShopId);

	public List<Order> selectPushOrder(String currentShopId,Long lastTime);

	public List<Order> selectCallOrder(String currentBrandId,Long lastTime);

	public Map<String, Object> printReceipt(String orderId,Integer selectPrinterId);

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

	public Order cancelOrderPos(String orderId) throws AppException;

	public void changePushOrder(Order order);

	public List<Map<String, Object>> printOrderAll(String orderId);

	public void setTableNumber(String orderId, String tableNumber);
	
	/**
	 * 根据取餐码查询店铺中已支付的订单
	 * @param vercode
	 * @return
	 */
	public List<Order> selectOrderByVercode(String vercode,String shopId);
	
	/**
	 * 根据桌号查询店铺中已支付的订单
	 * @param tableNumber
	 * @return
	 */
	public List<Order> selectOrderByTableNumber(String tableNumber,String shopId);
	
	/**
	 * 修改就餐模式
	 * @param modeId
	 * @param currentShopId
	 */
	public void updateDistributionMode(Integer modeId, String orderId);

	/**
	 * 清除所有订单状态信息
	 * @param currentShopId
	 */
	public void clearNumber(String currentShopId);

	public List<Order> listOrderByStatus(String currentShopId, Date begin, Date end, int[] productionStatus,
			int[] orderState);

	public void updateAllowContinue(String id, boolean b);

	List<Order> selectByParentId(String parentOrderId);

	public Order findCustomerNewPackage(String currentCustomerId, String currentShopId);
	
	/**
	 * 根据时间 和指定 店铺ID 查询已完成的订单(orderSatus = 2,10,11,12)
	 * @param beginDate
	 * @param endDate
	 * @param shopId
	 * @return
	 */
	SaleReportDto selectArticleSumCountByData(String beginDate,String endDate,String brandId);
	
	/**
	 * 根据时间 和 指定 店铺 查询 已完成的订单的 菜品销售详情
	 * @param beginDate
	 * @param endDate
	 * @param shopId
	 * @return
	 */
	public List<ArticleSellDto> selectShopArticleSellByDate(String beginDate,String endDate,String shopId,String sort);
	
	/**
	 * 根据时间 查询 当前品牌已完成的订单的 菜品销售详情
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	public List<ArticleSellDto> selectBrandArticleSellByDate(String beginDate,String endDate,String order);

	public List<ArticleSellDto> selectBrandArticleSellByDateAndArticleFamilyId(String beginDate, String endDate,
			String articleFamilyId,String sort);
	/**
	 * 根据时间,指定店铺，指定菜品分类，查询已完成订单的销售详情
	 * @param beginDate
	 * @param endDate
	 * @param shopId
	 * @param articleFamilyId
	 * @return
	 */
	public List<ArticleSellDto> selectShopArticleSellByDateAndArticleFamilyId(String beginDate, String endDate,
			String shopId, String articleFamilyId,String sort);
}
