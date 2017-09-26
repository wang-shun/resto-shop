package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.constant.Common;
import com.resto.shop.web.constant.ShopCarType;
import com.resto.shop.web.dao.ShopCartMapper;
import com.resto.shop.web.model.ShopCart;
import com.resto.shop.web.service.ShopCartService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 */
@RpcService
public class ShopCartServiceImpl extends GenericServiceImpl<ShopCart, Integer> implements ShopCartService {

    @Resource
    private ShopCartMapper shopcartMapper;


    @Autowired
    private ShopDetailService shopDetailService;


    @Override
    public GenericDao<ShopCart, Integer> getDao() {
        return shopcartMapper;
    }

    @Override
    public List<ShopCart> listUserAndShop(ShopCart shopcart) {
        ShopDetail shopDetail = shopDetailService.selectByPrimaryKey(shopcart.getShopDetailId());
        if(shopDetail.getOpenManyCustomerOrder() == Common.YES && shopcart.getGroupId() != null){
            //开启多人点餐
            return shopcartMapper.getListByGroupId(shopcart.getGroupId());
        }else{
            return shopcartMapper.listUserAndShop(shopcart);
        }

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
        } else if (shopCart.getShopType().equals(ShopCarType.CAOBAO)){
            if(shopCartItem == null){
                insertShopCart(shopCart);
                return shopCart.getId();
            }else{
                shopCartItem.setNumber(shopCart.getNumber());
                shopcartMapper.updateShopCartItem(shopCartItem);
                shopcartMapper.delMealItem(shopCartItem.getId().toString());
                return shopCartItem.getId();
            }
        } else if (shopCart.getShopType().equals(ShopCarType.CAOBAODANPIN)){
            insertShopCart(shopCart);
        } else if (shopCart.getShopType().equals(ShopCarType.XINGUIGE)){
            insertShopCart(shopCart);
            return shopCart.getId();
        } else if (shopCart.getShopType().equals(ShopCarType.XINGUIGECAOBAODANPIN)){
            insertShopCart(shopCart);
        }
        if(shopCart.getShopType().equals(ShopCarType.DANPIN)){
            if (shopCartItem == null && number > 0) {
                log.info(shopCart.getRecommendArticleId());
                insertShopCart(shopCart);
                return number;
            } else if (shopCartItem != null && number > 0) {
                shopCartItem.setNumber(number);
                shopcartMapper.updateShopCartItem(shopCartItem);
                return number - oldNumber;
            } else if (shopCart != null){
                if(number <= 0 &&  shopCart.getId() != null){
                    shopcartMapper.delMealArticle(shopCart.getId().toString());
                }else if(number <= 0 &&  shopCart.getId() == null &&  shopCartItem != null && shopCartItem.getId() != null){
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

    @Override
    public ShopCart selectByUuId(String uuid) {
        return shopcartMapper.selectByUuId(uuid);
    }

    @Override
    public void clearGroupId(Integer id) {
        shopcartMapper.clearGroupId(id);
    }

    @Override
    public void deleteCustomerArticle(String customerId, String articleId) {
        shopcartMapper.deleteCustomerArticle(customerId, articleId);
    }

    @Override
    public void groupNew(String customerId, String shopId, String groupId) {
        shopcartMapper.groupNew(customerId, shopId, groupId);
    }


    @Override
    public Boolean checkRepeat(String articleId, String groupId) {
        return shopcartMapper.checkRepeat(articleId, groupId) > 1;
    }
}
