package com.resto.shop.web.dao;

import com.resto.shop.web.model.Kitchen;
import com.resto.brand.core.generic.GenericDao;

public interface KitchenMapper  extends GenericDao<Kitchen,Integer> {
    int deleteByPrimaryKey(Integer id);

    int insert(Kitchen record);

    int insertSelective(Kitchen record);

    Kitchen selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Kitchen record);

    int updateByPrimaryKey(Kitchen record);
}
