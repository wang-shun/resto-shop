package com.resto.shop.web.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.web.dto.ArticleSellDto;
import com.resto.brand.web.dto.OrderArticleDto;
import com.resto.brand.web.dto.OrderPayDto;
import com.resto.brand.web.dto.ShopArticleReportDto;
import com.resto.shop.web.model.Order;
import com.resto.shop.web.model.OrderItem;
import com.resto.shop.web.model.OrderPaymentItem;

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
     * @param proStatus
     * @return
     */

	List<Order> selectShopOrderByDateAndProductionStates(@Param("shopId")String shopId,@Param("date") Date date,@Param("proStatus") int[] proStatus);


	/**
	 *  根据店铺id得到店铺当日的订单情况
	 * @param shopId
	 * @return
     */
	Order getOrderAccount(String shopId);


	/**
	 * 查询某天的历史订单
	 * 根据店铺模式查询不同状态的历史订单。（2016-10-11，已取消此功能，改为所有模式的店铺的历史订单都不为取消状态和未支付状态）
	 * @param currentShopId
	 * @param dateBegin
	 * @param dateEnd
	 * @param shopMode
     * @return
	 */
	List<Order> selectHistoryOrderList(@Param("shopId") String currentShopId, @Param("dateBegin") Date dateBegin, @Param("dateEnd") Date dateEnd, @Param("shopMode") Integer shopMode);

	List<Order>listHoufuFinishedOrder(String shopId);

	List<Order>listHoufuUnFinishedOrder(String shopId);

	/**
	 * 查询某天的异常订单
	 * @param currentShopId
	 * @param dateBegin
	 * @param dateEnd
	 * @return
	 */
	List<Order> selectErrorOrderList(@Param("shopId")String currentShopId, @Param("dateBegin")Date dateBegin, @Param("dateEnd")Date dateEnd);


	/**
	 * 查询未付款的订单（后付模式）
	 * @param currentShopId
	 * @param dateBegin
	 * @param dateEnd
     * @return
     */
	List<Order> getOrderNoPayList(@Param("shopId")String currentShopId, @Param("dateBegin")Date dateBegin, @Param("dateEnd")Date dateEnd);


	void clearPushOrder(String id, int notOrder);

	void setOrderNumber(String orderId, String tableNumber);
	
	/**
	 * 根据取餐码查询已支付的订单
	 * @param vercode
	 * @return
	 */
	List<Order> selectOrderByVercode(@Param("vercode")String vercode,@Param("shopId")String shopId);

	/**
	 * 查询已消费订单的订单数目和订单总额
	 * @param begin
	 * @param end
	 * @param brandId
	 * @return
	 */
	OrderPayDto selectBytimeAndState(@Param("beginDate")Date begin, @Param("endDate")Date end, @Param("brandId")String brandId);


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

	List<String> selectChildIdsByParentIdByFive(String id);

	String selectNewCustomerPackageId(String currentCustomerId, String currentShopId);

	List<Order> selectReadyList(String currentShopId);
	
	
	/**
	 * 根据时间 和指定 店铺ID 查询已完成的订单(orderSatus = 2,10,11,12)
	 * 的菜品销售总和。
	 * @param beginDate
	 * @param endDate
	 * @param brandId
	 * @return
	 */
    Integer selectArticleSumCountByData(@Param("beginDate")Date beginDate, @Param("endDate")Date endDate, @Param("brandId")String brandId);
	
	
	/**
	 * 根据时间 和 指定 店铺 查询 已完成的订单的 菜品销售详情
	 * @param beginDate
	 * @param endDate
	 * @param shopId
	 * @return
	 */
	public List<ArticleSellDto> selectShopArticleSellByDate(@Param("beginDate")Date beginDate,@Param("endDate")Date endDate,@Param("shopId")String shopId, @Param("sort")String sort);
	
	
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
	 * 根据时间查询店铺已完成订单的菜品销售详情
	 * @param shopId
	 * @param begin
	 * @param end
	 * @param sort
	 * @return
	 */
	List<ArticleSellDto> selectShopArticleByDate(@Param("shopId")String shopId,@Param("beginDate") Date begin,@Param("endDate") Date end,@Param("sort") String sort);

	/**
	 * 根据时间  菜品分类id  查询店铺已完成订单的菜品销售详情
	 * @param begin
	 * @param end
	 * @param shopId
	 * @param articleFamilyId
	 * @param sort
	 * @return
	 */
	List<ArticleSellDto> selectShopArticleByDateAndArticleFamilyId(@Param("beginDate")Date begin, @Param("endDate")Date end, @Param("shopId")String shopId,
			@Param("articleFamilyId")String articleFamilyId,@Param("sort") String sort);

	/**
	 * 查询品牌下所有店铺的菜品销售情况
	 * @param beginDate
	 * @param endDate
	 * @param brandId
	 * @return
	 */
	List<ShopArticleReportDto> selectShopArticleDetails(@Param("beginDate")Date beginDate,@Param("endDate") Date endDate, @Param("brandId")String brandId);

	List<ShopArticleReportDto> selectShopArticleCom(@Param("beginDate")Date beginDate,@Param("endDate") Date endDate, @Param("brandId")String brandId);

    /**
     * 查询店铺的菜品销售总量
     * @param brandId
     * @param begin
     * @param end
     * @param sort
     * @return
     */
//	int selectShopArticleNum(@Param("beginDate")Date beginDate,@Param("endDate") Date endDate, @Param("shopId")String shopId);

	List<ArticleSellDto> selectBrandArticleSellByDateAndFamilyId(@Param("brandId")String brandId,@Param("beginDate") Date begin,@Param("endDate") Date end,@Param("sort") String sort);

	List<ArticleSellDto> selectBrandArticleSellByDateAndId(@Param("brandId")String brandId,@Param("beginDate") Date begin, @Param("endDate")Date end,@Param("sort") String sort);

	List<ArticleSellDto> selectBrandFamilyArticleSellByDateAndArticleFamilyId(@Param("brandId")String brandId,@Param("articleFamilyName")String articleFamilyName, @Param("beginDate")Date begin,
			 @Param("endDate")Date end, @Param("sort") String sort);

	List<Order> selectMoneyAndNumByDate(@Param("beginDate")Date begin, @Param("endDate")Date end, @Param("brandId")String brandId);

	List<ArticleSellDto> selectShopArticleSellByDateAndFamilyId(@Param("shopId")String shopId,@Param("beginDate") Date begin,@Param("endDate") Date end, @Param("sort")String sort);

	List<ArticleSellDto> selectShopArticleSellByDateAndId(@Param("shopId")String shopId,@Param("beginDate") Date begin, @Param("endDate")Date end,@Param("sort") String sort);

	List<Order> selectListByTime(@Param("beginDate")Date begin, @Param("endDate")Date end,@Param("shopId") String shopId);

	Order selectOrderDetails(String orderId);


	int setOrderPrintFail(String orderId);

	List<ArticleSellDto> selectArticleFamilyByBrandAndFamilyName(@Param("brandId")String brandId,@Param("beginDate") Date begin, @Param("endDate")Date end,
			@Param("articleFamilyName")String articleFamilyName);
	/**
	 * 查询已消费的订单
	 * @param begin
	 * @param end
	 * @param brandId
	 * @return
	 */
	List<Order> selectListBybrandId(@Param("beginDate")Date begin,@Param("endDate") Date end,@Param("brandId") String brandId);

	List<Order> selectAppraiseByShopId(@Param("beginDate")Date beginDate, @Param("endDate")Date endDate, @Param("shopId")String shopId);

	/**
	 * 根据菜品id查看菜品库存
	 * @param articleId
	 * @return
     */
	Integer selectArticleCount(String articleId);

	/**
	 * 查看有规格的菜品库存
	 * @param articleId
	 * @return
     */
	Integer selectArticlePriceCount(String articleId);



	/**
	 * 更新该餐品库存 （-1）（无规格）
	 * @param articleId 餐品id
	 * @return
	 */
	Boolean updateArticleStock(@Param("articleId") String articleId,@Param("type") String type,@Param("count") Integer count);

	/**
	 * 更新该餐品库存 （-1）（有规格）
	 * @param articleId 餐品id
	 * @return
	 */
	Boolean updateArticlePriceStock(@Param("articleId") String articleId,@Param("type") String type,@Param("count") Integer count);

	/**
	 * 库存为0时设置沽清	---	tb_article
	 * @param articleId
	 * @return
	 */
	Boolean setEmpty(String articleId);

	
	/**
	 * 库存为0时设置沽清	---	tb_article_price
	 * @param articleId
	 * @return
	 */
	Boolean setArticlePriceEmpty(String articleId);
	
	
	/**
	 * 还原库存时重置售罄状态	---	tb_article	
	 * @param articleId
	 * @return
     */
	Boolean setEmptyFail(String articleId);
	
	/**
	 * 还原库存时重置售罄状态	---	tb_article_price	
	 * @param articleId
	 * @return
     */
	Boolean setArticlePriceEmptyFail(String articleId);

	/**
	 * 将单品最低库存设置为 套餐库存
	 * @return
	 */
	Boolean setStockBySuit(@Param("shopId")String shopId);

	BigDecimal getPayment(@Param("type") Integer type,@Param("shopId") String shopId);

    /**
     *
     * @param shopId
     * @param familyId
     * @return
     */
	Integer getArticleCount(@Param("shopId") String shopId,@Param("familyId") String familyId);

    List<OrderArticleDto>  selectOrderArticle(@Param("brandId") String brandId,@Param("beginTime") Date beginTime,@Param("endTime") Date endTime);

    /**
     * 查询品牌菜品的数据 用于中间数据库
     * @param brandId
     * @param begin
     * @param end
     * @return
     */
    List<Map<String,Object>> selectBrandArticleSellList(@Param("brandId") String brandId,@Param("beginDate") Date begin,@Param("endDate") Date end);

    /**
     * 查询订单详情数据 用于中间数据库
     * @param begin
     * @param end
     * @param brandId
     * @return
     */
    List<Order> selectListByTimeAndBrandId(@Param("brandId") String brandId,@Param("beginDate") Date begin, @Param("endDate") Date end);

    /**
     * 查询店铺菜品数据 用于中间数据库
     * @param shopId
     * @param begin
     * @param end
     * @return
     */
    List<Map<String,Object>> selectShopArticleSellList(@Param("shopId") String shopId, @Param("beginDate") Date begin, @Param("endDate") Date end);


    /**
     * 查询品牌菜品的总销量
     * @param begin
     * @param end
     * @param brandId
     * @return
     */
    int selectBrandArticleSum(@Param("beginDate") Date begin,@Param("endDate") Date end,@Param("brandId") String brandId);

    /**
     * 查询品牌菜品的总价值
     * @param begin
     * @param end
     * @param brandId
     * @return
     */

    BigDecimal selectBrandArticleSell(@Param("beginDate") Date begin,@Param("endDate") Date end,@Param("brandId") String brandId);

    /**
     * 查询品牌下每个店铺的菜品销量和
     * @param begin
     * @param end
     * @param brandId
     * @return
     */
    List<ShopArticleReportDto> selectShopArticleSum(@Param("beginDate") Date begin,@Param("endDate") Date end,@Param("brandId") String brandId);

    /**
     * 查询品牌下每个店铺的菜品销售和
     * @param begin
     * @param end
     * @param brandId
     * @return
     */
    List<ShopArticleReportDto> selectShopArticleSell(@Param("beginDate") Date begin,@Param("endDate") Date end,@Param("brandId") String brandId);

    /**
     * 查询店铺下所有的已消费的订单
     * @param begin
     * @param end
     * @param shopId
     * @return
     */

    List<Order> selectListByShopId(@Param("beginDate") Date begin, @Param("endDate") Date end,@Param("shopId") String shopId);

    /**
     * 根据订单状态和生产状态查询指定店铺的订单
     * @param shopId
     * @param orderStates
     * @param productionStates
     * @return
     */
    List<Order> selectByOrderSatesAndProductionStates(@Param("shopId")String shopId,@Param("orderStates")String[] orderStates,@Param("productionStates")String[] productionStates);

    /**
     * 查询所有不正常的订单(并非异常订单)
     * 原因：所有支付的订单(2,10,11,12) 都会去打印，如果打印机有问题或连结不上最终也只会到异常订单里面 不会出现 0,1的情况
     * 所以出现已支付但是生产状态是0,1的都是不正常的订单
     * @param begin
     * @param end
     * @param brandId
     * @return
     */
    List<Order> selectExceptionOrderListBybrandId(@Param("beginDate") Date begin,@Param("endDate") Date end, @Param("brandId") String brandId);

    /**
     * @param begin
     * @param end
     * @param brandId
     * @return
     */
    List<Order> selectHasPayListOrderByBrandId(@Param("beginDate") Date begin,@Param("endDate") Date end,@Param("brandId") String brandId);

    List<Order> selectHasPayOrderPayMentItemListBybrandId(@Param("beginDate") Date begin, @Param("endDate") Date end, @Param("brandId") String brandId);


    int  selectBrandArticleNum(@Param("beginDate") Date begin, @Param("endDate") Date end, @Param("brandId") String brandId);

    BigDecimal  selectConfirmMoney(@Param("beginDate") Date begin, @Param("endDate") Date end, @Param("brandId") String brandId);

    /**
     * 手动取消订单
     * @param brandId
     * @param begin
     * @param end
     * @return
     */
    List<Order> selectNeedCacelOrderList(@Param("brandId") String brandId, @Param("begin") Date begin, @Param("end") Date end);
	/**
	 * 获取所有桌号加菜列表
	 * @param shopId
	 * @return
     */
	List<Order> getTableNumberAll(@Param("shopId") String shopId);

	Order getOrderDetail(String orderId);

	List<OrderPaymentItem> selectOrderPaymentItems(String orderId);

	List<OrderItem> selectOrderItems(String orderId);

	List<Order> getOrderByEmployee(@Param("shopId") String shopId,@Param("employeeId") String employeeId);


	Order getLastOrderByCustomer(@Param("customerId")String customerId,@Param("shopId") String shopId,@Param("time") Integer time);

	Order getLastOrderByTableNumber(String tableNumber);

	BigDecimal getServicePrice(String shopId);

	void refundServicePrice(@Param("id") String id,@Param("servicePrice") BigDecimal servicePrice,@Param("customerCount") Integer customerCount);

	Integer getMealALLNumber(String shopId);
}
