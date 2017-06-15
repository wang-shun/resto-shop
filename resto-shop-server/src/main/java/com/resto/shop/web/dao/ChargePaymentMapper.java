package com.resto.shop.web.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.web.dto.RechargeLogDto;
import com.resto.shop.web.model.ChargePayment;

public interface ChargePaymentMapper  extends GenericDao<ChargePayment,String> {
    int deleteByPrimaryKey(String id);

    int insert(ChargePayment record);

    int insertSelective(ChargePayment record);

    ChargePayment selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ChargePayment record);

    int updateByPrimaryKeyWithBLOBs(ChargePayment record);

    int updateByPrimaryKey(ChargePayment record);

	List<ChargePayment> selectPayList(@Param("begin")Date begin,@Param("end")Date end);
	

	
}
