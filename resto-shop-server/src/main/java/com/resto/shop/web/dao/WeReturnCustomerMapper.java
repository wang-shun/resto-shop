package com.resto.shop.web.dao;

import com.resto.shop.web.model.WeReturnCustomer;

public interface WeReturnCustomerMapper {
    int deleteByPrimaryKey(Long id);

    int insert(WeReturnCustomer record);

    int insertSelective(WeReturnCustomer record);

    WeReturnCustomer selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(WeReturnCustomer record);

    int updateByPrimaryKey(WeReturnCustomer record);
}