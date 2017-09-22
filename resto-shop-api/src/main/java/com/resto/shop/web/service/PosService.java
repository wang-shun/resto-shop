package com.resto.shop.web.service;

import com.resto.shop.web.model.Article;
import com.resto.shop.web.posDto.ArticleSupport;

import java.util.List;
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

    /**
     * 菜品上架、下架
     * @param articleId 菜品id
     * @param actived 0 下架  1 上架
     * @return
     */
    void articleActived(String articleId,Integer actived);

    /**
     * 菜品沽清
     * @param articleId 菜品id
     */
    void articleEmpty(String articleId);

    /**
     * 编辑菜品库存
     * @param articleId 菜品id
     * @param count 数量
     */
    void articleEdit(String articleId,Integer count);

    /**
     * 打印成功
     * @param orderId 订单
     */
    void printSuccess(String orderId);

    /**
     * 同步pos端创建的订单
     * @param data json数据
     */
    void syncPosOrder(String data);


    /**
     * 同步pos端订单支付信息
     * @param data json数据
     */
    void syncPosOrderPay(String data);

    /**
     * 同步pos端退菜信息
     * @param data json数据
     */
    String syncPosRefundOrder(String data);


    /**
     * pos确认订单
     * @param orderId 订单id
     */
    void syncPosConfirmOrder(String orderId);

    /**
     * 同步菜品供应时间
     * @return
     */
    List<ArticleSupport>  syncArticleSupport(String shopId);

    void syncChangeTable(String orderId,String tableNumber);

    void syncOpenTable(String tableNumber);



    void test();
}
