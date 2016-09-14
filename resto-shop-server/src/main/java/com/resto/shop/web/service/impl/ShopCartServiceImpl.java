package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.constant.ShopCarType;
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
    public int updateShopCart(ShopCart shopCart) {
        //先查询当前客户是否有该商品的 购物车的条目
        Integer number = shopCart.getNumber();
        ShopCart shopCartItem = new ShopCart();
        if(shopCart.getShopType().equals(ShopCarType.TAOCAN)){
            insertShopCart(shopCart);
            return shopCart.getId();
        } else if (shopCart.getShopType().equals(ShopCarType.TAOCANDANPIN)) {
            insertShopCart(shopCart);
            return shopCart.getId();
        } else if (shopCart.getShopType().equals(ShopCarType.DANPIN)) {
            shopCartItem = shopcartMapper.selectShopCartItem(shopCart);
        }
        if(shopCart.getShopType().equals(ShopCarType.DANPIN)){
            if (shopCartItem == null && number > 0) {
                insertShopCart(shopCart);
                return shopCart.getId();
            } else if (shopCartItem != null && number > 0) {
                shopCartItem.setNumber(number);
                shopcartMapper.updateShopCartItem(shopCartItem);
            } else if (shopCartItem != null && number <= 0) {
                deleteShopCartItem(shopCartItem.getId());
            }
        }
        return 0;
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


}
