package com.resto.shop.web.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.web.dto.ArticleSellDto;
import com.resto.shop.web.model.Order;

public interface OrderMapper  extends GenericDao<Order,String> {
    int deleteByPrimaryKey(String id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);
    
    /**
     * 根据当前 店铺ID 和 用户ID 分页查询 其订单列表
     * @param start
     * @param datalength
     * @param shopId
     * @param customerId
     * @return
     */
    List<Order> orderList(@Param("start") Integer start,@Param("datalength") Integer datalength,@Param("shopId") String shopId,@Param("customerId") String customerId,@Param("orderState") String[] orderState);
    
    /**
     * 根据订单查询 订单状态 和 生产状态
     * @param orderId
     * @return
     */
    Order selectOrderStatesById(@Param("orderId") String orderId);
    
    /**
     * 查询  用户的新订单,如果 订单ID有值。则查询订单的详情
     * @param beginDate		当天的开始时间
     * @param customerId	当前用户ID
     * @param shopId		当前店铺ID
     * @param orderId		订单ID
     * @return
     */
    Order findCustomerNewOrder(@Param("beginDate") Date beginDate,@Param("customerId") String customerId,@Param("shopId") String shopId,@Param("orderState") Integer[] orderState,@Param("orderId") String orderId);

    /**
     * 查询某个店铺某天，某个状态的订单
     * @param shopId
     * @param date
     * @param productionState
     * @return
     */
	List<Order> selectShopOrderByDateAndProductionStates(@Param("shopId")String shopId,@Param("date") Date date,@Param("proStatus") int[] proStatus);

	/**
	 * 查询某天的历史订单
	 * @param currentShopId
	 * @param dateBegin
	 * @param dateEnd
	 * @return
	 */
	List<Order> selectHistoryOrderList(@Param("shopId")String currentShopId, @Param("dateBegin")Date dateBegin, @Param("dateEnd")Date dateEnd);

	void clearPushOrder(String id, int notOrder);

	void setOrderNumber(String orderId, String tableNumber);
	
	/**
	 * 根据取餐码查询已支付的订单
	 * @param vercode
	 * @return
	 */
	List<Order> selectOrderByVercode(@Param("vercode")String vercode,@Param("shopId")String shopId);
	
	/**
	 * 根据桌号查询店铺已支付的订单
	 * @param tableNumber
	 * @return
	 */
	List<Order> selectOrderByTableNumber(@Param("tableNumber")String tableNumber,@Param("shopId")String shopId);

	List<Order> listOrderByStatus(@Param("shopId")String currentShopId,@Param("begin") Date begin, 
			@Param("end")Date end, @Param("p_status")int[] productionStatus, 
			@Param("o_state")int[] orderState);

	void updateParentAmount(String orderId, Double money);

	Double selectParentAmount(String orderId);

	void changeAllowContinue(String id, boolean b);

	Integer selectArticleCountById(String id);

	List<Order> selectByParentId(String parentOrderId);

	List<String> selectChildIdsByParentId(String id);

	String selectNewCustomerPackageId(String currentCustomerId, String currentShopId);

	List<Order> selectReadyList(String currentShopId);
	
	
	/**
	 * 根据时间 和指定 店铺ID 查询已完成的订单(orderSatus = 2,10,11,12)
	 * 的菜品销售总和。
	 * @param beginDate
	 * @param endDate
	 * @param shopId
	 * @return
	 */
	int selectArticleSumCountByData(@Param("beginDate")Date beginDate,@Param("endDate")Date endDate,@Param("shopId")String shopId);
	
	
	/**
	 * 根据时间 和 指定 店铺 查询 已完成的订单的 菜品销售详情
	 * @param beginDate
	 * @param endDate
	 * @param shopId
	 * @return
	 */
	public List<ArticleSellDto> selectShopArticleSellByDate(@Param("beginDate")Date beginDate,@Param("endDate")Date endDate,@Param("shopId")String shopId,@Param("sort")String sort);
	
	
	/**
	 * 根据时间 查询 当前品牌已完成的订单的 菜品销售详情
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	public List<ArticleSellDto> selectBrandArticleSellByDate(@Param("beginDate")Date beginDate,@Param("endDate")Date endDate,@Param("sort")String sort);
	
	/**
	 * 根据时间 查询当前品牌已完成订单的某个餐品分类的销售详情
	 * @param beginDate
	 * @param endDate
	 * @param articleFamilyId
	 * @return
	 */
	List<ArticleSellDto> selectBrandArticleSellByDateAndArticleFamilyId(@Param("beginDate")Date beginDate, @Param("endDate")Date endDate,@Param("articleFamilyId")String articleFamilyId,@Param("sort")String sort);


	List<ArticleSellDto> selectShopArticleSellByDateAndArticleFamilyId(@Param("beginDate")Date begin,@Param("endDate")Date end,@Param("shopId")String shopId,@Param("articleFamilyId")String articleFamilyId ,@Param("sort")String sort);


	/**
	 * 更新该餐品库存 （-1）（无规格）
	 * @param articleId 餐品id
	 * @return
     */
	Boolean updateArticleStock(@Param("articleId") String articleId,@Param("type") String type);

	/**
	 * 更新该餐品库存 （-1）（有规格）
	 * @param articleId 餐品id
	 * @return
	 */
	Boolean updateArticlePriceStock(@Param("articleId") String articleId,@Param("type") String type);

	/**
	 * 库存为0时设置沽清
	 * @param articleId
	 * @return
     */
	Boolean setEmpty(String articleId);
		
}
