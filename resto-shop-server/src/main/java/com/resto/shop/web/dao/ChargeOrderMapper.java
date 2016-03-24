package com.resto.shop.web.dao;

import com.resto.shop.web.model.ChargeOrder;
import com.resto.brand.core.generic.GenericDao;

public interface ChargeOrderMapper  extends GenericDao<ChargeOrder,String> {
    int deleteByPrimaryKey(String id);

    int insert(ChargeOrder record);

    int insertSelective(ChargeOrder record);

    ChargeOrder selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ChargeOrder record);

    int updateByPrimaryKey(ChargeOrder record);
}
