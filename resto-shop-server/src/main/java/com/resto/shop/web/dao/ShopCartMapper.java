package com.resto.shop.web.dao;

import com.resto.shop.web.model.ShopCart;
import com.resto.brand.core.generic.GenericDao;

public interface ShopCartMapper  extends GenericDao<ShopCart,Integer> {
    int deleteByPrimaryKey(Integer id);

    int insert(ShopCart record);

    int insertSelective(ShopCart record);

    ShopCart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ShopCart record);

    int updateByPrimaryKey(ShopCart record);
}
