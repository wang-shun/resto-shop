package com.resto.shop.web.dao;

import com.resto.shop.web.model.ChargePayment;
import com.resto.brand.core.generic.GenericDao;

public interface ChargePaymentMapper  extends GenericDao<ChargePayment,String> {
    int deleteByPrimaryKey(String id);

    int insert(ChargePayment record);

    int insertSelective(ChargePayment record);

    ChargePayment selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ChargePayment record);

    int updateByPrimaryKeyWithBLOBs(ChargePayment record);

    int updateByPrimaryKey(ChargePayment record);
}
