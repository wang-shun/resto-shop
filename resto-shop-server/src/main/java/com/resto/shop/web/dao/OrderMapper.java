package com.resto.shop.web.dao;

import com.resto.shop.web.model.Order;
import com.resto.brand.core.generic.GenericDao;

public interface OrderMapper  extends GenericDao<Order,String> {
    int deleteByPrimaryKey(String id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);
}
