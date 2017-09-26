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

    void clearGroupId(Integer id);


    void deleteCustomerArticle(String customerId,String articleId);

    /**
     * 把用户在这家店铺的购物车同步给某个组
     * @param customerId
     * @param shopId
     * @param groupId
     */
    void groupNew(String customerId,String shopId,String groupId);
}
