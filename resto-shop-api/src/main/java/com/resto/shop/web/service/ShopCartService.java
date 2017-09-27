package com.resto.shop.web.service;

import java.util.List;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.ShopCart;

public interface ShopCartService extends GenericService<ShopCart, Integer> {

    List<ShopCart> listUserAndShop(ShopCart shopcart);

    Integer updateShopCart(ShopCart shopCart);

	void clearShopCart(String customerId, Integer distributionModeId, String shopDetailId);

	void clearShopCart(String currentCustomerId, String currentShopId);

    void clearShopCartGeekPos(String userId, String shopId);

    List<ShopCart> listUserShopCart(String userId, String shopId, Integer distributionModeId);

    void delMealArticle(String id);

    void delMealItem(String articleId);

    ShopCart selectByUuId(String uuid);

    void deleteCustomerArticle(String customerId,String articleId);

    List<ShopCart> selectListByGroupId(String groupId);
}
