package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.ShopCart;

public interface ShopCartService extends GenericService<ShopCart, Integer> {

    List<ShopCart> listUserAndShop(ShopCart shopcart);

    int updateShopCart(ShopCart shopCart);

	void clearShopCart(String customerId, Integer distributionModeId, String shopDetailId);

	void clearShopCart(String currentCustomerId, String currentShopId);
    
}
