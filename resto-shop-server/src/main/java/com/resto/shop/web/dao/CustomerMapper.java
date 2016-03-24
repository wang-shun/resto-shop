package com.resto.shop.web.dao;

import com.resto.shop.web.model.Customer;
import com.resto.brand.core.generic.GenericDao;

public interface CustomerMapper  extends GenericDao<Customer,String> {
    int deleteByPrimaryKey(String id);

    int insert(Customer record);

    int insertSelective(Customer record);

    Customer selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Customer record);

    int updateByPrimaryKey(Customer record);
}
