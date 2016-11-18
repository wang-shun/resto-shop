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
    public Integer updateShopCart(ShopCart shopCart) {
        //先查询当前客户是否有该商品的 购物车的条目
        Integer number = shopCart.getNumber();
        Integer oldNumber = new Integer(0);
        ShopCart shopCartItem = shopcartMapper.selectShopCartItem(shopCart);
        if(shopCartItem != null){
            oldNumber = shopCartItem.getNumber();
        }
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
                return number;
            } else if (shopCartItem != null && number > 0) {
                shopCartItem.setNumber(number);
                shopcartMapper.updateShopCartItem(shopCartItem);
                return number - oldNumber;
            }else if(shopCart != null){
                if(number <= 0 &&  shopCart.getId() != null){
                    shopcartMapper.delMealArticle(shopCart.getId().toString());
                }else if(number <= 0 &&  shopCart.getId() == null && shopCartItem.getId() != null){
                    shopcartMapper.delMealArticle(shopCartItem.getId().toString());
                }
            }
        }
        return 0;
    }

    private void deleteShopCartItem(Integer id) {
        shopcartMapper.deleteByPrimaryKey(id);
    }

    private Integer insertShopCart(ShopCart shopCart) {
        shopcartMapper.insertSelective(shopCart);
        Integer farId = shopCart.getId();
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

    @Override
    public void delMealArticle(String id) {
        shopcartMapper.delMealArticle(id);
    }

    @Override
    public void delMealItem(String articleId) {
        shopcartMapper.delMealItem(articleId);
    }
}
