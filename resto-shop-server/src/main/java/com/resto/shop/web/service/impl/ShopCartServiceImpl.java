package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.ShopCartMapper;
import com.resto.shop.web.model.ShopCart;
import com.resto.shop.web.service.ShopCartService;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 */
@RpcService
public class ShopCartServiceImpl extends GenericServiceImpl<ShopCart, Integer> implements ShopCartService {

    @Resource
    private ShopCartMapper shopcartMapper;

    @Override
    public GenericDao<ShopCart, Integer> getDao() {
        return shopcartMapper;
    }

    @Override
    public List<ShopCart> listUserAndShop(ShopCart shopcart) {
        return shopcartMapper.listUserAndShop(shopcart);
    }

    @Override
    public void updateShopCart(ShopCart shopCart) {
        //先查询当前客户是否有该商品的 购物车的条目
        Integer number = shopCart.getNumber();
        ShopCart shopCartItem = shopcartMapper.selectShopCartItem(shopCart);
        if (shopCartItem == null && number > 0) {
            insertShopCart(shopCart);
        } else if (shopCartItem != null && number > 0) {
            shopCartItem.setNumber(number);
            shopcartMapper.updateShopCartItem(shopCartItem);
        } else if (shopCartItem != null && number <= 0) {
            deleteShopCartItem(shopCartItem.getId());
        }

    }

    private void deleteShopCartItem(Integer id) {
        shopcartMapper.deleteByPrimaryKey(id);
    }

    private int insertShopCart(ShopCart shopCart) {
        shopcartMapper.insertSelective(shopCart);
        int farId = shopCart.getId();
        return farId;
    }

    @Override
    public void clearShopCart(String customerId, Integer distributionModeId, String shopDetailId) {
        shopcartMapper.clearShopCart(customerId, distributionModeId, shopDetailId);
    }

    @Override
    public void clearShopCart(String customerId, String shopDetailId) {
        shopcartMapper.clearAllShopCart(customerId, shopDetailId);
    }

    @Override
    public void clearShopCartGeekPos(String userId, String shopId) {
        shopcartMapper.clearShopCartGeekPos(userId, shopId);
    }

    @Override
    public List<ShopCart> listUserShopCart(String userId, String shopId, Integer distributionModeId) {
        return  shopcartMapper.listUserShopCart(userId, shopId, distributionModeId);
    }


}
