package com.resto.shop.web.dao;

import java.util.List;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.ChargePayment;

public interface ChargePaymentMapper  extends GenericDao<ChargePayment,String> {
    int deleteByPrimaryKey(String id);

    int insert(ChargePayment record);

    int insertSelective(ChargePayment record);

    ChargePayment selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ChargePayment record);

    int updateByPrimaryKeyWithBLOBs(ChargePayment record);

    int updateByPrimaryKey(ChargePayment record);

	List<ChargePayment> selectPayList();
}
