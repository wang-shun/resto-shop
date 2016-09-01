package com.resto.shop.web.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.ShopCartMapper;
import com.resto.shop.web.model.ShopCart;
import com.resto.shop.web.service.ShopCartService;

import cn.restoplus.rpc.server.RpcService;

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
        if("1".equals(shopCart.getShopType())){
            Integer number = shopCart.getNumber();
            ShopCart shopCartItem  = shopcartMapper.selectShopCartItem(shopCart);
            if(shopCartItem==null&&number>0){
                insertShopCart(shopCart);
                return shopCart.getId();
            }else if(shopCartItem!=null&&number>0){
                shopCartItem.setNumber(number);
                shopcartMapper.updateShopCartItem(shopCartItem);
                return  shopCartItem.getId();
            }else if(shopCartItem!=null&&number<=0){
                deleteShopCartItem(shopCartItem.getId());
                return shopCartItem.getId();
            }
        } else {
            insertShopCart(shopCart);
            return shopCart.getId();
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
		shopcartMapper.clearShopCart(customerId,distributionModeId,shopDetailId);
	}

	@Override
	public void clearShopCart(String customerId, String shopDetailId) {
		shopcartMapper.clearAllShopCart(customerId, shopDetailId);
	}

    

}
