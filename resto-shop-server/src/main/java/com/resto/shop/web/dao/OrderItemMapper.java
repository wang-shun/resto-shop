package com.resto.shop.web.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.OrderItem;

public interface OrderItemMapper  extends GenericDao<OrderItem,String> {
    int deleteByPrimaryKey(String id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);
    
    //根据订单ID查询订单项
    List<OrderItem> listByOrderId(Map<String, String> param);

    //套餐
    List<OrderItem> listTotalByOrderId(@Param("orderId") String orderId);


    List<OrderItem> listByParentId(String orderId);

	void insertBatch(List<OrderItem> orderItems);

	
	/**
	 * 根据时间查询 当前店铺的 菜品销售记录
	 * @param beginDate
	 * @param endDate
	 * @param shopId
	 * @return
	 */
	public List<OrderItem> selectSaleArticleByDate(@Param("beginDate")Date beginDate,@Param("endDate")Date endDate,@Param("shopId")String shopId,@Param("sort")String sort);

	List<OrderItem> listByOrderIds(List<String> childIds);

    List<OrderItem> getListBySort(@Param("parentid") String parentid,@Param("articleid") String articleid);



    
    
    /**
     * 查询订单详情   【用于同步 中间数据库 操作】
     * @param beginDate
     * @param endDate
     * @return
     */
    List<Map<String, Object>> selectOrderItems(@Param("beginDate")Date beginDate,@Param("endDate")Date endDate);

    List<OrderItem> getOrderItemByRecommendId(@Param("recommendId")String recommendId,@Param("orderId")String orderId);

    void refundArticle(@Param("id") String id,@Param("count") Integer count);

    List<OrderItem> selectOrderItemByOrderIds(Map<String, Object> map);

    List<OrderItem> selectOrderItemByOrderId(Map<String, Object> map);
    
    List<OrderItem> selectRefundOrderItem(Map<String, Object> map);

    void refundArticleChild(String parentId);

    /**
     * 得到套餐下的子品
     */
    List<OrderItem> getListByParentId(String parentId);

    List<OrderItem> getListByRecommendId(@Param("recommendId") String recommendId,@Param("orderId") String orderId);

    List<OrderItem> selectRefundArticleItem(@Param("orderId") String orderId);

    List<OrderItem> selectByArticleIds(String[] articleIds);

    /**
     * 根据 订单ID 删除
     * Pos 2.0 同步数据使用
     * @param orderId
     */
    void posSyncDeleteByOrderId(String orderId);

    List<OrderItem> getOrderBefore(@Param("tableNumber") String tableNumber,
                                   @Param("shopId") String shopId,@Param("customerId") String customerId);

    List<OrderItem> posSyncListByOrderId(@Param("orderId") String orderId);
}
