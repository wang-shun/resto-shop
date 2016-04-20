package com.resto.shop.web.dao;

import com.resto.shop.web.model.ShopCart;

import java.util.List;

import com.resto.brand.core.generic.GenericDao;

public interface ShopCartMapper  extends GenericDao<ShopCart,Integer> {
    int deleteByPrimaryKey(Integer id);

    int insert(ShopCart record);

    int insertSelective(ShopCart record);

    ShopCart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ShopCart record);

    int updateByPrimaryKey(ShopCart record);

    List<ShopCart> listUserAndShop(ShopCart shopcart);

    ShopCart selectShopCartItem(ShopCart shopCart);

    void updateShopCartItem(ShopCart shopCartItem);


	void clearShopCart(String customerId, Integer distributionModeId, String shopDetailId);
}
