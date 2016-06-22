package com.resto.shop.web.dao;

import java.math.BigDecimal;

import org.apache.ibatis.annotations.Param;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.ChargeOrder;

public interface ChargeOrderMapper  extends GenericDao<ChargeOrder,String> {
    int deleteByPrimaryKey(String id);

    int insert(ChargeOrder record);

    int insertSelective(ChargeOrder record);

    ChargeOrder selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ChargeOrder record);

    int updateByPrimaryKey(ChargeOrder record);

	BigDecimal selectTotalBalance(String customerId);

	void updateBalance(@Param("id")String id, @Param("useCharge")BigDecimal useCharge, @Param("useReward")BigDecimal useReward);

	ChargeOrder selectFirstBalanceOrder(String customerId);

	void refundCharge(BigDecimal payValue, String id);

	void refundReward(BigDecimal payValue, String id);
    
}
