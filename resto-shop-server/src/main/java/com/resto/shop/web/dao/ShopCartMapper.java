package com.resto.shop.web.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.ShopCart;

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

	void clearAllShopCart(@Param("customrId")String customerId, @Param("shopDetailId")String shopDetailId);
}
