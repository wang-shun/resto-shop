package com.resto.shop.web.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.resto.brand.core.generic.GenericDao;
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
    List<Order> orderList(@Param("start") Integer start,@Param("datalength") Integer datalength,@Param("shopId") String shopId,@Param("customerId") String customerId);
    
    /**
     * 根据订单查询 订单状态 和 生产状态
     * @param orderId
     * @return
     */
    Map<String, Integer> selectOrderStatesById(@Param("orderId") String orderId);
}
