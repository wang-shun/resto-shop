package com.resto.shop.web.dao;

import com.resto.shop.web.model.OffLineOrder;
import com.resto.brand.core.generic.GenericDao;

public interface OffLineOrderMapper  extends GenericDao<OffLineOrder,String> {
    int deleteByPrimaryKey(String id);

    int insert(OffLineOrder record);

    int insertSelective(OffLineOrder record);

    OffLineOrder selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(OffLineOrder record);

    int updateByPrimaryKey(OffLineOrder record);
}
