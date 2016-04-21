package com.resto.shop.web.dao;

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
    List<OrderItem> listByOrderId(@Param("orderId") String orderId);

	void insertBatch(List<OrderItem> orderItems);

	List<Map<String, Object>> selectOrderArticleList(String orderId);
	
	
}
