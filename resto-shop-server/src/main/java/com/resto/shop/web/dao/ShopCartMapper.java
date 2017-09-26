package com.resto.shop.web.dao;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.ShopCart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

	void clearAllShopCart(String customerId,String shopDetailId);

    void clearShopCartGeekPos(String userId, String shopId);

    List<ShopCart> listUserShopCart(String userId, String shopId, Integer distributionModeId);

    void delMealArticle(String id);

    void delMealItem(String articleId);

    ShopCart selectByUuId(String uuid);

    void deleteCustomerArticle(@Param("customerId") String customerId,@Param("articleId") String articleId);

    void clearGroupId(Integer id);
}
