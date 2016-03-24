package com.resto.shop.web.dao;

import com.resto.shop.web.model.OrderItem;
import com.resto.brand.core.generic.GenericDao;

public interface OrderItemMapper  extends GenericDao<OrderItem,String> {
    int deleteByPrimaryKey(String id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);
}
