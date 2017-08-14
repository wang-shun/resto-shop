package com.resto.shop.web.service;

import com.resto.shop.web.model.Article;

import java.util.Map;

/**
 * Created by KONATA on 2017/8/9.
 */
public interface PosService {
    /**
     * 同步店铺菜品库存 (服务器->pos)
     * @param shopId 店铺id
     * @return
     */
    String syncArticleStock(String shopId);


    /**
     * 当门店后台数据发生变更时通知pos （服务器->pos）
     * @param shopId 发生信息变更的门店
     * @return 发生信息变更的门店
     */
    String shopMsgChange(String shopId);

    /**
     * 同步订单创建时的订单信息 （服务器->pos）
     * @param orderId
     * @return
     */
    String syncOrderCreated(String orderId);

    /**
     * 同步订单支付时的信息 （服务器 ->pos）
     * @param orderId
     * @return
     */
    String syncOrderPay(String orderId);

    /**
     * 同步第三方外卖数据（服务器->pos）
     * @param orderId 第三方外卖id
     */
    String syncPlatform(String orderId);

}
